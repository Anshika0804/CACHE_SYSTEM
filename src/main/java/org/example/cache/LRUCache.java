package org.example.cache;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache<K, V> {

    static class Node<K, V> {
        /* Since Node doesn't need to access any members of LRUCache, it’s best practice to make it static
           to avoid extra references, memory issues, and keep the design clean. */
        K key;
        V value;
        long expiryTime;
        Node<K, V> prev, next;

        Node(K key, V value, long ttlInMillis) {
            this.key = key;
            this.value = value;
            this.expiryTime = (ttlInMillis <= 0) ? Long.MAX_VALUE : System.currentTimeMillis() + ttlInMillis;
            /* System.currentTimeMillis() returns the current time in milliseconds since the Unix epoch.
               The Unix Epoch is: 00:00:00 UTC on January 1, 1970.
               System.currentTimeMillis() gives you the number of milliseconds that have passed from Jan 1, 1970 till now (current time). */
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;
    // final int capacity: the number of allowed entries in the cache can’t be changed after construction.

    private final Map<K, Node<K, V>> cache;
    // final Map<K, Node<K, V>> cache: the reference to the map is fixed; however, the contents of the map can still change.

    private final Node<K, V> head, tail;
    /* final Node head, tail: once the head and tail nodes are created, you can modify their fields (like next, prev),
       but you cannot reassign head or tail to another object. */

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    /* ScheduledExecutorService: Schedules tasks to run periodically or after a delay.
       Executors.newSingleThreadScheduledExecutor(): Creates a thread pool with one thread to run tasks.
       private: Makes the cleaner variable accessible only within the LRUCache class.
       final: Ensures cleaner is assigned once and can't be reassigned later. */

    private final ReentrantLock lock = new ReentrantLock();
    // Lock for making the cache thread-safe

    public LRUCache(int capacity) {
        this.capacity = capacity;
        cache = new HashMap<>();
        head = new Node<>(null, null, 0);
        tail = new Node<>(null, null, 0);
        head.next = tail;
        tail.prev = head;

        startCleaner(); // 🧹 Background thread for auto-cleanup
    }

    private void startCleaner() { //startCleaner(): Starts a background task to clean up expired entries from the cache periodically.
        //cleaner: This is a ScheduledExecutorService (a background thread pool) initialized as: Executors.newSingleThreadScheduledExecutor()
        cleaner.scheduleAtFixedRate(() -> {/*scheduleAtFixedRate: Schedules a task to run at fixed intervals, regardless of how long the task takes.
                                            () -> { ... }: This is a lambda expression, shorthand for defining a Runnable (the task to run). It’s equivalent to:
                                            It’s equivalent to:new Runnable() {
                                                                public void run() {
                                                                    // code here
                                                                }
                                                            }*/
            long now = System.currentTimeMillis();

            List<K> expiredKeys = new ArrayList<>();
            lock.lock(); // Lock during cleanup, lock: This is a ReentrantLock, which is used for mutual exclusion.
            try {
                for (Map.Entry<K, Node<K, V>> entry : cache.entrySet()) {
                    Node<K, V> node = entry.getValue();
                    if (node.isExpired()) {
                        expiredKeys.add(entry.getKey());
                    }
                }

                for (K key : expiredKeys) {
                    Node<K, V> node = cache.get(key);
                    if (node != null) {
                        removeNode(node);
                        cache.remove(key);
                    }
                }
            } finally {/*finally: A finally block always executes, whether or not an exception occurs.
                        Ensures lock.unlock() is called, even if an error occurs inside the try block.
                        Prevents deadlocks — without this, the lock might never get released, freezing other threads.*/

                lock.unlock();
            }
        }, 1, 1, TimeUnit.SECONDS);  // runs every second
    }

    private void addNode(Node<K, V> node) {
        Node<K, V> temp = head.next;
        head.next = node;
        node.prev = head;
        node.next = temp;
        temp.prev = node;
    }

     void removeNode(Node<K, V> node) {
        Node<K, V> prevNode = node.prev;
        Node<K, V> nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    void removeNodeByKey(K key) {//This is a helper function which is used for AOFLogger
        Node<K, V> node = cache.get(key);
        if (node != null) {
            removeNode(node);
            cache.remove(key);
            AOFLogger.getInstance().logRemovedCommand(key); // ✅ Log this removal
        }
    }

    public V getKey(K key) {
        lock.lock();
        try {
            if (!cache.containsKey(key)) return null;

            Node<K, V> node = cache.get(key);

            // 🔥 Lazy Expiry Check
            if (node.isExpired()) {
                removeNode(node);
                cache.remove(key);  // ✅ Clean up
                return null;
            }

            // 🔁 Move to front (most recently used)
            removeNode(node);
            addNode(node);

            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value, long ttlInMillis) {
        lock.lock();
        try {
            putInternal(key, value, ttlInMillis);// 👈 Actual cache logic

            // ✅ Only log during regular use
            AOFLogger.getInstance().logPutCommand(key, value, ttlInMillis);// 👈 Logging

        } finally {
            lock.unlock();
        }
    }

    //The actual logic that updates the in-memory cache without writing anything to disk.
     void putInternal(K key, V value, long ttlInMillis) {
        // If key exists (expired or not), remove the old entry.
        // We don't need to check isExpired() here because either way, we remove and replace it.
        if (cache.containsKey(key)) {
            Node<K, V> existingNode = cache.get(key);
            removeNode(existingNode);
            cache.remove(key);
        }

        // Evict least recently used entry if capacity is full
        if (cache.size() == capacity) {
            cache.remove(tail.prev.key);
            removeNode(tail.prev);
        }

        // Insert new node with updated TTL
        Node<K, V> newNode = new Node<>(key, value, ttlInMillis);
        addNode(newNode);
        cache.put(key, newNode);
    }
}
