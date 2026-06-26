package com.restaurant.wallet.mapper;

import com.restaurant.wallet.dto.WalletResp;
import com.restaurant.wallet.dto.WalletTransactionResp;
import com.restaurant.wallet.entity.WalletEntity;
import com.restaurant.wallet.entity.WalletTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResp map(WalletEntity entity) {
        return new WalletResp(entity.getWalletId(), entity.getUserId(), entity.getBalance());
    }

    public WalletTransactionResp map(WalletTransactionEntity entity) {
        return new WalletTransactionResp(
                entity.getTransactionId(),
                entity.getWalletEntity().getWalletId(),
                entity.getAdminId(),
                entity.getAmount(),
                entity.getType() != null ? entity.getType().name() : null,
                entity.getDescription(),
                entity.getCreatedAt());
    }
}
