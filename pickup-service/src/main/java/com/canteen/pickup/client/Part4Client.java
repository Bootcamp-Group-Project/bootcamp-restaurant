package com.canteen.pickup.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 負責 HTTP call 同學4（Order/Wallet 系統）嘅 API
 *
 * 同學4嘅 API 跑係 http://localhost:8084
 * 用到嘅 endpoints:
 *   GET /orders/{orderId}/items  → 搵訂單內所有 items
 *   GET /orders/{orderId}        → 搵訂單資料（包含 userId）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Part4Client {

    private final RestTemplate restTemplate;

    // 同學4嘅 server 地址，設定喺 application.properties
    @Value("${part4.base-url:http://localhost:8084}")
    private String part4BaseUrl;

    /**
     * 驗證 itemId 係咪存在喺同學4嘅 order_items 表
     * 建立取餐記錄前先 call 呢個確認
     */
    public boolean itemExists(Long itemId) {
        try {
            // 同學4冇直接搵單個 item 嘅 API
            // 所以我哋 call 佢返 200 就當存在
            restTemplate.getForObject(
                part4BaseUrl + "/orders/items/" + itemId,
                OrderItemDto.class
            );
            return true;
        } catch (Exception e) {
            log.warn("Item {} 喺 Part 4 唔存在或 Part 4 未啟動: {}", itemId, e.getMessage());
            return false;
        }
    }

    /**
     * 搵某張訂單嘅所有 items
     * 用途：查返訂單有幾多個 item 要建立取餐記錄
     */
    public OrderItemDto[] getOrderItems(Long orderId) {
        try {
            return restTemplate.getForObject(
                part4BaseUrl + "/orders/" + orderId + "/items",
                OrderItemDto[].class
            );
        } catch (Exception e) {
            log.error("Call Part 4 失敗，orderId={}: {}", orderId, e.getMessage());
            return new OrderItemDto[0];
        }
    }
}
