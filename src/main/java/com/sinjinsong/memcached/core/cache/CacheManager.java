package com.sinjinsong.memcached.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sinjinsong
 * @date 2018/4/3
 */

public class CacheManager {
    private volatile static CacheManager instance;
    private Map<String, String> cache;

    private CacheManager() {
        cache = new ConcurrentHashMap<>();
    }

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

    public String get(String key) {
        return cache.get(key);
    }

    public void set(String key, String value) {
        cache.put(key, value);
    }

    public String delete(String key) {
        return cache.remove(key);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }
}
