package com.restaurant.order.mapper;

import com.restaurant.order.dto.OrderItemResp;
import com.restaurant.order.dto.OrderResp;
import com.restaurant.order.entity.OrderEntity;
import com.restaurant.order.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResp map(OrderEntity entity) {
        return new OrderResp(
                entity.getOrderId(),
                entity.getUserId(),
                entity.getTotalAmt(),
                entity.getTotalQty(),
                entity.getDepositAmt(),
                entity.getOrderStatus() != null ? entity.getOrderStatus().name() : null,
                entity.getCreatedAt(),
                entity.getPhone());
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
                .menuType(entity.getMenuType())
                .build();
    }
}
