package com.sinjinsong.memcached.core.server;

import com.sinjinsong.memcached.core.request.RequestExecutor;
import com.sinjinsong.memcached.core.server.reactor.impl.Acceptor;
import com.sinjinsong.memcached.core.server.reactor.impl.Poller;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务器主体，监听在8888端口
 * 基于Java NIO，实现IO多路复用
 * 支持多个客户端连接
 *
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class Server {
    private static final int DEFAULT_PORT = 8888;
    private static final int POLLER_SIZE = Runtime.getRuntime().availableProcessors();
    private ServerSocketChannel server;
    private Acceptor acceptor;
    private RequestExecutor requestExecutor;
    private Poller[] pollers;

    /**
     * 启动服务器
     */
    public void start() {
        try {
            initServerSocket(DEFAULT_PORT);
            initRequestDispatcher();
            initPollers();
            initAcceptor();
            log.info("服务器启动");
        } catch (Exception e) {
            log.info("初始化服务器失败", e);
            close();
        }
    }

    public void await() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("服务器关闭中...");
                close();
            }
        });
    }

    private void initRequestDispatcher() {
        requestExecutor = new RequestExecutor();
    }

    private void initServerSocket(int port) throws IOException {
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(false);
    }

    private void initPollers() {
        this.pollers = new Poller[POLLER_SIZE];
        for (int i = 0; i < POLLER_SIZE; i++) {
            pollers[i] = new Poller(requestExecutor);
            pollers[i].init();
        }
    }

    private void initAcceptor() {
        this.acceptor = new Acceptor(this::selectPoller);
        this.acceptor.init();
        Selector selector = this.acceptor.getSelector();
        this.acceptor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    server.register(selector, SelectionKey.OP_ACCEPT, null);
                    selector.wakeup();
                    log.info("register server socket channel to acceptor succeeded");
                } catch (Throwable t) {
                    log.error("register failed, caused by:", t);
                    try {
                        server.close();
                        System.exit(1);
                    } catch (IOException e) {
                        log.warn("close failed, caused by: ", e);
                    }
                }
            }
        });
    }

    public Poller selectPoller() {
        return pollers[ThreadLocalRandom.current().nextInt(pollers.length)];
    }

    public void close() {
        try {
            if (acceptor != null) {
                acceptor.close();
            }
            if (pollers != null) {
                for (Poller poller : pollers) {
                    poller.close();
                }
            }
            if (requestExecutor != null) {
                requestExecutor.close();
            }
            if (server != null) {
                server.close();
            }
        } catch (Throwable t) {
            log.warn("close resources failed, casued by: ", t);
        }
    }
}
