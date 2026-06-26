package com.bootcamp.restaurant.service;

import com.bootcamp.restaurant.client.PickupClient;
import com.bootcamp.restaurant.client.PickupCreateReq;
import com.bootcamp.restaurant.dto.OrderItemResp;
import com.bootcamp.restaurant.dto.OrderResp;
import com.bootcamp.restaurant.entity.OrderEntity;
import com.bootcamp.restaurant.entity.OrderItemEntity;
import com.bootcamp.restaurant.enums.OrderStatus;
import com.bootcamp.restaurant.exception.ResourceNotFoundException;
import com.bootcamp.restaurant.mapper.OrderMapper;
import com.bootcamp.restaurant.model.OrderCreateReq;
import com.bootcamp.restaurant.repository.OrderItemRepository;
import com.bootcamp.restaurant.repository.OrderRepository;
import com.bootcamp.restaurant.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private PickupClient pickupClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ── helpers ────────────────────────────────────────────
    private OrderCreateReq.OrderItemReq item(long menuId, int qty, BigDecimal price, String note) {
        return new OrderCreateReq.OrderItemReq(menuId, qty, price, note);
    }

    // ════════════════════════════════════════════════
    //  getOrdersByUserId
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("getOrdersByUserId: 回傳該使用者所有訂單")
    void getOrdersByUserId_shouldReturnList() {
        OrderEntity o = OrderEntity.builder().orderId(1L).userId(10L).build();
        OrderResp resp = OrderResp.builder().orderId(1L).userId(10L).build();

        when(orderRepository.findByUserId(10L)).thenReturn(List.of(o));
        when(orderMapper.map(o)).thenReturn(resp);

        List<OrderResp> result = orderService.getOrdersByUserId(10L);

        assertThat(result).hasSize(1).containsExactly(resp);
    }

    // ════════════════════════════════════════════════
    //  getOrderById
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("getOrderById: 訂單存在 → 回傳 OrderResp")
    void getOrderById_whenFound_shouldReturnOrderResp() {
        OrderEntity o = OrderEntity.builder().orderId(1L).build();
        OrderResp resp = OrderResp.builder().orderId(1L).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        when(orderMapper.map(o)).thenReturn(resp);

        assertThat(orderService.getOrderById(1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("getOrderById: 訂單不存在 → 拋出 ResourceNotFoundException")
    void getOrderById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ════════════════════════════════════════════════
    //  createOrder — 正常邏輯
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("createOrder: totalQty < 4 → depositAmt = 0")
    void createOrder_whenTotalQtyLessThan4_depositShouldBeZero() {
        // qty = 1 + 2 = 3
        OrderCreateReq req = new OrderCreateReq(10L, List.of(
                item(1L, 1, BigDecimal.valueOf(60), null),
                item(2L, 2, BigDecimal.valueOf(40), null)
        ));
        OrderEntity saved = OrderEntity.builder().orderId(1L).userId(10L).build();

        when(orderRepository.save(any())).thenReturn(saved);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.map(saved)).thenReturn(OrderResp.builder().orderId(1L).build());

        orderService.createOrder(req);

        assertThat(saved.getDepositAmt()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createOrder: totalQty = 4 → depositAmt = 50")
    void createOrder_whenTotalQtyEquals4_depositShouldBe50() {
        // qty = 2 + 2 = 4
        OrderCreateReq req = new OrderCreateReq(10L, List.of(
                item(1L, 2, BigDecimal.valueOf(60), null),
                item(2L, 2, BigDecimal.valueOf(40), null)
        ));
        OrderEntity saved = OrderEntity.builder().orderId(1L).userId(10L).build();

        when(orderRepository.save(any())).thenReturn(saved);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.map(saved)).thenReturn(OrderResp.builder().orderId(1L).build());

        orderService.createOrder(req);

        assertThat(saved.getDepositAmt()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("createOrder: totalAmt = Σ(unitPrice × quantity)")
    void createOrder_totalAmtShouldBeCalculatedCorrectly() {
        // 2 × 60 + 1 × 80 = 200
        OrderCreateReq req = new OrderCreateReq(10L, List.of(
                item(1L, 2, BigDecimal.valueOf(60), null),
                item(2L, 1, BigDecimal.valueOf(80), null)
        ));
        OrderEntity saved = OrderEntity.builder().orderId(1L).userId(10L).build();

        when(orderRepository.save(any())).thenReturn(saved);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.map(saved)).thenReturn(OrderResp.builder().orderId(1L).build());

        orderService.createOrder(req);

        assertThat(saved.getTotalAmt()).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("createOrder: 初始狀態必須為 PENDING_PAY")
    void createOrder_initialStatusShouldBePendingPay() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(
                item(1L, 1, BigDecimal.valueOf(50), null)
        ));
        OrderEntity saved = OrderEntity.builder().orderId(1L).userId(10L).build();

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity arg = inv.getArgument(0);
            saved.setOrderStatus(arg.getOrderStatus());
            return saved;
        });
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.map(any(OrderEntity.class))).thenReturn(OrderResp.builder().build());

        orderService.createOrder(req);

        assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAY);
    }

    // ════════════════════════════════════════════════
    //  createOrder — 欄位驗證
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("createOrder: items 為空清單 → 拋出 IllegalArgumentException")
    void createOrder_whenItemsIsEmpty_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of());

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("items");
    }

    @Test
    @DisplayName("createOrder: item.quantity = 0 → 拋出 IllegalArgumentException")
    void createOrder_whenItemQuantityIsZero_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(item(1L, 0, null, null)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    @DisplayName("createOrder: item.quantity < 0 → 拋出 IllegalArgumentException")
    void createOrder_whenItemQuantityIsNegative_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(item(1L, -1, null, null)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    @DisplayName("createOrder: item.unitPrice = null → 拋出 IllegalArgumentException")
    void createOrder_whenUnitPriceIsNull_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(item(1L, 1, null, null)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unitPrice");
    }

    @Test
    @DisplayName("createOrder: item.unitPrice < 0 → 拋出 IllegalArgumentException")
    void createOrder_whenUnitPriceIsNegative_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(item(1L, 1, BigDecimal.valueOf(-1), null)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unitPrice");
    }

    @Test
    @DisplayName("createOrder: specialNote 超過 200 字 → 拋出 IllegalArgumentException")
    void createOrder_whenSpecialNoteExceeds200Chars_shouldThrow() {
        OrderCreateReq req = new OrderCreateReq(10L, List.of(
                item(1L, 1, BigDecimal.TEN, "A".repeat(201))
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("specialNote");
    }

    // ════════════════════════════════════════════════
    //  updateOrderStatus
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("updateOrderStatus: 合法狀態字串 → 更新成功")
    void updateOrderStatus_withValidStatus_shouldUpdate() {
        OrderEntity order = OrderEntity.builder().orderId(1L).userId(1L)
                .orderStatus(OrderStatus.PENDING_PAY).build();
        OrderResp resp = OrderResp.builder().orderId(1L)
                .orderStatus(OrderStatus.DEPOSIT_PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.map(order)).thenReturn(resp);

        OrderResp result = orderService.updateOrderStatus(1L, "DEPOSIT_PAID");

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.DEPOSIT_PAID);
    }

    @Test
    @DisplayName("updateOrderStatus: 非法狀態字串 → 拋出 IllegalArgumentException")
    void updateOrderStatus_withInvalidStatus_shouldThrow() {
        OrderEntity order = OrderEntity.builder().orderId(1L)
                .orderStatus(OrderStatus.PENDING_PAY).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID_STATUS");
    }

    @Test
    @DisplayName("updateOrderStatus: 訂單不存在 → 拋出 ResourceNotFoundException")
    void updateOrderStatus_whenNotFound_shouldThrow() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, "COMPLETE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateOrderStatus: 狀態更新為 DEPOSIT_PAID → 為每個品項呼叫 pickupClient.createPickup")
    void updateOrderStatus_toDepositPaid_shouldCallPickupForEachItem() {
        OrderEntity order = OrderEntity.builder().orderId(1L).userId(1L).orderStatus(OrderStatus.PENDING_PAY).build();
        OrderItemEntity item1 = OrderItemEntity.builder().itemId(10L).orderEntity(order).build();
        OrderItemEntity item2 = OrderItemEntity.builder().itemId(11L).orderEntity(order).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderItemRepository.findByOrderEntity_OrderId(1L)).thenReturn(List.of(item1, item2));
        when(orderMapper.map(order)).thenReturn(OrderResp.builder().orderId(1L).build());

        orderService.updateOrderStatus(1L, "DEPOSIT_PAID");

        verify(pickupClient, times(2)).createPickup(any(PickupCreateReq.class));
    }

    @Test
    @DisplayName("updateOrderStatus: 狀態更新為 COMPLETE → 不呼叫 pickupClient")
    void updateOrderStatus_toComplete_shouldNotCallPickup() {
        OrderEntity order = OrderEntity.builder().orderId(1L).orderStatus(OrderStatus.FULLY_PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.map(order)).thenReturn(OrderResp.builder().orderId(1L).build());

        orderService.updateOrderStatus(1L, "COMPLETE");

        verify(pickupClient, times(0)).createPickup(any());
    }

    // ════════════════════════════════════════════════
    //  getOrderItems
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("getOrderItems: 回傳該訂單所有品項")
    void getOrderItems_shouldReturnList() {
        OrderEntity order = OrderEntity.builder().orderId(1L).build();
        OrderItemEntity item = OrderItemEntity.builder().itemId(1L).orderEntity(order).build();
        OrderItemResp itemResp = OrderItemResp.builder().itemId(1L).orderId(1L).build();

        when(orderItemRepository.findByOrderEntity_OrderId(1L)).thenReturn(List.of(item));
        when(orderMapper.map(item)).thenReturn(itemResp);

        List<OrderItemResp> result = orderService.getOrderItems(1L);

        assertThat(result).hasSize(1).containsExactly(itemResp);
    }
}
