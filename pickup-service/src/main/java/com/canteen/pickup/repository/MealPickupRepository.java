package com.canteen.pickup.repository;

import com.canteen.pickup.entity.MealPickup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPickupRepository extends JpaRepository<MealPickup, Long> {

    // 用 itemId 搵取餐記錄
    Optional<MealPickup> findByItemId(Long itemId);

    // 搵所有未取餐（actual_time 係 NULL）且已過預計時間
    @Query("SELECT m FROM MealPickup m WHERE m.actualTime IS NULL AND m.expectedTime < :now")
    List<MealPickup> findOverduePickups(Instant now);

    // 搵所有未通知 Admin 嘅記錄
    List<MealPickup> findByAdminNotifiedFalse();

    // 搵所有未取餐（actual_time 係 NULL）
    @Query("SELECT m FROM MealPickup m WHERE m.actualTime IS NULL ORDER BY m.expectedTime ASC")
    List<MealPickup> findPendingPickups();

    // 核銷時驗證：itemId + method 必須匹配
    Optional<MealPickup> findByItemIdAndMethod(Long itemId, String method);
}
