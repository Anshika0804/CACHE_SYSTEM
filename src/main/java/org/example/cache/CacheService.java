package org.example.cache;

import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private final LRUCache<Integer, Integer> cache;

    public CacheService() {
        this.cache = new LRUCache<>(100); // default capacity 100
    }

    public Integer get(int key) {
        Integer value = cache.getKey(key);
        return (value != null) ? value : -1;  // Or you can throw exception if you want
    }

    public void put(int key, int value, long ttl) {
        cache.put(key, value, ttl);
    }
}
