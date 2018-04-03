package com.sinjinsong.memcached.core.request;

import com.sinjinsong.memcached.core.command.CommandExecutor;
import com.sinjinsong.memcached.core.constant.CharsetProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
     * 对于发送的数据：客户机软件把来自用户终端的按键和命令序列转换为NVT格式，
     * 并发送到服务器，服务器软件将收到的数据和命令，从NVT格式转换为远地系统需要的格式；
     * <p>
     * 对于返回的数据：远地服务器将数据从远地机器的格式转换为NVT格式，
     * 而本地客户机将将接收到的NVT格式数据再转换为本地的格式。
     */
    @Override
    public void run() {
        String commandLine = collectLine();
        if (commandLine == null) {
            log.info("未读取到有效数据，直接丢弃");
            return;
        }
        log.info("{}", commandLine);
        String result = CommandExecutor.execute(this, commandLine);
        try {
            if (result != null) {
                socketChannel.write(ByteBuffer.wrap(new String(result + "\r\n").getBytes(CharsetProperties.UTF_8_CHARSET)));
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
            requestDispatcher.closeClient(socketChannel);
        }
        byte[] data = baos.toByteArray();
        if (data.length == 0) {
            return null;
        }
        log.info("开始读取Request");
        return new String(data, CharsetProperties.UTF_8_CHARSET).replace("\r\n", "");
    }
}
