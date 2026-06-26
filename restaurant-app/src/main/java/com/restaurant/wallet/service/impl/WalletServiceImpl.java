package com.restaurant.wallet.service.impl;

import com.restaurant.shared.exception.ResourceNotFoundException;
import com.restaurant.wallet.dto.WalletAdjustReq;
import com.restaurant.wallet.dto.WalletResp;
import com.restaurant.wallet.dto.WalletTransactionResp;
import com.restaurant.wallet.entity.WalletEntity;
import com.restaurant.wallet.entity.WalletTransactionEntity;
import com.restaurant.wallet.enums.TransactionType;
import com.restaurant.wallet.mapper.WalletMapper;
import com.restaurant.wallet.repository.WalletRepository;
import com.restaurant.wallet.repository.WalletTransactionRepository;
import com.restaurant.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletResp getWalletByUserId(Long userId) {
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(
                        WalletEntity.builder().userId(userId).balance(BigDecimal.ZERO).build()));
        return walletMapper.map(wallet);
    }

    @Override
    public List<WalletTransactionResp> getTransactions(Long walletId) {
        return transactionRepository.findByWalletEntity_WalletId(walletId)
                .stream().map(walletMapper::map).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WalletTransactionResp recharge(Long walletId, WalletAdjustReq req) {
        return adjust(walletId, req, TransactionType.RECHARGE);
    }

    @Override
    @Transactional
    public WalletTransactionResp deduct(Long walletId, WalletAdjustReq req) {
        return adjust(walletId, req, TransactionType.DEDUCT);
    }

    @Override
    @Transactional
    public WalletTransactionResp refund(Long walletId, WalletAdjustReq req) {
        return adjust(walletId, req, TransactionType.REFUND);
    }

    private WalletTransactionResp adjust(Long walletId, WalletAdjustReq req, TransactionType type) {
        if (req.idempotencyKey() != null) {
            var existing = transactionRepository.findByIdempotencyKey(req.idempotencyKey());
            if (existing.isPresent()) return walletMapper.map(existing.get());
        }

        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + req.amount());
        }

        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        if (type == TransactionType.DEDUCT && wallet.getBalance().compareTo(req.amount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance: balance=" + wallet.getBalance() + ", requested=" + req.amount());
        }

        BigDecimal newBalance = type == TransactionType.DEDUCT
                ? wallet.getBalance().subtract(req.amount())
                : wallet.getBalance().add(req.amount());

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransactionEntity transaction = WalletTransactionEntity.builder()
                .walletEntity(wallet)
                .adminId(req.adminId())
                .amount(req.amount())
                .type(type)
                .description(req.description())
                .idempotencyKey(req.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();

        return walletMapper.map(transactionRepository.save(transaction));
    }
}
