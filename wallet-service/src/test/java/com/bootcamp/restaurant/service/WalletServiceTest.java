package com.bootcamp.restaurant.service;

import com.bootcamp.restaurant.dto.WalletResp;
import com.bootcamp.restaurant.dto.WalletTransactionResp;
import com.bootcamp.restaurant.entity.WalletEntity;
import com.bootcamp.restaurant.entity.WalletTransactionEntity;
import com.bootcamp.restaurant.enums.TransactionType;
import com.bootcamp.restaurant.exception.ResourceNotFoundException;
import com.bootcamp.restaurant.mapper.WalletMapper;
import com.bootcamp.restaurant.model.WalletAdjustReq;
import com.bootcamp.restaurant.repository.WalletRepository;
import com.bootcamp.restaurant.repository.WalletTransactionRepository;
import com.bootcamp.restaurant.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private WalletTransactionRepository transactionRepository;
    @Mock private WalletMapper walletMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    // ════════════════════════════════════════════════
    //  getWalletByUserId
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("getWalletByUserId: 錢包存在 → 回傳 WalletResp")
    void getWalletByUserId_whenWalletExists_shouldReturnWalletResp() {
        WalletEntity wallet = WalletEntity.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(500)).build();
        WalletResp expected = WalletResp.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(500)).build();

        when(walletRepository.findByUserId(10L)).thenReturn(Optional.of(wallet));
        when(walletMapper.map(wallet)).thenReturn(expected);

        WalletResp result = walletService.getWalletByUserId(10L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getWalletByUserId: 錢包不存在 → 自動建立餘額 $0 的錢包並回傳")
    void getWalletByUserId_whenNotFound_shouldAutoCreateWallet() {
        WalletEntity created = WalletEntity.builder().walletId(2L).userId(99L).balance(BigDecimal.ZERO).build();
        WalletResp expected = WalletResp.builder().walletId(2L).userId(99L).balance(BigDecimal.ZERO).build();

        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(WalletEntity.class))).thenReturn(created);
        when(walletMapper.map(created)).thenReturn(expected);

        WalletResp result = walletService.getWalletByUserId(99L);

        assertThat(result.getUserId()).isEqualTo(99L);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ════════════════════════════════════════════════
    //  getTransactions
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("getTransactions: 回傳該錢包所有交易紀錄")
    void getTransactions_shouldReturnList() {
        WalletEntity wallet = WalletEntity.builder().walletId(1L).build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .transactionId(1L).walletEntity(wallet)
                .amount(BigDecimal.valueOf(100)).type(TransactionType.RECHARGE).build();
        WalletTransactionResp txResp = WalletTransactionResp.builder()
                .transactionId(1L).walletId(1L)
                .amount(BigDecimal.valueOf(100)).type(TransactionType.RECHARGE).build();

        when(transactionRepository.findByWalletEntity_WalletId(1L)).thenReturn(List.of(tx));
        when(walletMapper.map(tx)).thenReturn(txResp);

        List<WalletTransactionResp> result = walletService.getTransactions(1L);

        assertThat(result).hasSize(1).containsExactly(txResp);
    }

    // ════════════════════════════════════════════════
    //  recharge
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("recharge: 合法金額 → 餘額增加，回傳交易紀錄")
    void recharge_whenValid_shouldIncreaseBalance() {
        WalletEntity wallet = WalletEntity.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(100)).build();
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(200), "top-up", "key-001");
        WalletTransactionResp txResp = WalletTransactionResp.builder()
                .transactionId(1L).amount(BigDecimal.valueOf(200)).type(TransactionType.RECHARGE).build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        when(transactionRepository.save(any())).thenReturn(WalletTransactionEntity.builder().build());
        when(walletMapper.map(any(WalletTransactionEntity.class))).thenReturn(txResp);

        walletService.recharge(1L, req);

        assertThat(wallet.getBalance()).isEqualByComparingTo("300");
    }

    @Test
    @DisplayName("recharge: amount = 0 → 拋出 IllegalArgumentException")
    void recharge_whenAmountIsZero_shouldThrow() {
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.ZERO, "zero", "key-002");

        assertThatThrownBy(() -> walletService.recharge(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    @DisplayName("recharge: amount < 0 → 拋出 IllegalArgumentException")
    void recharge_whenAmountIsNegative_shouldThrow() {
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(-50), "neg", "key-003");

        assertThatThrownBy(() -> walletService.recharge(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    // ════════════════════════════════════════════════
    //  deduct
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("deduct: 餘額充足 → 正常扣款，餘額減少")
    void deduct_whenBalanceSufficient_shouldDecreaseBalance() {
        WalletEntity wallet = WalletEntity.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(500)).build();
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(200), "meal", "key-004");
        WalletTransactionResp txResp = WalletTransactionResp.builder()
                .transactionId(1L).amount(BigDecimal.valueOf(200)).type(TransactionType.DEDUCT).build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        when(transactionRepository.save(any())).thenReturn(WalletTransactionEntity.builder().build());
        when(walletMapper.map(any(WalletTransactionEntity.class))).thenReturn(txResp);

        walletService.deduct(1L, req);

        assertThat(wallet.getBalance()).isEqualByComparingTo("300");
    }

    @Test
    @DisplayName("deduct: 餘額不足 → 拋出 IllegalArgumentException（Insufficient balance）")
    void deduct_whenInsufficientBalance_shouldThrow() {
        WalletEntity wallet = WalletEntity.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(50)).build();
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(200), "meal", "key-005");

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.deduct(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("deduct: amount ≤ 0 → 拋出 IllegalArgumentException")
    void deduct_whenAmountIsZeroOrNegative_shouldThrow() {
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.ZERO, "zero", "key-006");

        assertThatThrownBy(() -> walletService.deduct(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    // ════════════════════════════════════════════════
    //  refund
    // ════════════════════════════════════════════════

    @Test
    @DisplayName("refund: 合法金額 → 餘額增加，回傳交易紀錄")
    void refund_whenValid_shouldIncreaseBalance() {
        WalletEntity wallet = WalletEntity.builder()
                .walletId(1L).userId(10L).balance(BigDecimal.valueOf(100)).build();
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(50), "refund-meal", "key-007");
        WalletTransactionResp txResp = WalletTransactionResp.builder()
                .transactionId(1L).amount(BigDecimal.valueOf(50)).type(TransactionType.REFUND).build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        when(transactionRepository.save(any())).thenReturn(WalletTransactionEntity.builder().build());
        when(walletMapper.map(any(WalletTransactionEntity.class))).thenReturn(txResp);

        walletService.refund(1L, req);

        assertThat(wallet.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    @DisplayName("refund: amount ≤ 0 → 拋出 IllegalArgumentException")
    void refund_whenAmountIsNegative_shouldThrow() {
        WalletAdjustReq req = new WalletAdjustReq(1L, BigDecimal.valueOf(-10), "neg-refund", "key-008");

        assertThatThrownBy(() -> walletService.refund(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }
}
