package com.restaurant.menu.controller;

import com.restaurant.menu.dto.OrderWindowConfigReq;
import com.restaurant.menu.dto.OrderWindowConfigResp;
import com.restaurant.menu.service.OrderWindowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu/order-window")
@RequiredArgsConstructor
public class OrderWindowController {

    private final OrderWindowService orderWindowService;

    @GetMapping
    public ResponseEntity<OrderWindowConfigResp> getConfig() {
        return ResponseEntity.ok(orderWindowService.getConfig());
    }

    @PutMapping
    public ResponseEntity<OrderWindowConfigResp> updateConfig(
            @Valid @RequestBody OrderWindowConfigReq req) {
        return ResponseEntity.ok(orderWindowService.updateConfig(req));
    }
}
