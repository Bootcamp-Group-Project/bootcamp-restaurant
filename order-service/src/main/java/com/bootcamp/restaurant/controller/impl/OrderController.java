package com.bootcamp.restaurant.controller.impl;

import com.bootcamp.restaurant.controller.OrderOperation;
import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.model.OrderCreateReq;
import com.bootcamp.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController implements OrderOperation {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    @Override
    public List<OrderResp> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/user/{userId}")
    @Override
    public List<OrderResp> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/orders/{orderId}")
    @Override
    public OrderResp getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @PostMapping("/orders")
    @Override
    public OrderResp createOrder(@RequestBody OrderCreateReq req) {
        return orderService.createOrder(req);
    }

    @PutMapping("/orders/{orderId}/status")
    @Override
    public OrderResp updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @GetMapping("/orders/{orderId}/items")
    @Override
    public List<OrderItemResp> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }
}
