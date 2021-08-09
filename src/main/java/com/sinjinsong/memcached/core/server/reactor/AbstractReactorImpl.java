package com.sinjinsong.memcached.core.server.reactor;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author songxinjian
 * @date 2021/8/11
 */
@Slf4j
public abstract class AbstractReactorImpl implements Reactor, Runnable {
    private Thread thread;
    private Selector selector;
    /**
     * 为什么要用queue？
     * 因为select和register共用了一把锁，要么select，要么register，无法同时调用，所以要放到同一个线程里执行
     */
    private final Queue<Runnable> q = new ConcurrentLinkedQueue<>();
    protected String name;

    public AbstractReactorImpl(String name) {
        this.name = name;
    }


    @Override
    public void init() {
        try {
            this.selector = Selector.open();
            this.thread = new Thread(this, name);
            this.thread.start();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }



    @Override
    public String getName() {
        return name;
    }

    @Override
    public void submit(Runnable runnable) {
        q.add(runnable);
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public void close() {
        if(selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                log.warn("close selector failed, caused by: ", e);
            }
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public final void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processTasks();
                if (selector.select(1000) <= 0) {
                    continue;
                }
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    handle(key);
                    it.remove();
                }
            } catch (Throwable t) {
                log.error("reactor handle event failed, caused by: ", t);
            }
        }
    }

    private void processTasks() {
        while (!q.isEmpty()) {
            Runnable runnable = q.poll();
            try {
                runnable.run();
            } catch (Throwable t) {
                log.error("process task failed", t);
            }
        }
    }
}
