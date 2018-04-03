package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import static com.sinjinsong.memcached.core.constant.MessageConstant.STORED;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class SetCommand implements Command {

    @Override
    public boolean supports(String commandLine, RequestHandler requestHandler) {
        return commandLine.startsWith("set") || 
                (requestHandler.getLastCommandLine() != null && 
                        requestHandler.getLastCommandLine().startsWith("set"));
    }

    /**
     * set key1
     *
     * @param commandLine
     * @param manager
     * @param requestHandler
     * @return
     */
    @Override
    public String execute(String commandLine, CacheManager manager, RequestHandler requestHandler) {
        String lastCommandLine = requestHandler.getLastCommandLine();
        try {
            if (lastCommandLine == null) {
                log.info("set命令，等待value数据读取");
                requestHandler.setLastCommandLine(commandLine);
                return null;
            }
            log.info("set命令,lastCommandLine为{},commandLine为{}",lastCommandLine,commandLine);
            String[] slices = lastCommandLine.split(" ");
            String command = slices[0];
            String key = slices[1];
            String value = commandLine;
            log.info("command : {}, key:{}, value:{}", command, key, value);
            manager.set(key, value);
            requestHandler.setLastCommandLine(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return STORED;
    }
}
