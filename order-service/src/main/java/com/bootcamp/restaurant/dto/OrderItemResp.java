package com.bootcamp.restaurant.dto;

import com.bootcamp.restaurant.enums.PickupStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResp {
    private Long itemId;
    private Long orderId;
    private Long menuId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private PickupStatus pickupStatus;
    private String specialNote;
}
