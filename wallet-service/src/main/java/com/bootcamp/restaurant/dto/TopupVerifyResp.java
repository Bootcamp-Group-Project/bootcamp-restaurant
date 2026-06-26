package com.bootcamp.restaurant.dto;

import com.bootcamp.restaurant.enums.TopupStatus;

import java.math.BigDecimal;

public record TopupVerifyResp(TopupStatus status, BigDecimal walletBalance, String message) {}
