package com.restaurant.order.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionClientResp(Long transactionId, Long walletId,
                                           BigDecimal amount, String type,
                                           String description, LocalDateTime createdAt) {}
