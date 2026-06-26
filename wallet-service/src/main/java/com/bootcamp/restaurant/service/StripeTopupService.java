package com.bootcamp.restaurant.service;

import com.bootcamp.restaurant.dto.TopupCreateResp;
import com.bootcamp.restaurant.dto.TopupVerifyResp;
import com.stripe.exception.StripeException;

import java.math.BigDecimal;

public interface StripeTopupService {
    TopupCreateResp createTopupSession(Long walletId, BigDecimal amount) throws StripeException;
    TopupVerifyResp verifyTopup(Long walletId, String sessionId) throws StripeException;
}
