package com.sinjinsong.memcached.core.server.reactor.impl;

import com.sinjinsong.memcached.core.request.RequestExecutor;
import com.sinjinsong.memcached.core.request.Connection;
import com.sinjinsong.memcached.core.server.reactor.AbstractReactorImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author songxinjian
 * @date 2021/8/9
 */
@Slf4j
public class Poller extends AbstractReactorImpl {
    private static final AtomicInteger INC = new AtomicInteger();
    private final RequestExecutor requestExecutor;

    public Poller(RequestExecutor requestExecutor) {
        super("Poller-" + INC.getAndIncrement());
        this.requestExecutor = requestExecutor;
    }

    @Override
    public void handle(SelectionKey key) {
        try {
            if (key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                log.info("服务器连接客户端读事件就绪");
                log.info("客户端为:{}", socketChannel.getRemoteAddress());

                Connection connection = (Connection) key.attachment();
                Optional<String> input = connection.doRead();
                input.ifPresent(s -> requestExecutor.submit(s, connection));
            } else if (key.isWritable()) {
                Connection connection = (Connection) key.attachment();
                connection.doWrite();
            }
        } catch (Throwable t) {
            log.error("{} handle failed, caused by: ", name, t);
            try {
                key.channel().close();
            } catch (IOException e) {
                log.warn("close channel failed, caused by: ", e);
            }
        }
    }
}
