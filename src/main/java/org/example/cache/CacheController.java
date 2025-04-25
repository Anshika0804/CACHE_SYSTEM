package org.example.cache;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService service;

    public CacheController(CacheService service) {
        this.service = service;
    }

    @GetMapping("/{key}")
    public int get(@PathVariable int key) {
        return service.get(key);
    }

    @PostMapping("/{key}")
    public String put(
            @PathVariable int key,
            @RequestParam int value,
            @RequestParam(required = false, defaultValue = "0") long ttl) {

        service.put(key, value, ttl);
        return "OK";
    }
}
