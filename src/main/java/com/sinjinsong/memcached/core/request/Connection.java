package com.sinjinsong.memcached.core.request;

import com.sinjinsong.memcached.core.command.CommandExecutor;
import com.sinjinsong.memcached.core.constant.CharsetProperties;
import com.sinjinsong.memcached.core.server.reactor.impl.Poller;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import sun.swing.StringUIClientPropertyKey;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.sinjinsong.memcached.core.constant.MessageConstant.LINE_SEPARATOR;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
@Data
public class Connection {
    private static final int MAX_LENGTH = 1024;
    private SocketChannel socketChannel;
    private String lastCommandLine;
    private Poller poller;
    private ByteBuffer readBuffer;
    private Queue<ByteBuffer> writeQ = new ConcurrentLinkedQueue<>();
    private SelectionKey selectionKey;

    public Connection(SocketChannel socketChannel, Poller poller, SelectionKey key) throws IOException {
        this.socketChannel = socketChannel;
        this.poller = poller;
        this.readBuffer = ByteBuffer.allocate(MAX_LENGTH);
        this.selectionKey = key;
    }

    public Optional<String> doRead() throws IOException {
        socketChannel.read(readBuffer);
        int endIndex = -1;
        for (int i = 0; i < readBuffer.position(); i++) {
            if (readBuffer.get(i) == '\r') {
                if (i < readBuffer.position()) {
                    i++;
                    if (readBuffer.get(i) == '\n') {
                        endIndex = i;
                        break;
                    }
                }
            }
        }
        // 没有读到结束符
        if (endIndex == -1) {
            // 已经达到最大长度了
            if (readBuffer.remaining() == MAX_LENGTH) {
                readBuffer.clear();
            }
            readBuffer.reset();
            return Optional.empty();
        } else {
            // 开始往外读，此时\r\n后面可能还有内容，可能就没有了
            // 如果还有的话，假设目前\r\n到第10个字节
            // endIndex=9，position = 20, limit = 1024（共20个字节）
            int length = endIndex + 1;
            byte[] input = new byte[length];
            // position = 0, limit = 20
            readBuffer.flip();
            // position = 14, limit = 20
            readBuffer.get(input, 0, length);
            // 此时应该只留下剩下的部分
            // position = 0, limit = 1024
            readBuffer.compact();
            // 跳过最后的\r和\n
            return Optional.of(new String(input, 0, length - 2));
        }
    }

    public void handle(String commandLine) {
        log.info("commandLine:{}, lastCommandLine:{}", commandLine, lastCommandLine);
        String[] result = CommandExecutor.execute(this, commandLine);
        try {
            // 将结果逐行拼接，进行输出
            if (result != null) {
                StringBuilder sb = new StringBuilder();
                for (String res : result) {
                    sb.append(res);
                    sb.append(LINE_SEPARATOR);
                }
                writeQ.add(ByteBuffer.wrap(sb.toString().getBytes(CharsetProperties.UTF_8_CHARSET)));
                poller.submit(this::doWrite);
            }
        } catch (Throwable t) {
            log.error("handle failed, caused by: ", t);
        }
    }

    public void doWrite() {
        if (writeQ.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ & ~SelectionKey.OP_WRITE);
            return;
        }
        try {
            while (!writeQ.isEmpty()) {
                ByteBuffer buffer = writeQ.peek();
                int bytes = socketChannel.write(buffer);
                if (bytes <= 0) {
                    break;
                }
                if (bytes < buffer.capacity()) {
                    // 将buffer中已经写完的部分移除掉
                    buffer.compact();
                    break;
                } else {
                    writeQ.remove();
                }
            }
            if (!writeQ.isEmpty() && (selectionKey.interestOps() & SelectionKey.OP_WRITE) != 0) {
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            log.error("write failed", e);
        }
    }

    public void closeClient() {
        try {
            socketChannel.close();
        } catch (Throwable t) {
            log.error("close client failed, caused by: ", t);
        }
    }
}
