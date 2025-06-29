# 🚀 JCacheX – Java-Based Redis-like Caching System

A high-performance, persistent, and thread-safe caching system built in Java, inspired by Redis. Supports TTL-based expiry, LRU eviction, REST APIs, and AOF-style persistence.

---

## ✅ Features Summary

| Feature Group               | Status   |
|----------------------------|----------|
| Core Cache + TTL + LRU     | ✅ Done  |
| Persistence (AOF, Snapshot)| ✅ Done  |
| REST API + Consistency     | ✅ Done  |
| Write Policies             | ✅ Done  |
| Thread Affinity            | ✅ Done  |

---

## 🧱 Project Structure

```
CACHE-SYSTEM/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/cache/
│   │   │       ├── AOFLogger.java          # Buffered AOF persistence logger
│   │   │       ├── CacheApplication.java   # Spring Boot main app
│   │   │       ├── CacheController.java    # REST endpoints for cache
│   │   │       ├── CacheService.java       # Business logic for cache operations
│   │   │       ├── LRUCache.java           # Core cache (with TTL + LRU)
│   │   │       └── Main.java               # (Optional) standalone runner
│   │   └── resources/
│   ├── test/                               # Unit tests
├── append-only.aof                         # AOF command log for recovery
├── pom.xml                                 # Maven build config
├── .gitignore
└── README.md
```

---

## 🔧 How to Run

### 1. Build the project
```bash
mvn clean install
```

### 2. Run the application
```bash
java -jar target/cache-system.jar
```

> Replace the `.jar` file name with the actual name generated in the `target/` directory.

---

## 🌐 REST API Endpoints

Here are some basic endpoints (Spring Boot-based):

- `POST /cache/set`  
  Set key-value with optional TTL
  ```json
  {
    "key": "user1",
    "value": "Anshika",
    "ttl": 60
  }
  ```

- `GET /cache/get/{key}`  
  Retrieve value by key

- `DELETE /cache/delete/{key}`  
  Delete a key from the cache

---

## 🧠 Internals

- 🔁 **LRU** eviction with `LinkedHashMap`
- ⏲️ **TTL support** with scheduled background cleaner
- 📜 **AOF persistence** using `AOFLogger` class
- 🧵 **Thread safety** ensured using `ReentrantLock`
- ♻️ **Log rotation** using snapshot rewrite technique
- 🧪 Unit testing support in `src/test`

---

## 📌 Future Enhancements

- 📡 Distributed mode (via sharding or clustering)
- 📊 Admin dashboard (React-based)
- 🔐 API authentication & rate limiting
- 📈 In-memory stats (hits, misses, evictions)

---

## 👩‍💻 Author

**Anshika Rai**  
[GitHub](https://github.com/Anshika0804)

---

## 📄 License

This project is for learning/demo purposes and is not licensed for commercial use.

