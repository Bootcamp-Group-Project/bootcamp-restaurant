package com.restaurant.pickup.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "meal_pickups")
public class MealPickup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pickup_id")
    private Long pickupId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "expected_time")
    private Instant expectedTime;

    @Column(name = "actual_time")
    private Instant actualTime;

    @Column(name = "admin_notified", nullable = false)
    private Boolean adminNotified = false;
}
