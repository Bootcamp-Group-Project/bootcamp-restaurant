package com.bootcamp.restaurant.client;

import com.bootcamp.restaurant.exception.PickupServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PickupClient {

    private final RestClient pickupRestClient;

    public PickupClient(RestClient pickupRestClient) {
        this.pickupRestClient = pickupRestClient;
    }

    public void createPickup(PickupCreateReq req) {
        try {
            pickupRestClient.post()
                    .uri("/api/pickups/create")
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new PickupServiceException("Failed to create pickup record: " + e.getMessage());
        }
    }
}
