package com.restaurant.wallet.service;

import com.restaurant.wallet.dto.WalletAdjustReq;
import com.restaurant.wallet.dto.WalletResp;
import com.restaurant.wallet.dto.WalletTransactionResp;

import java.util.List;

public interface WalletService {
    WalletResp getWalletByUserId(Long userId);
    List<WalletTransactionResp> getTransactions(Long walletId);
    WalletTransactionResp recharge(Long walletId, WalletAdjustReq req);
    WalletTransactionResp deduct(Long walletId, WalletAdjustReq req);
    WalletTransactionResp refund(Long walletId, WalletAdjustReq req);
}
