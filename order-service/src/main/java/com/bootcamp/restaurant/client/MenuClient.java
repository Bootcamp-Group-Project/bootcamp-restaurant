package com.bootcamp.restaurant.client;

import com.bootcamp.restaurant.exception.MenuServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class MenuClient {

    private final RestClient menuRestClient;

    public MenuClient(RestClient menuRestClient) {
        this.menuRestClient = menuRestClient;
    }

    public DishInfo getDish(Long dishId) {
        try {
            return menuRestClient.get()
                    .uri("/api/dishes/{id}", dishId)
                    .retrieve()
                    .body(DishInfo.class);
        } catch (RestClientException e) {
            throw new MenuServiceException("Failed to fetch dish " + dishId + ": " + e.getMessage());
        }
    }

    /**
     * Sets the dish balance to newBalance (absolute value — Alice's API design).
     * Caller must compute: currentBalance - orderedQuantity.
     */
    public void deductStock(Long dishId, int newBalance) {
        try {
            menuRestClient.patch()
                    .uri("/api/dishes/{id}/balance", dishId)
                    .body(Map.of("balance", newBalance))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new MenuServiceException("Failed to deduct stock for dish " + dishId + ": " + e.getMessage());
        }
    }
}
