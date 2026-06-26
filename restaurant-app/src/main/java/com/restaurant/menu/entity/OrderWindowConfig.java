package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_window_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderWindowConfig {

    @Id
    private Long id;

    @Column(name = "slot1_start", length = 5, nullable = false)
    private String slot1Start;

    @Column(name = "slot1_end", length = 5, nullable = false)
    private String slot1End;

    @Column(name = "slot2_start", length = 5)
    private String slot2Start;

    @Column(name = "slot2_end", length = 5)
    private String slot2End;

    @Column(name = "zone_id", length = 50, nullable = false)
    private String zoneId;

    @Column(nullable = false)
    private boolean enforced;
}
