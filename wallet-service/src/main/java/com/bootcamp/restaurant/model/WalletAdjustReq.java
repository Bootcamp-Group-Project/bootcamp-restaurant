package com.bootcamp.restaurant.model;

import java.math.BigDecimal;

public record WalletAdjustReq(
        Long adminId,
        BigDecimal amount,
        String description,
        String idempotencyKey
) {}
