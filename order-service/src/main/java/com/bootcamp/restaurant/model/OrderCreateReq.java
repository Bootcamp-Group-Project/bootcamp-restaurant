package com.bootcamp.restaurant.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateReq(
        Long userId,
        List<OrderItemReq> items
) {
    public record OrderItemReq(
            Long menuId,
            Integer quantity,
            BigDecimal unitPrice,
            String specialNote
    ) {}
}
