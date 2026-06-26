package com.canteen.pickup.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * 對應資料庫 meal_pickups 表
 * 記錄每個 order_item 嘅取餐狀態
 */
@Data
@Entity
@Table(name = "meal_pickups")
public class MealPickup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pickup_id")
    private Long pickupId;

    // 對應 Order_Items.Item_ID（由其他同學管理，呢度只存 FK）
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    // 負責核銷嘅管理員
    @Column(name = "admin_id")
    private Long adminId;

    // 核銷方式：手機後4碼 + 訂單末3碼，例如 "56781234-023"
    // 格式：{phone_last4}-{order_last3}
    @Column(name = "method", length = 10)
    private String method;

    // 預計取餐時間（UTC）
    @Column(name = "expected_time")
    private Instant expectedTime;

    // 實際取餐時間（UTC），未取就係 NULL
    @Column(name = "actual_time")
    private Instant actualTime;

    // 0 = 未通知 Admin, 1 = 已通知 Admin
    @Column(name = "admin_notified", nullable = false)
    private Boolean adminNotified = false;
}
