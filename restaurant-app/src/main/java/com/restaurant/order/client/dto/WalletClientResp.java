package com.restaurant.order.client.dto;

import java.math.BigDecimal;

public record WalletClientResp(Long walletId, Long userId, BigDecimal balance) {}
