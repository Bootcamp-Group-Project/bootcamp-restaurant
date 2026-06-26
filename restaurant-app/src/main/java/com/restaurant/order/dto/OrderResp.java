package com.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResp(
        Long orderId,
        Long userId,
        BigDecimal totalAmt,
        Integer totalQty,
        BigDecimal depositAmt,
        String orderStatus,
        LocalDateTime createdAt,
        String phone
) {}
