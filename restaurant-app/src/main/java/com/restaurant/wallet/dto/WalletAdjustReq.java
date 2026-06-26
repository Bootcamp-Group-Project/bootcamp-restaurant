package com.restaurant.wallet.dto;

import java.math.BigDecimal;

public record WalletAdjustReq(Long adminId, BigDecimal amount,
                               String description, String idempotencyKey) {}
