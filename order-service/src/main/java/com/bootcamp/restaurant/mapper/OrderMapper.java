package com.bootcamp.restaurant.mapper;

import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.entity.OrderEntity;
import com.bootcamp.restaurant.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResp map(OrderEntity entity) {
        return OrderResp.builder()
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .totalAmt(entity.getTotalAmt())
                .totalQty(entity.getTotalQty())
                .depositAmt(entity.getDepositAmt())
                .orderStatus(entity.getOrderStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public OrderItemResp map(OrderItemEntity entity) {
        return OrderItemResp.builder()
                .itemId(entity.getItemId())
                .orderId(entity.getOrderEntity().getOrderId())
                .menuId(entity.getMenuId())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .pickupStatus(entity.getPickupStatus())
                .specialNote(entity.getSpecialNote())
                .build();
    }
}
