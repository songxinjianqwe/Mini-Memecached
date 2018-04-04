package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import static com.sinjinsong.memcached.core.cache.CacheManager.ValueHolder.NO_EXPIRE;
import static com.sinjinsong.memcached.core.constant.MessageConstant.BLANK;
import static com.sinjinsong.memcached.core.constant.MessageConstant.CLIENT_ERROR;
import static com.sinjinsong.memcached.core.constant.MessageConstant.STORED;

/**
 * Set 指令 执行器
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
    public String[] execute(String commandLine, CacheManager manager, RequestHandler requestHandler) {
        String lastCommandLine = requestHandler.getLastCommandLine();
        try {
            if (lastCommandLine == null) {
                log.info("set命令，等待value数据读取");
                requestHandler.setLastCommandLine(commandLine);
                return null;
            }
            log.info("set命令,lastCommandLine为{},commandLine为{}", lastCommandLine, commandLine);
            String[] slices = lastCommandLine.split(BLANK);
            String command = slices[0];
            String key = slices[1];
            int flags = Integer.parseInt(slices[2]);
            int expireTime = Integer.parseInt(slices[3]);
            String value = commandLine;
            log.info("command : {}, key:{}, value:{}", command, key, value);
            manager.set(key, 
                    CacheManager.ValueHolder.builder()
                            .value(value)
                            .expireTime(expireTime == NO_EXPIRE ? NO_EXPIRE : System.currentTimeMillis() + expireTime * 1000)
                            .flags(flags).build());
            requestHandler.setLastCommandLine(null);
        } catch (Exception e) {
            requestHandler.setLastCommandLine(null);
            e.printStackTrace();
            return new String[]{CLIENT_ERROR};
        }
        return new String[]{STORED};
    }
}
