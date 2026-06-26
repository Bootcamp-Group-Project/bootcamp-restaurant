package com.restaurant.pickup.repository;

import com.restaurant.pickup.entity.MealPickup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPickupRepository extends JpaRepository<MealPickup, Long> {
    Optional<MealPickup> findByItemId(Long itemId);
    Optional<MealPickup> findByItemIdAndMethod(Long itemId, String method);
    List<MealPickup> findByMethod(String method);
    List<MealPickup> findByAdminNotifiedFalse();

    @Query("SELECT m FROM MealPickup m WHERE m.actualTime IS NULL AND m.expectedTime < :now")
    List<MealPickup> findOverduePickups(Instant now);

    @Query("SELECT m FROM MealPickup m WHERE m.actualTime IS NULL ORDER BY m.expectedTime ASC")
    List<MealPickup> findPendingPickups();
}
