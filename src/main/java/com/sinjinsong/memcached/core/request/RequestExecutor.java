package com.sinjinsong.memcached.core.request;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端读事件就绪后的请求分发器
 *
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class RequestExecutor {
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 每个客户端的连接总是对应同一个RequestHandler，在RequestHandler中保存了客户端的历史数据
     *
     * @param
     * @param connection
     */
    public void submit(String commandLine, Connection connection) {
        pool.submit(() -> connection.handle(commandLine));
    }

    /**
     * 关闭
     */
    public void close() {
        pool.shutdown();
    }
}
