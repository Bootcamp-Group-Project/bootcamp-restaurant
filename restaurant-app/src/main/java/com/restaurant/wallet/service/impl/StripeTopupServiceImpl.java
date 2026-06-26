package com.restaurant.wallet.service.impl;

import com.restaurant.shared.exception.ResourceNotFoundException;
import com.restaurant.wallet.dto.TopupCreateResp;
import com.restaurant.wallet.dto.TopupVerifyResp;
import com.restaurant.wallet.entity.StripeTopupRecord;
import com.restaurant.wallet.entity.WalletEntity;
import com.restaurant.wallet.entity.WalletTransactionEntity;
import com.restaurant.wallet.enums.TopupStatus;
import com.restaurant.wallet.enums.TransactionType;
import com.restaurant.wallet.repository.StripeTopupRepository;
import com.restaurant.wallet.repository.WalletRepository;
import com.restaurant.wallet.repository.WalletTransactionRepository;
import com.restaurant.wallet.service.StripeTopupService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeTopupServiceImpl implements StripeTopupService {

    @Value("${stripe.secret-key}")  private String stripeSecretKey;
    @Value("${stripe.success-url}") private String successUrl;
    @Value("${stripe.cancel-url}")  private String cancelUrl;
    @Value("${stripe.currency}")    private String currency;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final StripeTopupRepository topupRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public TopupCreateResp createTopupSession(Long walletId, BigDecimal amount) throws StripeException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }
        walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        long stripeAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(stripeAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Wallet Top-up")
                                                                .setDescription("Top up wallet #" + walletId + " by " + amount + " " + currency.toUpperCase())
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("walletId", String.valueOf(walletId))
                .build();

        Session session = Session.create(params);

        topupRepository.save(StripeTopupRecord.builder()
                .walletId(walletId)
                .stripeSessionId(session.getId())
                .amount(amount)
                .status(TopupStatus.PENDING)
                .build());

        log.info("Created Stripe topup session {} for walletId={} amount={}", session.getId(), walletId, amount);
        return new TopupCreateResp(session.getUrl());
    }

    @Override
    @Transactional
    public TopupVerifyResp verifyTopup(Long walletId, String sessionId) throws StripeException {
        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        Session session = Session.retrieve(sessionId);

        if (!"paid".equals(session.getPaymentStatus())) {
            return new TopupVerifyResp(TopupStatus.PENDING, wallet.getBalance(), "Payment not completed");
        }

        StripeTopupRecord record = topupRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Topup record not found for session: " + sessionId));

        if (record.getStatus() == TopupStatus.SUCCEEDED) {
            log.info("Session {} already fulfilled — idempotent skip", sessionId);
            return new TopupVerifyResp(TopupStatus.SUCCEEDED, wallet.getBalance(), "Already processed");
        }

        wallet.setBalance(wallet.getBalance().add(record.getAmount()));
        walletRepository.save(wallet);

        transactionRepository.save(WalletTransactionEntity.builder()
                .walletEntity(wallet)
                .amount(record.getAmount())
                .type(TransactionType.STRIPE_TOPUP)
                .description("Stripe top-up via session " + sessionId)
                .idempotencyKey("stripe-" + sessionId)
                .createdAt(LocalDateTime.now())
                .build());

        record.setStripePaymentIntentId(session.getPaymentIntent());
        record.setStatus(TopupStatus.SUCCEEDED);
        topupRepository.save(record);

        log.info("Fulfilled topup session {} — walletId={} new balance={}", sessionId, walletId, wallet.getBalance());
        return new TopupVerifyResp(TopupStatus.SUCCEEDED, wallet.getBalance(), "Top-up successful");
    }
}
