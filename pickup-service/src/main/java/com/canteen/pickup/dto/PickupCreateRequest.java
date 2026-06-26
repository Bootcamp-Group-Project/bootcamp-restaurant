package com.canteen.pickup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.Instant;

/**
 * 同學4（訂單系統）訂單付款成功後，call 呢個 DTO 建立取餐記錄
 *
 * 使用場景：
 * 當 Order 狀態變成 Fully_Paid 或 Deposit_Paid，
 * 同學4嘅 OrderService 要 POST /api/pickups/create，
 * 傳入以下資料建立 meal_pickup 記錄
 */
@Data
public class PickupCreateRequest {

    // 來自 Order_Items.Item_ID（同學4嘅表的 PK）
    @NotNull(message = "item_id 唔可以空")
    private Long itemId;

    // 預計取餐時間，來自 Menus.Supply_Time
    @NotNull(message = "expected_time 唔可以空")
    private Instant expectedTime;

    // 核銷碼 = 手機後4碼 + "-" + 訂單末3碼
    // 例如：手機 96125678，訂單 ID 1023 → "5678-023"
    // 由同學4負責計算後傳入
    @NotBlank(message = "核銷碼唔可以空")
    @Pattern(
        regexp = "^\\d{4}-\\d{3}$",
        message = "核銷碼格式錯誤，應為「手機後4碼-訂單末3碼」，例如：5678-023"
    )
    private String method;
}
