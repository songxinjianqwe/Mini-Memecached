package com.sinjinsong.memcached.core.command;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.impl.DeleteCommand;
import com.sinjinsong.memcached.core.command.impl.GetCommand;
import com.sinjinsong.memcached.core.command.impl.QuitCommand;
import com.sinjinsong.memcached.core.command.impl.SetCommand;
import com.sinjinsong.memcached.core.constant.MessageConstant;
import com.sinjinsong.memcached.core.request.Connection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令执行器
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class CommandExecutor {
    /**
     * 责任链模式
     */
    private static List<Command> chain;

    static {
        // 这里是硬编码了，如果可以，使用Spring的getBean(Command.class)可以避免硬编码
        chain = new ArrayList<>();
        chain.add(new DeleteCommand());
        chain.add(new SetCommand());
        chain.add(new GetCommand());
        chain.add(new QuitCommand());
    }

    /**
     * 文本协议解析+执行
     *
     * @param commandString
     * @return
     */
    public static String[] execute(Connection connection, String commandString) {
        log.info("开始尝试执行命令...");
        CacheManager cacheManager = CacheManager.getInstance();
        for (Command command : chain) {
            if (command.supports(commandString, connection)) {
                return command.execute(commandString, cacheManager, connection);
            }
        }
        log.info("不支持该指令 {}", commandString);
        return new String[]{MessageConstant.ERROR};
    }
}
