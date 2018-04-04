package com.sinjinsong.memcached.core.command;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.request.RequestHandler;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
public interface Command {
    /**
     * 判断该commandLine能否被当前Command所支持
     * @param commandLine
     * @param requestHandler
     * @return
     */
    boolean supports(String commandLine, RequestHandler requestHandler);

    /**
     * 执行该命令
     * @param commandLine
     * @param manager
     * @param requestHandler
     * @return
     */
    String[] execute(String commandLine, CacheManager manager, RequestHandler requestHandler);
}
