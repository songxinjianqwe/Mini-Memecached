package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.RequestHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class GetCommand implements Command {

    @Override
    public boolean supports(String commandLine, RequestHandler requestHandler) {
        return commandLine.startsWith("get");
    }

    @Override
    public String execute(String commandLine, CacheManager manager, RequestHandler requestHandler) {
        try {
            String[] slices = commandLine.split(" ");
            String command = slices[0];
            String key = slices[1];
            log.info("command : {}, key:{}",command,key);
            if(manager.contains(key)) {
                return manager.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
