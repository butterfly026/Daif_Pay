package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.OrderMapper;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.CacheService;
import com.daifubackend.api.service.OrderService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
public class CacheServiceImpl implements CacheService {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public boolean clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        return true;
    }

    @Override
    public Cache clearIndividualCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return cache;
        } else {
            return null;
        }
    }

    @Override
    public boolean clearIndividualCacheEntry(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            if (key != null && !key.isEmpty()) {
                cache.evictIfPresent(key);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public List<Object> getAllCachesEntries() {
        List<Object> caches = new ArrayList<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();

        if (!cacheNames.isEmpty()) {
            cacheNames.forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    caches.add(cache.getNativeCache());
                    System.out.println("Retrieved cache: " + cacheName);
                } else {
                    System.out.println("Cache not found: " + cacheName);
                }
            });
            return caches;
        } else {
            System.out.println("No caches found.");
            return null;
        }
    }

    @Override
    public List<String> getAllCacheNames() {
        return new ArrayList<>(cacheManager.getCacheNames());
    }

    @Override
    public List<Object> getAllCaches() {
        List<Object> caches = new ArrayList<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();

        if (!cacheNames.isEmpty()) {
            cacheNames.forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    caches.add(cache);
                    System.out.println("Retrieved cache: " + cacheName);
                }
            });
            return caches;
        } else {
            System.out.println("No caches defined.");
            return null;
        }
    }

    @Override
    public Object getCacheEntries(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            return cache.getNativeCache();
        } else {
            System.out.println("Cache not found: " + cacheName);
            return null;
        }
    }

    @Override
    public Cache getCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            System.out.println("Cache found: " + cacheName);
            return cache;
        } else {
            System.out.println("Cache not found: " + cacheName);
            return null;
        }
    }

    @Override
    public boolean addNewCacheEntry(String cacheName, String key, Object value) {
        if (cacheName == null || key == null || value == null) {
            System.out.println("Cache entry key or value is missing");
            return false;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            System.out.println("Cache entry added successfully in cache: " + cacheName);
            return true;
        } else {
            System.out.println("Cache not found: " + cacheName);
            return false;
        }
    }

    @Override
    public Object getCacheEntry(String cacheName, String key) {
        if (cacheName == null || key == null) {
            System.out.println("Cache entry key is missing");
            return null;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Object value = cache.get(key);
            System.out.println("Cache entry added successfully in cache: " + cacheName);
            return value;
        } else {
            System.out.println("Cache not found: " + cacheName);
            return null;
        }
    }


}
