package com.sinjinsong.memcached.core.request;

import com.sinjinsong.memcached.core.command.CommandExecutor;
import com.sinjinsong.memcached.core.constant.CharsetProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.sinjinsong.memcached.core.constant.MessageConstant.LINE_SEPARATOR;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
@Data
public class RequestHandler implements Runnable {
    private SocketChannel socketChannel;
    private RequestDispatcher requestDispatcher;
    private String lastCommandLine;

    public RequestHandler(RequestDispatcher requestDispatcher, SocketChannel socketChannel) {
        this.requestDispatcher = requestDispatcher;
        this.socketChannel = socketChannel;
    }

    /**
     * 每次总是读取一行，移除最后的\r\n，交给CommandExecutor
     */
    @Override
    public void run() {
        String commandLine = collectLine().trim();
        if (commandLine == null) {
            log.info("未读取到有效数据，直接丢弃");
            return;
        }
        log.info("commandLine:{}", commandLine);
        log.info("lastCommandLine:{}", lastCommandLine);
        String[] result = CommandExecutor.execute(this, commandLine);
        try {
            // 将结果逐行拼接，进行输出
            if (result != null) {
                StringBuilder sb = new StringBuilder();
                for(String res : result) {
                    sb.append(res);
                    sb.append(LINE_SEPARATOR);
                }
                socketChannel.write(ByteBuffer.wrap(sb.toString().getBytes(CharsetProperties.UTF_8_CHARSET)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String collectLine() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                baos.write(buffer.array());
            }
            baos.close();
        } catch (IOException e) {
            log.info("客户端已关闭连接");
            closeClient();
        }
        byte[] data = baos.toByteArray();
        if (data.length == 0) {
            return null;
        }
        log.info("开始读取Request");
        return new String(data, CharsetProperties.UTF_8_CHARSET).replace("\r\n", "");
    }
    
    public void closeClient() {
        requestDispatcher.closeClient(socketChannel);
    }
}
