package com.bootcamp.restaurant.repository;

import com.bootcamp.restaurant.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findByWalletEntity_WalletId(Long walletId);
}
