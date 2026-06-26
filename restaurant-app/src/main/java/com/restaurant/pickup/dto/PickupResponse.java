package com.restaurant.pickup.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PickupResponse {
    private Long pickupId;
    private Long itemId;
    private Long adminId;
    private String method;
    private Instant expectedTime;
    private Instant actualTime;
    private Boolean adminNotified;
    private String message;
}
