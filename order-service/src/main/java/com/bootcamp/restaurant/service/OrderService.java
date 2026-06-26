package com.bootcamp.restaurant.service;

import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.model.OrderCreateReq;

import java.util.List;

public interface OrderService {
    List<OrderResp> getAllOrders();
    List<OrderResp> getOrdersByUserId(Long userId);
    OrderResp getOrderById(Long orderId);
    OrderResp createOrder(OrderCreateReq req);
    OrderResp updateOrderStatus(Long orderId, String status);
    List<OrderItemResp> getOrderItems(Long orderId);
}
