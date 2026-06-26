package com.restaurant.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    long countByIsReadFalse();
    List<FeedbackEntity> findAllByOrderByCreatedAtDesc();
}
