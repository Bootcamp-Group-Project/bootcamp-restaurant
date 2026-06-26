package com.restaurant.order.service.impl;

import com.restaurant.order.client.PickupClient;
import com.restaurant.order.client.dto.PickupCreateReq;
import com.restaurant.order.dto.OrderCreateReq;
import com.restaurant.order.dto.OrderItemResp;
import com.restaurant.order.dto.OrderResp;
import com.restaurant.order.dto.OrderWithItemsResp;
import com.restaurant.order.entity.OrderEntity;
import com.restaurant.order.entity.OrderItemEntity;
import com.restaurant.order.enums.OrderStatus;
import com.restaurant.order.enums.PickupStatus;
import com.restaurant.order.mapper.OrderMapper;
import com.restaurant.order.repository.OrderItemRepository;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.service.OrderService;
import com.restaurant.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final PickupClient pickupClient;

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

        OrderEntity order = OrderEntity.builder()
                .userId(req.userId())
                .phone(req.phone())
                .orderStatus(OrderStatus.PENDING_PAY)
                .createdAt(LocalDateTime.now())
                .build();
        OrderEntity savedOrder = orderRepository.save(order);

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
                    .menuType(itemReq.menuType())
                    .pickupStatus(PickupStatus.PENDING)
                    .specialNote(itemReq.specialNote())
                    .build());
        }

        BigDecimal depositAmt = totalQty >= 4 ? BigDecimal.valueOf(50) : BigDecimal.ZERO;
        savedOrder.setTotalAmt(totalAmt);
        savedOrder.setTotalQty(totalQty);
        savedOrder.setDepositAmt(depositAmt);
        orderRepository.save(savedOrder);
        orderItemRepository.saveAll(items);

        return orderMapper.map(savedOrder);
    }

    @Override
    @Transactional
    public OrderResp updateStatus(Long orderId, String status) {
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
            String phone = order.getPhone();
            String phoneLast4 = (phone != null && phone.length() >= 4)
                    ? phone.substring(phone.length() - 4)
                    : String.format("%04d", order.getUserId() % 10000);
            String method = phoneLast4 + "-" + String.format("%03d", orderId % 1000);
            for (OrderItemEntity item : orderItems) {
                pickupClient.createPickup(new PickupCreateReq(item.getItemId(), expectedTime, method));
            }
        }

        return resp;
    }

    public List<OrderItemResp> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderEntity_OrderId(orderId).stream()
                .map(orderMapper::map).collect(Collectors.toList());
    }

    @Override
    public List<OrderWithItemsResp> getTodayOrdersWithItems() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<OrderEntity> orders = orderRepository.findByCreatedAtBetween(start, end);
        if (orders.isEmpty()) return List.of();

        List<Long> orderIds = orders.stream().map(OrderEntity::getOrderId).toList();
        Map<Long, List<OrderItemResp>> itemsByOrder = orderItemRepository
                .findByOrderEntity_OrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getOrderEntity().getOrderId(),
                        Collectors.mapping(orderMapper::map, Collectors.toList())));

        return orders.stream()
                .map(o -> new OrderWithItemsResp(
                        o.getOrderId(), o.getUserId(), o.getTotalAmt(), o.getTotalQty(),
                        o.getDepositAmt(),
                        o.getOrderStatus() != null ? o.getOrderStatus().name() : null,
                        o.getCreatedAt(),
                        o.getPhone(),
                        itemsByOrder.getOrDefault(o.getOrderId(), List.of())))
                .collect(Collectors.toList());
    }
}
