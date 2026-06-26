package com.restaurant.wallet.controller;

import com.restaurant.wallet.dto.WalletResp;
import com.restaurant.wallet.dto.WalletAdjustReq;
import com.restaurant.wallet.dto.WalletTransactionResp;
import com.restaurant.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    public WalletResp getByUser(@PathVariable Long userId) {
        return walletService.getWalletByUserId(userId);
    }

    @GetMapping("/{walletId}/transactions")
    public List<WalletTransactionResp> getTransactions(@PathVariable Long walletId) {
        return walletService.getTransactions(walletId);
    }

    @PostMapping("/{walletId}/recharge")
    public WalletTransactionResp recharge(@PathVariable Long walletId,
                                          @RequestBody WalletAdjustReq req) {
        return walletService.recharge(walletId, req);
    }

    @PostMapping("/{walletId}/deduct")
    public WalletTransactionResp deduct(@PathVariable Long walletId,
                                        @RequestBody WalletAdjustReq req) {
        return walletService.deduct(walletId, req);
    }

    @PostMapping("/{walletId}/refund")
    public WalletTransactionResp refund(@PathVariable Long walletId,
                                        @RequestBody WalletAdjustReq req) {
        return walletService.refund(walletId, req);
    }
}
