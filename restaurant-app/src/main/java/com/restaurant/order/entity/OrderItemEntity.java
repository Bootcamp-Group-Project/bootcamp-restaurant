package com.restaurant.order.entity;

import com.restaurant.order.enums.PickupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@AllArgsConstructor @NoArgsConstructor @Getter @Setter @Builder
public class OrderItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

    @Column(name = "menu_id")
    private Long menuId;

    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    private PickupStatus pickupStatus;

    @Column(length = 200)
    private String specialNote;

    @Column(length = 10)
    private String menuType;
}
