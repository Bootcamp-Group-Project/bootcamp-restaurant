package com.restaurant.wallet.dto;

import com.restaurant.wallet.enums.TopupStatus;

import java.math.BigDecimal;

public record TopupVerifyResp(TopupStatus status, BigDecimal walletBalance, String message) {}
