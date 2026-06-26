package com.restaurant.wallet.service;

import com.restaurant.wallet.dto.TopupCreateResp;
import com.restaurant.wallet.dto.TopupVerifyResp;
import com.stripe.exception.StripeException;

import java.math.BigDecimal;

public interface StripeTopupService {
    TopupCreateResp createTopupSession(Long walletId, BigDecimal amount) throws StripeException;
    TopupVerifyResp verifyTopup(Long walletId, String sessionId) throws StripeException;
}
