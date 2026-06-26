package com.restaurant.pickup.dto;

public record PickupStatusResp(
        String code,
        String status,       // READY | PENDING | NOT_FOUND
        String expectedTime  // HH:mm HKT, null if unknown
) {}
