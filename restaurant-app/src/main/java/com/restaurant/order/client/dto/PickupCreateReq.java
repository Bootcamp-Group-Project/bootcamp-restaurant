package com.restaurant.order.client.dto;

import java.time.Instant;

public record PickupCreateReq(Long itemId, Instant expectedTime, String method) {}
