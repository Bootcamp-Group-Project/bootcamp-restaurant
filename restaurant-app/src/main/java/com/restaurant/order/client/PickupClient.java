package com.restaurant.order.client;

import com.restaurant.order.client.dto.PickupCreateReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pickup-client", url = "http://127.0.0.1:8080")
public interface PickupClient {

    @PostMapping("/api/pickups/create")
    void createPickup(@RequestBody PickupCreateReq req);
}
