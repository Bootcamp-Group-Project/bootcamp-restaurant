package com.bootcamp.restaurant.client;

import java.math.BigDecimal;

/**
 * Subset of Alice's DishResponse fields that order-service needs.
 * status is "ON_LIST" or "SOLD_OUT".
 */
public record DishInfo(
        Long id,
        String name,
        BigDecimal price,
        Integer balance,
        String status
) {}
