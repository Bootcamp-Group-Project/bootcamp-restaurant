package com.restaurant.wallet.dto;

import java.math.BigDecimal;

public record WalletResp(Long walletId, Long userId, BigDecimal balance) {}
