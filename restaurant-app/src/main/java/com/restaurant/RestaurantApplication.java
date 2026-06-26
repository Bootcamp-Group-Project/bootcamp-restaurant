package com.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Modular Monolith — 1 JAR, 5 modules (login / menu / order / wallet / pickup).
 *
 * Cross-module calls go through OpenFeign → http://127.0.0.1:8080
 * so each module stays decoupled and can be extracted to a true microservice
 * by simply changing the Feign URL.
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableScheduling
public class RestaurantApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class, args);
    }
}
