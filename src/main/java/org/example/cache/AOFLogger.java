package org.example.cache;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class AOFLogger {
    private static final String FILE_PATH = System.getProperty("aof.file", "append-only.aof");
    // Single, unchangeable file path used only within this class

    private final ReentrantLock lock = new ReentrantLock();
    // Not static because Singleton has only one instance, so one lock is enough

    private final List<String> buffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static volatile AOFLogger instance = null;
    // Singleton instance (created lazily when first needed)
    // Not final because it's assigned lazily at runtime, not at declaration

    private AOFLogger() {
        //Flush every 3 seconds
        scheduler.scheduleAtFixedRate(this::flushBufferToDisk, 3, 3, TimeUnit.SECONDS);
    }

    public static AOFLogger getInstance() {
        if (instance == null) {
            synchronized (AOFLogger.class) {
                if (instance == null) {
                    instance = new AOFLogger();
                }
            }
        }
        return instance;
    }

    public void logPutCommand(Object key, Object value, long ttl){
        long expiryTimeStamp = (ttl > 0) ? System.currentTimeMillis() + ttl : -1;
        String command = (ttl > 0)
                ? ("PUT " + key + " " + value + " TTL " + expiryTimeStamp)
                : ("PUT " + key + " " + value);

        logCommand(command);
    }

    public void logRemovedCommand(Object key){
        logCommand("REMOVE " + key);
    }
    public void logCommand(String command) {
        lock.lock();
        try {
            buffer.add(command);
        } finally {
            lock.unlock();
        }
    }

    private void flushBufferToDisk() {
        lock.lock();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            for(String command : buffer){
                out.println(command);
            }
            buffer.clear(); //Important: clear buffer after writing...
        } catch (IOException e) {
            System.err.println("AOF flush failed: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void shutdown(){
        scheduler.shutdown();
        flushBufferToDisk();; //Final Flush
    }
    public <K, V> void loadFromAOF(LRUCache<K, V> cache) {
        // Replays the AOF commands and restores cache state
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                String cmd = parts[0];

                @SuppressWarnings("unchecked")
                K key = (K) Integer.valueOf(parts[1]); // ✅ safely convert string to Integer

                switch (cmd) {
                    case "PUT" -> {
                        @SuppressWarnings("unchecked")
                        V value = (V) parts[2]; // assuming value is a String

                        long remainingTTL = -1;
                        if (parts.length == 5 && parts[3].equals("TTL")) {
                            long expiryTimestamp = Long.parseLong(parts[4]);
                            long currentTime = System.currentTimeMillis();
                            remainingTTL = expiryTimestamp - currentTime;
                        }

                        if (remainingTTL == -1 || remainingTTL > 0) {
                            cache.putInternal(key, value, remainingTTL); // Use remaining TTL
                        }
                        // else, the entry has already expired — don't insert
                    }
                    case "REMOVE" -> cache.removeNodeByKey(key);

                    default -> System.err.println("Unknown command in AOF: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("AOF file not found — assuming fresh start.");
        } catch (IOException e) {
            System.err.println("AOF load failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while parsing AOF: " + e.getMessage());
        }
    }
}
