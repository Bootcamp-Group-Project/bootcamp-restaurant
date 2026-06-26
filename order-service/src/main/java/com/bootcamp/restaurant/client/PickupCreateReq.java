package com.bootcamp.restaurant.client;

import java.time.Instant;

public record PickupCreateReq(Long itemId, Instant expectedTime, String method) {}
