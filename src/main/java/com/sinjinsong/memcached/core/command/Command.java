package com.sinjinsong.memcached.core.command;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.request.Connection;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
public interface Command {
    /**
     * 判断该commandLine能否被当前Command所支持
     * @param commandLine
     * @param connection
     * @return
     */
    boolean supports(String commandLine, Connection connection);

    /**
     * 执行该命令
     * @param commandLine
     * @param manager
     * @param connection
     * @return
     */
    String[] execute(String commandLine, CacheManager manager, Connection connection);
}
