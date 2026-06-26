package com.canteen.pickup.service;

import com.canteen.pickup.dto.PickupCreateRequest;
import com.canteen.pickup.dto.PickupResponse;
import com.canteen.pickup.dto.PickupVerifyRequest;
import com.canteen.pickup.entity.MealPickup;
import com.canteen.pickup.repository.MealPickupRepository;
import com.canteen.pickup.websocket.PickupCallMessage;
import com.canteen.pickup.websocket.PickupWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPickupService {

    private final MealPickupRepository repository;
    private final PickupWebSocketHandler webSocketHandler;

    // ── 建立取餐記錄（同學4訂單付款後 call 呢個）────────────────────────────
    // 同學4：當 Order 狀態係 Deposit_Paid 或 Fully_Paid，
    //        要幫每個 Order_Item call 呢個方法
    @Transactional
    public PickupResponse createPickup(PickupCreateRequest req) {
        // 冪等：同一個 item_id 已有記錄就直接回傳
        return repository.findByItemId(req.getItemId())
                .map(existing -> toResponse(existing, "取餐記錄已存在"))
                .orElseGet(() -> {
                    MealPickup pickup = new MealPickup();
                    pickup.setItemId(req.getItemId());
                    pickup.setExpectedTime(req.getExpectedTime());
                    pickup.setMethod(req.getMethod());
                    pickup.setAdminNotified(false);
                    repository.save(pickup);
                    return toResponse(pickup, "取餐記錄建立成功");
                });
    }

    // ── 核銷取餐 ──────────────────────────────────────────────────────────────
    // Admin 輸入核銷碼（手機後4碼-訂單末3碼），確認學生取餐
    @Transactional
    public PickupResponse verifyPickup(PickupVerifyRequest req) {
        // 1. 搵番呢個 item 嘅取餐記錄，並驗證核銷碼
        MealPickup pickup = repository.findByItemIdAndMethod(req.getItemId(), req.getMethod())
                .orElseThrow(() -> new IllegalArgumentException("核銷碼錯誤或找不到對應記錄"));

        // 2. 防止重複核銷
        if (pickup.getActualTime() != null) {
            throw new IllegalStateException("呢個餐已經核銷過喇：" + pickup.getActualTime());
        }

        // 3. 記錄實際取餐時間 + 記錄負責 Admin
        pickup.setActualTime(Instant.now());
        pickup.setAdminId(req.getAdminId());
        repository.save(pickup);

        return toResponse(pickup, "✅ 核銷成功");
    }

    // ── 廚房叫號：廣播取餐通知給所有大螢幕 ────────────────────────────────
    public PickupResponse callPickup(Long pickupId) {
        MealPickup pickup = repository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 pickupId: " + pickupId));
        webSocketHandler.broadcast(PickupCallMessage.of(pickupId, pickup.getMethod()));
        return toResponse(pickup, "叫號廣播已發送");
    }

    // ── 查詢所有逾時未取餐 ──────────────────────────────────────────────────
    public List<PickupResponse> getOverduePickups() {
        return repository.findOverduePickups(Instant.now())
                .stream()
                .map(p -> toResponse(p, "⚠️ 逾時未取"))
                .toList();
    }

    // ── 標記已通知 Admin ────────────────────────────────────────────────────
    @Transactional
    public PickupResponse markAdminNotified(Long pickupId) {
        MealPickup pickup = repository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 pickupId: " + pickupId));

        pickup.setAdminNotified(true);
        repository.save(pickup);

        return toResponse(pickup, "已標記通知 Admin");
    }

    // ── 查詢單一取餐記錄 ────────────────────────────────────────────────────
    public PickupResponse getByItemId(Long itemId) {
        MealPickup pickup = repository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 itemId: " + itemId));
        return toResponse(pickup, null);
    }

    // ── 查詢所有未取餐記錄（actualTime 係 NULL）────────────────────────────
    public List<PickupResponse> getPendingPickups() {
        return repository.findPendingPickups()
                .stream()
                .map(p -> toResponse(p, "待取餐"))
                .toList();
    }

    // ── 查詢所有未通知 Admin 嘅記錄 ────────────────────────────────────────
    public List<PickupResponse> getUnnotified() {
        return repository.findByAdminNotifiedFalse()
                .stream()
                .map(p -> toResponse(p, "未通知"))
                .toList();
    }

    // ── 私有：Entity → DTO ─────────────────────────────────────────────────
    private PickupResponse toResponse(MealPickup p, String message) {
        return PickupResponse.builder()
                .pickupId(p.getPickupId())
                .itemId(p.getItemId())
                .adminId(p.getAdminId())
                .method(p.getMethod())
                .expectedTime(p.getExpectedTime())
                .actualTime(p.getActualTime())
                .adminNotified(p.getAdminNotified())
                .message(message)
                .build();
    }
}
