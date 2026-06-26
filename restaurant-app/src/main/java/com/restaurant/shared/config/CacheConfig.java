package com.restaurant.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            build("menus-today",  3, 10),
            build("dishes-today", 3, 10),
            build("drinks-today", 3, 10),
            build("menu-by-id",  10, 200),
            build("dish-by-id",  10, 200),
            build("drink-by-id", 10, 200)
        ));
        return manager;
    }

    private CaffeineCache build(String name, long ttlMinutes, long maxSize) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
