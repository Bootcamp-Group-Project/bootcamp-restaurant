package com.bootcamp.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    // FK to users table (owned by System 1)
    @Column(name = "user_id")
    private Long userId;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;
}
