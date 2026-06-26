package com.bootcamp.restaurant.controller.impl;

import com.bootcamp.restaurant.controller.TopupOperation;
import com.bootcamp.restaurant.dto.TopupCreateResp;
import com.bootcamp.restaurant.dto.TopupVerifyResp;
import com.bootcamp.restaurant.model.TopupCreateReq;
import com.bootcamp.restaurant.service.StripeTopupService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets/{walletId}/topup")
public class TopupController implements TopupOperation {

    @Autowired
    private StripeTopupService stripeTopupService;

    @PostMapping("/create-session")
    @Override
    public TopupCreateResp createSession(@PathVariable Long walletId, @RequestBody TopupCreateReq req)
            throws StripeException {
        return stripeTopupService.createTopupSession(walletId, req.amount());
    }

    @GetMapping("/verify")
    @Override
    public TopupVerifyResp verify(@PathVariable Long walletId, @RequestParam String sessionId)
            throws StripeException {
        return stripeTopupService.verifyTopup(walletId, sessionId);
    }
}
