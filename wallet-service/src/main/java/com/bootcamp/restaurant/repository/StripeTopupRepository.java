package com.bootcamp.restaurant.repository;

import com.bootcamp.restaurant.entity.StripeTopupRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeTopupRepository extends JpaRepository<StripeTopupRecord, Long> {
    Optional<StripeTopupRecord> findByStripeSessionId(String stripeSessionId);
}
