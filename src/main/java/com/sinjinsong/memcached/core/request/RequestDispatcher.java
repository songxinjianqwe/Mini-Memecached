package com.sinjinsong.memcached.core.request;

import com.sinjinsong.memcached.core.Server;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class RequestDispatcher {
    private ThreadPoolExecutor pool;
    private Map<SocketChannel, RequestHandler> handlers;
    private Server server;
    public RequestDispatcher(Server server) {
        this.server = server;
        this.pool = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
        this.handlers = new ConcurrentHashMap<>();
    }

    public void execute(SocketChannel socketChannel) {
        if (handlers.containsKey(socketChannel)) {
            pool.submit(handlers.get(socketChannel));
        } else {
            RequestHandler requestHandler = new RequestHandler(this,socketChannel);
            handlers.put(socketChannel,requestHandler);
            pool.submit(requestHandler);
        }
    }
    
    public void closeClient(SocketChannel socketChannel) {
        handlers.remove(socketChannel);
        server.closeClient(socketChannel);
    }

    public void close() {
        this.pool.shutdown();
    }
}
