package com.bootcamp.restaurant.dto;

import com.bootcamp.restaurant.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResp {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmt;
    private Integer totalQty;
    private BigDecimal depositAmt;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
}
