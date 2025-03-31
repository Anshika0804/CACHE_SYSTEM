package org.example;


import org.example.cache.LRUCache;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2); // Capacity = 2

        cache.put(1, 10);
        cache.put(2, 20);
        System.out.println(cache.getKey(1)); // Returns 10

        cache.put(3, 30); // Removes key 2
        System.out.println(cache.getKey(2)); // Returns -1 (not found)

        cache.put(4, 40); // Removes key 1
        System.out.println(cache.getKey(1)); // Returns -1 (not found)
        System.out.println(cache.getKey(3)); // Returns 30
        System.out.println(cache.getKey(4)); // Returns 40
    }
}
