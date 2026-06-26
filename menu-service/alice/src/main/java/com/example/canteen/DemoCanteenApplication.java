package com.example.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoCanteenApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoCanteenApplication.class, args);
    }
}
