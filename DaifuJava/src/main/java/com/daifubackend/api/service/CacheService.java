package com.daifubackend.api.service;

import org.springframework.cache.Cache;

import java.util.List;

public interface CacheService {
    boolean clearAllCaches();
    Cache clearIndividualCache(String cacheName);
    boolean clearIndividualCacheEntry(String cacheName, String key);
    List<Object> getAllCachesEntries();
    List<String> getAllCacheNames();
    List<Object> getAllCaches();
    Object getCacheEntries(String cacheName);
    Cache getCache(String cacheName);
    boolean addNewCacheEntry(String cacheName, String key, Object value);
    Object getCacheEntry(String cacheName, String key);
}
