package com.canteen.pickup.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * API 統一回傳格式
 */
@Data
@Builder
public class PickupResponse {
    private Long pickupId;
    private Long itemId;
    private Long adminId;
    private String method;
    private Instant expectedTime;
    private Instant actualTime;
    private Boolean adminNotified;
    private String message; // 例如 "核銷成功" / "已取餐"
}
