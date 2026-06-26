package com.bootcamp.restaurant.controller.impl;

import com.bootcamp.restaurant.controller.WalletOperation;
import com.bootcamp.restaurant.dto.WalletResp;
import com.bootcamp.restaurant.dto.WalletTransactionResp;
import com.bootcamp.restaurant.model.WalletAdjustReq;
import com.bootcamp.restaurant.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WalletController implements WalletOperation {

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallets/user/{userId}")
    @Override
    public WalletResp getWallet(@PathVariable Long userId) {
        return walletService.getWalletByUserId(userId);
    }

    @GetMapping("/wallets/{walletId}/transactions")
    @Override
    public List<WalletTransactionResp> getTransactions(@PathVariable Long walletId) {
        return walletService.getTransactions(walletId);
    }

    @PostMapping("/wallets/{walletId}/recharge")
    @Override
    public WalletTransactionResp recharge(@PathVariable Long walletId, @RequestBody WalletAdjustReq req) {
        return walletService.recharge(walletId, req);
    }

    @PostMapping("/wallets/{walletId}/deduct")
    @Override
    public WalletTransactionResp deduct(@PathVariable Long walletId, @RequestBody WalletAdjustReq req) {
        return walletService.deduct(walletId, req);
    }

    @PostMapping("/wallets/{walletId}/refund")
    @Override
    public WalletTransactionResp refund(@PathVariable Long walletId, @RequestBody WalletAdjustReq req) {
        return walletService.refund(walletId, req);
    }
}
