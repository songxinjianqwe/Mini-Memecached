package com.sinjinsong.memcached.core.command.impl;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.Command;
import com.sinjinsong.memcached.core.request.Connection;
import lombok.extern.slf4j.Slf4j;

import static com.sinjinsong.memcached.core.constant.MessageConstant.BLANK;
import static com.sinjinsong.memcached.core.constant.MessageConstant.DELETED;
import static com.sinjinsong.memcached.core.constant.MessageConstant.NOT_FOUND;

/**
 * Delete 指令执行器
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class DeleteCommand implements Command {
    @Override
    public boolean supports(String commandLine, Connection connection) {
        return commandLine.startsWith("delete");
    }

    @Override
    public String[] execute(String commandLine, CacheManager manager, Connection connection) {
        try {
            String[] slices = commandLine.split(BLANK);
            String command = slices[0];
            String key = slices[1];
            log.info("command : {}, key:{}", command, key);
            if (manager.contains(key)) {
                manager.delete(key);
                log.info("已删除key:{}", key);
                return new String[]{DELETED};
            }
            log.info("未找到key:{}", key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{NOT_FOUND};
    }
}
