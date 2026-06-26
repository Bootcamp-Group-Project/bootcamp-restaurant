package com.bootcamp.restaurant.dto;

import com.bootcamp.restaurant.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WalletTransactionResp {
    private Long transactionId;
    private Long walletId;
    private Long adminId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
}
