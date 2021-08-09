package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.Connection;
import lombok.extern.slf4j.Slf4j;

/**
 * Quit 指令执行器 
 * @author sinjinsong
 * @date 2018/4/4
 */
@Slf4j
public class QuitCommand implements Command {
    @Override
    public boolean supports(String commandLine, Connection connection) {
        return commandLine.startsWith("quit");
    }

    @Override
    public String[] execute(String commandLine, CacheManager manager, Connection connection) {
        log.info("关闭客户端中...");
        connection.closeClient();
        return null;
    }
}
