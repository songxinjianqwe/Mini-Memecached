package com.sinjinsong.memcached.core.server.reactor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * 内部持有一个Selector
 * @author songxinjian
 * @date 2021/8/11
 */
public interface Reactor {
    void init();
    String getName();
    void submit(Runnable runnable);
    void handle(SelectionKey selectionKey);
    void close();
    Selector getSelector();
}
