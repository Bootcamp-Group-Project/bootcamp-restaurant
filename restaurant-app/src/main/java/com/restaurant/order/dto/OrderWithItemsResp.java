package com.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderWithItemsResp(
        Long orderId,
        Long userId,
        BigDecimal totalAmt,
        Integer totalQty,
        BigDecimal depositAmt,
        String orderStatus,
        LocalDateTime createdAt,
        String phone,
        List<OrderItemResp> items
) {}
