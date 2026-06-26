package com.restaurant.wallet.controller;

import com.restaurant.wallet.dto.TopupCreateReq;
import com.restaurant.wallet.dto.TopupCreateResp;
import com.restaurant.wallet.dto.TopupVerifyResp;
import com.restaurant.wallet.service.StripeTopupService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets/{walletId}/topup")
@RequiredArgsConstructor
public class TopupController {

    private final StripeTopupService stripeTopupService;

    @PostMapping("/create-session")
    public TopupCreateResp createSession(@PathVariable Long walletId,
                                         @RequestBody TopupCreateReq req) throws StripeException {
        return stripeTopupService.createTopupSession(walletId, req.amount());
    }

    @GetMapping("/verify")
    public TopupVerifyResp verify(@PathVariable Long walletId,
                                  @RequestParam String sessionId) throws StripeException {
        return stripeTopupService.verifyTopup(walletId, sessionId);
    }
}
