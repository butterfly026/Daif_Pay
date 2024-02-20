package com.daifubackend.api.utils;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

public class CustomCacheManager extends ConcurrentMapCacheManager {
    @Override
    protected Cache createConcurrentMapCache(String name) {
        return new CustomConcurrentMapCache(name);
    }
}
