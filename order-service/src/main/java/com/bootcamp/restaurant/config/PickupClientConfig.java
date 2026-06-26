package com.bootcamp.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PickupClientConfig {

    @Bean
    public RestClient pickupRestClient(@Value("${pickup.service.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .build();
    }
}
