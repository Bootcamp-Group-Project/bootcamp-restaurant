package com.restaurant.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResp(Long transactionId, Long walletId, Long adminId,
                                     BigDecimal amount, String type,
                                     String description, LocalDateTime createdAt) {}
