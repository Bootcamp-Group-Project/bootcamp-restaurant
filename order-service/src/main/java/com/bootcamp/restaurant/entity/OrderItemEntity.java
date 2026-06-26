package com.bootcamp.restaurant.entity;

import com.bootcamp.restaurant.enums.PickupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

    // FK to menus table (owned by System 2)
    @Column(name = "menu_id")
    private Long menuId;

    private Integer quantity;

    // Unit price snapshot at order time (sourced from System 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    private PickupStatus pickupStatus;

    @Column(length = 200)
    private String specialNote;
}
