package com.restaurant.wallet.repository;

import com.restaurant.wallet.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findByWalletEntity_WalletId(Long walletId);
    Optional<WalletTransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
