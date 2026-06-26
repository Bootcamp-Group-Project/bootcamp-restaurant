package com.bootcamp.restaurant.controller;

import com.bootcamp.restaurant.dto.WalletResp;
import com.bootcamp.restaurant.dto.WalletTransactionResp;
import com.bootcamp.restaurant.model.WalletAdjustReq;

import java.util.List;

public interface WalletOperation {
    WalletResp getWallet(Long userId);
    List<WalletTransactionResp> getTransactions(Long walletId);
    WalletTransactionResp recharge(Long walletId, WalletAdjustReq req);
    WalletTransactionResp deduct(Long walletId, WalletAdjustReq req);
    WalletTransactionResp refund(Long walletId, WalletAdjustReq req);
}
