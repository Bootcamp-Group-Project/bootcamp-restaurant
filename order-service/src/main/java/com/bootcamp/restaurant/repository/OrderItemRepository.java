package com.bootcamp.restaurant.repository;

import com.bootcamp.restaurant.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderEntity_OrderId(Long orderId);
}
