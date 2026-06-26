package com.restaurant.order.client;

import com.restaurant.order.client.dto.WalletClientResp;
import com.restaurant.order.client.dto.WalletDeductReq;
import com.restaurant.order.client.dto.WalletTransactionClientResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Order 模組透過 Feign 呼叫 Wallet 模組。
 *
 * URL 指向 127.0.0.1:8080（同一個 JVM）。
 * 未來拆成真正微服務時，只需把 url 改成 wallet-service 的實際地址。
 */
@FeignClient(name = "wallet-client", url = "http://127.0.0.1:8080")
public interface WalletClient {

    @GetMapping("/wallets/user/{userId}")
    WalletClientResp getWalletByUserId(@PathVariable Long userId);

    @PostMapping("/wallets/{walletId}/deduct")
    WalletTransactionClientResp deduct(@PathVariable Long walletId,
                                       @RequestBody WalletDeductReq req);

    @PostMapping("/wallets/{walletId}/refund")
    WalletTransactionClientResp refund(@PathVariable Long walletId,
                                       @RequestBody WalletDeductReq req);
}
