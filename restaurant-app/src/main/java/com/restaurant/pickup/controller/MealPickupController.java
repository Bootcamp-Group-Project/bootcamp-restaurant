package com.restaurant.pickup.controller;

import com.restaurant.pickup.dto.PickupCreateRequest;
import com.restaurant.pickup.dto.PickupResponse;
import com.restaurant.pickup.dto.PickupStatusResp;
import com.restaurant.pickup.dto.PickupVerifyRequest;
import com.restaurant.pickup.service.MealPickupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickups")
@RequiredArgsConstructor
public class MealPickupController {

    private final MealPickupService service;

    @PostMapping("/create")
    public ResponseEntity<PickupResponse> create(@Valid @RequestBody PickupCreateRequest req) {
        return ResponseEntity.status(201).body(service.createPickup(req));
    }

    @PostMapping("/verify")
    public ResponseEntity<PickupResponse> verify(@Valid @RequestBody PickupVerifyRequest req) {
        return ResponseEntity.ok(service.verifyPickup(req));
    }

    @GetMapping("/status")
    public ResponseEntity<PickupStatusResp> getStatus(@RequestParam String code) {
        return ResponseEntity.ok(service.getStatusByCode(code));
    }

    @PutMapping("/ready")
    public ResponseEntity<Void> markReady(@RequestParam String code) {
        service.markReadyByCode(code);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<PickupResponse> getByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.getByItemId(itemId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PickupResponse>> getPending() {
        return ResponseEntity.ok(service.getPendingPickups());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<PickupResponse>> getOverdue() {
        return ResponseEntity.ok(service.getOverduePickups());
    }

    @GetMapping("/unnotified")
    public ResponseEntity<List<PickupResponse>> getUnnotified() {
        return ResponseEntity.ok(service.getUnnotified());
    }

    @PatchMapping("/{pickupId}/notify")
    public ResponseEntity<PickupResponse> markNotified(@PathVariable Long pickupId) {
        return ResponseEntity.ok(service.markAdminNotified(pickupId));
    }

    @PostMapping("/{pickupId}/call")
    public ResponseEntity<PickupResponse> call(@PathVariable Long pickupId) {
        return ResponseEntity.ok(service.callPickup(pickupId));
    }
}
