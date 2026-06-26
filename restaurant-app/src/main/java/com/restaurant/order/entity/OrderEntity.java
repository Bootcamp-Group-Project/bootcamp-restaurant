package com.restaurant.order.entity;

import com.restaurant.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@AllArgsConstructor @NoArgsConstructor @Getter @Setter @Builder
public class OrderEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmt;

    private Integer totalQty;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositAmt;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime createdAt;

    @Column(length = 20)
    private String phone;
}
