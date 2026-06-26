package com.bootcamp.restaurant.entity;

import com.bootcamp.restaurant.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // FK to users table (owned by System 1)
    @Column(name = "user_id")
    private Long userId;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmt;

    private Integer totalQty;

    // 50 when totalQty >= 4, else 0
    @Column(precision = 10, scale = 2)
    private BigDecimal depositAmt;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime createdAt;
}
