package org.example;


import org.example.cache.LRUCache;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 100, 3000); // Key 1, value 100, TTL 3s
        cache.put(2, 200, 3000);

        System.out.println("Key 1: " + cache.getKey(1)); // Should print 100
        Thread.sleep(4000);
        System.out.println("Key 1 after 4s: " + cache.getKey(1)); // Should print -1 (expired)

    }

}
