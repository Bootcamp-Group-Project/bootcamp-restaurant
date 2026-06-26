package com.bootcamp.restaurant.service.impl;

import com.bootcamp.restaurant.dto.TopupCreateResp;
import com.bootcamp.restaurant.dto.TopupVerifyResp;
import com.bootcamp.restaurant.entity.StripeTopupRecord;
import com.bootcamp.restaurant.entity.WalletEntity;
import com.bootcamp.restaurant.entity.WalletTransactionEntity;
import com.bootcamp.restaurant.enums.TopupStatus;
import com.bootcamp.restaurant.enums.TransactionType;
import com.bootcamp.restaurant.exception.ResourceNotFoundException;
import com.bootcamp.restaurant.repository.StripeTopupRepository;
import com.bootcamp.restaurant.repository.WalletRepository;
import com.bootcamp.restaurant.repository.WalletTransactionRepository;
import com.bootcamp.restaurant.service.StripeTopupService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class StripeTopupServiceImpl implements StripeTopupService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.currency}")
    private String currency;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private StripeTopupRepository topupRepository;

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

        // TWD is zero-decimal: pass amount as-is (no multiply by 100)
        long stripeAmount = amount.longValue();

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

        StripeTopupRecord record = StripeTopupRecord.builder()
                .walletId(walletId)
                .stripeSessionId(session.getId())
                .amount(amount)
                .status(TopupStatus.PENDING)
                .build();
        topupRepository.save(record);

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

        // Idempotent: skip if already fulfilled
        if (record.getStatus() == TopupStatus.SUCCEEDED) {
            log.info("Session {} already fulfilled — idempotent skip", sessionId);
            return new TopupVerifyResp(TopupStatus.SUCCEEDED, wallet.getBalance(), "Already processed");
        }

        // Credit wallet balance
        wallet.setBalance(wallet.getBalance().add(record.getAmount()));
        walletRepository.save(wallet);

        // Record the transaction in wallet_transactions
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .walletEntity(wallet)
                .amount(record.getAmount())
                .type(TransactionType.STRIPE_TOPUP)
                .description("Stripe top-up via session " + sessionId)
                .idempotencyKey("stripe-" + sessionId)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // Mark record succeeded
        record.setStripePaymentIntentId(session.getPaymentIntent());
        record.setStatus(TopupStatus.SUCCEEDED);
        topupRepository.save(record);

        log.info("Fulfilled topup session {} — walletId={} new balance={}", sessionId, walletId, wallet.getBalance());
        return new TopupVerifyResp(TopupStatus.SUCCEEDED, wallet.getBalance(), "Top-up successful");
    }
}
