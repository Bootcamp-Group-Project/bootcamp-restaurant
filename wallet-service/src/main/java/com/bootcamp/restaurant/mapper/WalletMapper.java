package com.bootcamp.restaurant.mapper;

import com.bootcamp.restaurant.dto.WalletResp;
import com.bootcamp.restaurant.dto.WalletTransactionResp;
import com.bootcamp.restaurant.entity.WalletEntity;
import com.bootcamp.restaurant.entity.WalletTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResp map(WalletEntity entity) {
        return WalletResp.builder()
                .walletId(entity.getWalletId())
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .build();
    }

    public WalletTransactionResp map(WalletTransactionEntity entity) {
        return WalletTransactionResp.builder()
                .transactionId(entity.getTransactionId())
                .walletId(entity.getWalletEntity().getWalletId())
                .adminId(entity.getAdminId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
