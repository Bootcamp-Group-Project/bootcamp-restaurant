package com.canteen.pickup.client;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 對應同學4 OrderItemResp 嘅結構
 * 用嚟接收 call Part 4 API 返嚟嘅資料
 */
@Data
public class OrderItemDto {
    private Long itemId;
    private Long orderId;
    private Long menuId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String pickupStatus;
    private String specialNote;
}
