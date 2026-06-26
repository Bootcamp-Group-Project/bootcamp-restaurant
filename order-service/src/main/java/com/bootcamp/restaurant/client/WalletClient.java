package com.bootcamp.restaurant.client;

import com.bootcamp.restaurant.exception.WalletServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class WalletClient {

    private final RestClient walletRestClient;

    public WalletClient(@Qualifier("walletRestClient") RestClient walletRestClient) {
        this.walletRestClient = walletRestClient;
    }

    public WalletResp getWalletByUserId(Long userId) {
        try {
            return walletRestClient.get()
                    .uri("/wallets/user/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new WalletServiceException("Wallet lookup failed for userId " + userId
                                + " — HTTP " + resp.getStatusCode());
                    })
                    .body(WalletResp.class);
        } catch (RestClientException e) {
            throw new WalletServiceException("Cannot reach wallet-service: " + e.getMessage());
        }
    }

    public void deduct(Long walletId, BigDecimal amount, Long userId, String idempotencyKey) {
        WalletDeductReq req = new WalletDeductReq(userId, amount, "Order payment", idempotencyKey);
        try {
            walletRestClient.post()
                    .uri("/wallets/{walletId}/deduct", walletId)
                    .body(req)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, resp) -> {
                        throw new WalletServiceException("Deduction failed for walletId " + walletId
                                + " — HTTP " + resp.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new WalletServiceException("Cannot reach wallet-service: " + e.getMessage());
        }
    }
}
