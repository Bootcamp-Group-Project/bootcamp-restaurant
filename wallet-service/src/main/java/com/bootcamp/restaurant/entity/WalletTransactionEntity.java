package com.bootcamp.restaurant.entity;

import com.bootcamp.restaurant.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private WalletEntity walletEntity;

    // FK to admin_users table (owned by System 1)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String description;

    // Prevents duplicate transactions on retry
    @Column(unique = true)
    private String idempotencyKey;

    private LocalDateTime createdAt;
}
