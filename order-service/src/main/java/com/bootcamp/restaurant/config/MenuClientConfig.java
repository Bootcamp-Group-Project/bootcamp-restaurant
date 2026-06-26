package com.bootcamp.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MenuClientConfig {

    @Bean
    public RestClient menuRestClient(
            @Value("${menu.service.url}") String url,
            @Value("${menu.service.username}") String username,
            @Value("${menu.service.password}") String password) {
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }
}
