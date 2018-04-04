package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import static com.sinjinsong.memcached.core.cache.CacheManager.ValueHolder.NO_EXPIRE;
import static com.sinjinsong.memcached.core.constant.MessageConstant.BLANK;
import static com.sinjinsong.memcached.core.constant.MessageConstant.END;
import static com.sinjinsong.memcached.core.constant.MessageConstant.VALUE;

/**
 * Get 指令执行器
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
    public String[] execute(String commandLine, CacheManager manager, RequestHandler requestHandler) {
        try {
            String[] slices = commandLine.split(BLANK);
            String command = slices[0];
            String key = slices[1];
            log.info("command : {}, key:{}", command, key);
            if (manager.contains(key)) {
                log.info("value:{}", manager.get(key));
                CacheManager.ValueHolder valueHolder = manager.get(key);
                // 惰性过期策略
                if (valueHolder.getExpireTime() != NO_EXPIRE && System.currentTimeMillis() >= valueHolder.getExpireTime()) {
                    log.info("key 已过期:{}", key);
                    manager.delete(key);
                } else {
                    return new String[]{valueFirstLineGen(key, valueHolder),
                            valueHolder.getValue(),
                            END};
                }
            } else {
                log.info("不存在 key:{}", key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{END};
    }

    private static String valueFirstLineGen(String key, CacheManager.ValueHolder valueHolder) {
        StringBuilder sb = new StringBuilder();
        sb.append(VALUE);
        sb.append(BLANK);
        sb.append(key);
        sb.append(BLANK);
        sb.append(valueHolder.getFlags());
        sb.append(BLANK);
        sb.append(valueHolder.getValue().length());
        return sb.toString();
    }
}
