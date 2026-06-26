package com.restaurant.pickup.service;

import com.restaurant.pickup.dto.PickupCreateRequest;
import com.restaurant.pickup.dto.PickupResponse;
import com.restaurant.pickup.dto.PickupStatusResp;
import com.restaurant.pickup.dto.PickupVerifyRequest;
import com.restaurant.pickup.entity.MealPickup;
import com.restaurant.pickup.repository.MealPickupRepository;
import com.restaurant.pickup.websocket.PickupCallMessage;
import com.restaurant.pickup.websocket.PickupWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPickupService {

    private final MealPickupRepository repository;
    private final PickupWebSocketHandler webSocketHandler;

    @Transactional
    public PickupResponse createPickup(PickupCreateRequest req) {
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

    @Transactional
    public PickupResponse verifyPickup(PickupVerifyRequest req) {
        MealPickup pickup = repository.findByItemIdAndMethod(req.getItemId(), req.getMethod())
                .orElseThrow(() -> new IllegalArgumentException("核銷碼錯誤或找不到對應記錄"));
        if (pickup.getActualTime() != null) {
            throw new IllegalStateException("呢個餐已經核銷過喇：" + pickup.getActualTime());
        }
        pickup.setActualTime(Instant.now());
        pickup.setAdminId(req.getAdminId());
        repository.save(pickup);
        return toResponse(pickup, "✅ 核銷成功");
    }

    public PickupResponse callPickup(Long pickupId) {
        MealPickup pickup = repository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 pickupId: " + pickupId));
        webSocketHandler.broadcast(PickupCallMessage.of(pickupId, pickup.getMethod()));
        return toResponse(pickup, "叫號廣播已發送");
    }

    public List<PickupResponse> getOverduePickups() {
        return repository.findOverduePickups(Instant.now())
                .stream().map(p -> toResponse(p, "⚠️ 逾時未取")).toList();
    }

    @Transactional
    public PickupResponse markAdminNotified(Long pickupId) {
        MealPickup pickup = repository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 pickupId: " + pickupId));
        pickup.setAdminNotified(true);
        repository.save(pickup);
        return toResponse(pickup, "已標記通知 Admin");
    }

    public PickupResponse getByItemId(Long itemId) {
        MealPickup pickup = repository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("找不到 itemId: " + itemId));
        return toResponse(pickup, null);
    }

    public List<PickupResponse> getPendingPickups() {
        return repository.findPendingPickups()
                .stream().map(p -> toResponse(p, "待取餐")).toList();
    }

    @Transactional
    public void markReadyByCode(String code) {
        List<MealPickup> pickups = repository.findByMethod(code.toUpperCase());
        Instant now = Instant.now();
        pickups.forEach(p -> { if (p.getActualTime() == null) p.setActualTime(now); });
        repository.saveAll(pickups);
    }

    public PickupStatusResp getStatusByCode(String code) {
        List<MealPickup> pickups = repository.findByMethod(code.toUpperCase());
        if (pickups.isEmpty()) return new PickupStatusResp(code, "NOT_FOUND", null);
        boolean allDone = pickups.stream().allMatch(p -> p.getActualTime() != null);
        String status = allDone ? "READY" : "PENDING";
        String expectedTime = pickups.stream()
                .map(MealPickup::getExpectedTime)
                .filter(t -> t != null)
                .findFirst()
                .map(t -> DateTimeFormatter.ofPattern("M月d日 HH:mm")
                        .withZone(ZoneId.of("Asia/Hong_Kong")).format(t))
                .orElse(null);
        return new PickupStatusResp(code, status, expectedTime);
    }

    public List<PickupResponse> getUnnotified() {
        return repository.findByAdminNotifiedFalse()
                .stream().map(p -> toResponse(p, "未通知")).toList();
    }

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
