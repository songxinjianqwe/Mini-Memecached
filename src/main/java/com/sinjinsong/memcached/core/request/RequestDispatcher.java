package com.sinjinsong.memcached.core.request;

import com.sinjinsong.memcached.core.server.Server;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端读事件就绪后的请求分发器
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

    /**
     * 每个客户端的连接总是对应同一个RequestHandler，在RequestHandler中保存了客户端的历史数据
     * @param socketChannel
     */
    public void dispatch(SocketChannel socketChannel) {
        if (handlers.containsKey(socketChannel)) {
            pool.submit(handlers.get(socketChannel));
        } else {
            RequestHandler requestHandler = new RequestHandler(this,socketChannel);
            handlers.put(socketChannel,requestHandler);
            pool.submit(requestHandler);
        }
    }

    /**
     * 关闭一个客户端连接，当客户端输入quit指令时
     * @param socketChannel
     */
    public void closeClient(SocketChannel socketChannel) {
        try {
            server.unregisterClient(socketChannel);
            handlers.remove(socketChannel);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭
     */
    public void close() {
        for(SocketChannel socketChannel : handlers.keySet()) {
            closeClient(socketChannel);
        }
        handlers.clear();
        pool.shutdown();
    }
}
