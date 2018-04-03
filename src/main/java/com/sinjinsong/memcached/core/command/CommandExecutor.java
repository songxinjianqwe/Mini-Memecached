package com.sinjinsong.memcached.core.command;

import com.sinjinsong.memcached.core.cache.CacheManager;
import com.sinjinsong.memcached.core.command.impl.DeleteCommand;
import com.sinjinsong.memcached.core.command.impl.GetCommand;
import com.sinjinsong.memcached.core.command.impl.SetCommand;
import com.sinjinsong.memcached.core.constant.MessageConstant;
import com.sinjinsong.memcached.core.request.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class CommandExecutor {
    private static List<Command> chain;
    static {
        chain = new ArrayList<>();
        chain.add(new DeleteCommand());
        chain.add(new SetCommand());
        chain.add(new GetCommand());
    }
    /**
     * 文本协议解析+执行
     * @param commandString
     * @return
     */
    public static String execute(RequestHandler requestHandler, String commandString) {
        log.info("开始尝试执行命令...");
        CacheManager cacheManager = CacheManager.getInstance();
        for(Command command: chain) {
            if(command.supports(commandString,requestHandler)) {
                return command.execute(commandString,cacheManager,requestHandler);
            }
        }
        log.info("不支持该指令 {}",commandString);
        return MessageConstant.ERROR;
    }
}
