package com.bootcamp.restaurant.controller;

import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.model.OrderCreateReq;

import java.util.List;

public interface OrderOperation {
    List<OrderResp> getAllOrders();
    List<OrderResp> getOrdersByUser(Long userId);
    OrderResp getOrderById(Long orderId);
    OrderResp createOrder(OrderCreateReq req);
    OrderResp updateOrderStatus(Long orderId, String status);
    List<OrderItemResp> getOrderItems(Long orderId);
}
