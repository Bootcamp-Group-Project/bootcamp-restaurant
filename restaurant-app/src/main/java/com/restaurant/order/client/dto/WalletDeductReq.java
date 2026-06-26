package com.restaurant.order.client.dto;

import java.math.BigDecimal;

public record WalletDeductReq(Long adminId, BigDecimal amount,
                               String description, String idempotencyKey) {}
