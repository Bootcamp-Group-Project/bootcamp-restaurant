package com.bootcamp.restaurant.client;

import java.math.BigDecimal;

public record WalletResp(
        Long walletId,
        Long userId,
        BigDecimal balance
) {}
