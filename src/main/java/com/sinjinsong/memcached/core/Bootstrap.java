package com.sinjinsong.memcached.core;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
public class Bootstrap {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        server.await();
    }
}
