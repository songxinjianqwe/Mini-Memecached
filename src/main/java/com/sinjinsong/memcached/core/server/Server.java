package com.sinjinsong.memcached.core.server;

import com.sinjinsong.memcached.core.request.RequestDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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
    private ServerSocketChannel server;
    private Selector selector;
    private Thread acceptor;
    private RequestDispatcher requestDispatcher;

    /**
     * 启动服务器
     */
    public void start() {
        try {
            initServerSocket(DEFAULT_PORT);
            initRequestDispatcher();
            initAcceptor();
            log.info("服务器启动");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("初始化服务器失败");
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

    private void initAcceptor() {
        String acceptorName = "Acceptor";
        Acceptor acceptor = new Acceptor(selector, server, requestDispatcher);
        Thread t = new Thread(acceptor, acceptorName);
        t.start();
        this.acceptor = t;
    }

    private void initRequestDispatcher() {
        requestDispatcher = new RequestDispatcher(this);
    }

    private void initServerSocket(int port) throws IOException {
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void close() {
        try {
            if (acceptor != null) {
                acceptor.interrupt();
            }
            if (requestDispatcher != null) {
                requestDispatcher.close();
            }
            if (selector != null) {
                selector.close();
            }
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将客户端从selector中移除
     *
     * @param socketChannel
     */
    public void unregisterClient(SocketChannel socketChannel) {
        socketChannel.keyFor(selector).cancel();
    }

    /**
     * 客户端连接与读事件就绪的处理器
     */
    private static class Acceptor implements Runnable {
        private Selector selector;
        private ServerSocketChannel server;
        private RequestDispatcher requestDispatcher;

        public Acceptor(Selector selector, ServerSocketChannel server, RequestDispatcher requestDispatcher) {
            this.selector = selector;
            this.server = server;
            this.requestDispatcher = requestDispatcher;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (selector.select() <= 0) {
                        continue;
                    }
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey key = it.next();
                        //如果"接收"事件已就绪
                        if (key.isAcceptable()) {
                            //如果"读取"事件已就绪
                            //交由读取事件的处理器处理
                            SocketChannel socketChannel = server.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            log.info("服务器连接客户端连接");
                            log.info("客户端为:{}", socketChannel.getRemoteAddress());
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            log.info("服务器连接客户端读事件就绪");
                            log.info("客户端为:{}", socketChannel.getRemoteAddress());
                            requestDispatcher.dispatch(socketChannel);
                        }
                        it.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
