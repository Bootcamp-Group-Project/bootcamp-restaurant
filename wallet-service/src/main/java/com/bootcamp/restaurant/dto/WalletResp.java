package com.bootcamp.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WalletResp {
    private Long walletId;
    private Long userId;
    private BigDecimal balance;
}
