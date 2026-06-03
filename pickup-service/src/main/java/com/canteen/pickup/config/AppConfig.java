package com.canteen.pickup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 用嚟 HTTP call 其他同學嘅 API
 * 例如 call 同學4嘅 Order API: http://localhost:8084/orders/{id}/items
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
