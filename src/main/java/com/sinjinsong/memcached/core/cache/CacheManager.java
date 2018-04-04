package com.sinjinsong.memcached.core.cache;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例的缓存管理器，线程安全
 * @author sinjinsong
 * @date 2018/4/3
 */
@Slf4j
public class CacheManager {
    private volatile static CacheManager instance;
    private Map<String, ValueHolder> cache;

    private CacheManager() {
        cache = new ConcurrentHashMap<>();
    }

    /**
     * 双重检查加锁的单例模式
     * @return
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    public ValueHolder get(String key) {
        log.debug("before get | cache:{}",cache);
        return cache.get(key);
    }

    public void set(String key, ValueHolder valueHolder) {
        cache.put(key, valueHolder);
        log.debug("after set | cache:{}",cache);
    }

    public void delete(String key) {
        log.debug("before delete | cache:{}",cache);
        cache.remove(key);
    }

    public boolean contains(String key) {
        log.debug("before contains | cache:{}",cache);
        return cache.containsKey(key);
    }
    
    @Data    
    @Builder
    public static class ValueHolder {
        String value;
        int flags;
        /**
         * 如果为0，则表示永不过期
         */
        long expireTime;
        public static long NO_EXPIRE = 0;
    }
}
