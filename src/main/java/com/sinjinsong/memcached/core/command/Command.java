package com.sinjinsong.memcached.core.command;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.request.RequestHandler;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
public interface Command {
    boolean supports(String commandLine, RequestHandler requestHandler);
    String execute(String commandLine, CacheManager manager, RequestHandler requestHandler);
}
