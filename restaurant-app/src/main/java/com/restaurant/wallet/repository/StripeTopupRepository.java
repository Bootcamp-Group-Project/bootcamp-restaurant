package com.restaurant.wallet.repository;

import com.restaurant.wallet.entity.StripeTopupRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeTopupRepository extends JpaRepository<StripeTopupRecord, Long> {
    Optional<StripeTopupRecord> findByStripeSessionId(String stripeSessionId);
}
