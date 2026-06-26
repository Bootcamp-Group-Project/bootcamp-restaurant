package com.bootcamp.restaurant.service.impl;

import com.bootcamp.restaurant.client.PickupClient;
import com.bootcamp.restaurant.client.PickupCreateReq;
import com.bootcamp.restaurant.client.WalletClient;
import com.bootcamp.restaurant.client.WalletResp;
import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.entity.OrderEntity;
import com.bootcamp.restaurant.entity.OrderItemEntity;
import com.bootcamp.restaurant.enums.OrderStatus;
import com.bootcamp.restaurant.enums.PickupStatus;
import com.bootcamp.restaurant.exception.ResourceNotFoundException;
import com.bootcamp.restaurant.mapper.OrderMapper;
import com.bootcamp.restaurant.model.OrderCreateReq;
import com.bootcamp.restaurant.repository.OrderItemRepository;
import com.bootcamp.restaurant.repository.OrderRepository;
import com.bootcamp.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderMapper orderMapper;
    @Autowired private PickupClient pickupClient;
    @Autowired private WalletClient walletClient;

    @Override
    public List<OrderResp> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::map).collect(Collectors.toList());
    }

    @Override
    public List<OrderResp> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::map).collect(Collectors.toList());
    }

    @Override
    public OrderResp getOrderById(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return orderMapper.map(order);
    }

    @Override
    @Transactional
    public OrderResp createOrder(OrderCreateReq req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        for (OrderCreateReq.OrderItemReq item : req.items()) {
            if (item.quantity() <= 0)
                throw new IllegalArgumentException("quantity must be positive, got: " + item.quantity());
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("unitPrice must not be negative for menuId: " + item.menuId());
            if (item.specialNote() != null && item.specialNote().length() > 200)
                throw new IllegalArgumentException(
                        "specialNote must not exceed 200 characters, got: " + item.specialNote().length());
        }

        // 1. Save order skeleton
        OrderEntity order = OrderEntity.builder()
                .userId(req.userId())
                .orderStatus(OrderStatus.PENDING_PAY)
                .createdAt(LocalDateTime.now())
                .build();
        OrderEntity savedOrder = orderRepository.save(order);

        // 2. Build items and compute totals
        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal totalAmt = BigDecimal.ZERO;
        int totalQty = 0;

        for (OrderCreateReq.OrderItemReq itemReq : req.items()) {
            totalAmt = totalAmt.add(itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
            totalQty += itemReq.quantity();
            items.add(OrderItemEntity.builder()
                    .orderEntity(savedOrder)
                    .menuId(itemReq.menuId())
                    .quantity(itemReq.quantity())
                    .unitPrice(itemReq.unitPrice())
                    .pickupStatus(PickupStatus.PENDING)
                    .specialNote(itemReq.specialNote())
                    .build());
        }

        BigDecimal depositAmt = totalQty >= 4 ? BigDecimal.valueOf(50) : BigDecimal.ZERO;
        BigDecimal deductAmt = depositAmt.compareTo(BigDecimal.ZERO) > 0 ? depositAmt : totalAmt;

        savedOrder.setTotalAmt(totalAmt);
        savedOrder.setTotalQty(totalQty);
        savedOrder.setDepositAmt(depositAmt);
        orderRepository.save(savedOrder);
        orderItemRepository.saveAll(items);

        // 3. Deduct from wallet via Railway internal network (throws WalletServiceException on failure → triggers rollback)
        if (deductAmt.compareTo(BigDecimal.ZERO) > 0) {
            WalletResp wallet = walletClient.getWalletByUserId(req.userId());
            String idempotencyKey = "order-" + savedOrder.getOrderId() + "-pay";
            walletClient.deduct(wallet.walletId(), deductAmt, req.userId(), idempotencyKey);

            // 4. Update order status after successful deduction
            OrderStatus newStatus = depositAmt.compareTo(BigDecimal.ZERO) > 0
                    ? OrderStatus.DEPOSIT_PAID
                    : OrderStatus.FULLY_PAID;
            savedOrder.setOrderStatus(newStatus);
            orderRepository.save(savedOrder);
        }

        return orderMapper.map(savedOrder);
    }

    @Override
    @Transactional
    public OrderResp updateOrderStatus(Long orderId, String status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        order.setOrderStatus(newStatus);
        OrderResp resp = orderMapper.map(orderRepository.save(order));

        if (newStatus == OrderStatus.DEPOSIT_PAID || newStatus == OrderStatus.FULLY_PAID) {
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderEntity_OrderId(orderId);
            Instant expectedTime = Instant.now().plus(30, ChronoUnit.MINUTES);
            String phoneLast4 = String.format("%04d", order.getUserId() % 10000);
            String method = phoneLast4 + "-" + String.format("%03d", orderId % 1000);
            for (OrderItemEntity item : orderItems) {
                pickupClient.createPickup(new PickupCreateReq(item.getItemId(), expectedTime, method));
            }
        }

        return resp;
    }

    @Override
    public List<OrderItemResp> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderEntity_OrderId(orderId).stream()
                .map(orderMapper::map).collect(Collectors.toList());
    }
}
