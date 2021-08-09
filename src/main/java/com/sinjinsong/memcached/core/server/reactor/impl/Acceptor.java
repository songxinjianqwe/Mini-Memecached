package com.sinjinsong.memcached.core.server.reactor.impl;

import com.sinjinsong.memcached.core.request.Connection;
import com.sinjinsong.memcached.core.server.reactor.AbstractReactorImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

/**
 * 客户端连接事件的处理器
 *
 * @author songxinjian
 * @date 2021/8/9
 */
@Slf4j
public class Acceptor extends AbstractReactorImpl {
    private final Supplier<Poller> pollerSupplier;

    public Acceptor(Supplier<Poller> pollerSupplier) {
        super("Acceptor");
        this.pollerSupplier = pollerSupplier;
    }

    @Override
    public void handle(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                //如果"读取"事件已就绪
                //交由读取事件的处理器处理
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                log.info("服务器连接客户端连接");
                log.info("客户端为:{}", socketChannel.getRemoteAddress());
                try {
                    Poller poller = pollerSupplier.get();

                    poller.submit(new Runnable() {
                        @Override
                        public void run() {
                            Selector selector = poller.getSelector();
                            try {
                                socketChannel.configureBlocking(false);
                                socketChannel.register(selector, SelectionKey.OP_READ, new Connection(socketChannel, poller, key));
                                selector.wakeup();
                                log.info("register {} to reactor {} succeeded", socketChannel, name);
                            } catch (Throwable t) {
                                log.error("register failed, caused by:", t);
                                try {
                                    socketChannel.close();
                                } catch (IOException e) {
                                    log.warn("close failed, caused by: ", e);
                                }
                            }
                        }
                    });
                } catch (Throwable t) {
                    log.error("register failed, caused by: ", t);
                    key.cancel();
                    if (key.channel() != null) {
                        key.channel().close();
                    }
                }
            }
        } catch (Throwable t) {
            log.error("{} handle failed, caused by", name, t);
        }
    }
}
