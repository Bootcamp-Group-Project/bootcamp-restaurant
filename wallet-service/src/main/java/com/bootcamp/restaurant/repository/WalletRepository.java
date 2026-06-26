package com.bootcamp.restaurant.repository;

import com.bootcamp.restaurant.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    Optional<WalletEntity> findByUserId(Long userId);
}
