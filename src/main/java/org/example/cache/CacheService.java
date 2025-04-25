package org.example.cache;

import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private final LRUCache cache;

    public CacheService() {
        this.cache = new LRUCache(100); // default capacity
    }

    public int get(int key) {
        return cache.getKey(key);
    }

    public void put(int key, int value, long ttl) {
        cache.put(key, value, ttl);
    }
}
