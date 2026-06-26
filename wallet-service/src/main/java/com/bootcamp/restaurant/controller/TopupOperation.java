package com.bootcamp.restaurant.controller;

import com.bootcamp.restaurant.dto.TopupCreateResp;
import com.bootcamp.restaurant.dto.TopupVerifyResp;
import com.bootcamp.restaurant.model.TopupCreateReq;
import com.stripe.exception.StripeException;

public interface TopupOperation {
    TopupCreateResp createSession(Long walletId, TopupCreateReq req) throws StripeException;
    TopupVerifyResp verify(Long walletId, String sessionId) throws StripeException;
}
