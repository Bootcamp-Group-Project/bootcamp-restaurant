package com.canteen.pickup.controller;

import com.canteen.pickup.dto.PickupCreateRequest;
import com.canteen.pickup.dto.PickupResponse;
import com.canteen.pickup.dto.PickupVerifyRequest;
import com.canteen.pickup.service.MealPickupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickups")
@RequiredArgsConstructor
public class MealPickupController {

    private final MealPickupService service;

    // POST /api/pickups/create
    // ⭐ 同學4（訂單系統）專用：訂單付款成功後 call 呢個建立取餐記錄
    // 同學4唔需要直接操作資料庫，call 呢個 API 就得
    @PostMapping("/create")
    public ResponseEntity<PickupResponse> create(@Valid @RequestBody PickupCreateRequest req) {
        return ResponseEntity.status(201).body(service.createPickup(req));
    }

    // POST /api/pickups/verify
    // Admin 核銷取餐（輸入核銷碼）
    @PostMapping("/verify")
    public ResponseEntity<PickupResponse> verify(@Valid @RequestBody PickupVerifyRequest req) {
        return ResponseEntity.ok(service.verifyPickup(req));
    }

    // GET /api/pickups/item/{itemId}
    // 查詢某個 order item 嘅取餐狀態
    @GetMapping("/item/{itemId}")
    public ResponseEntity<PickupResponse> getByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.getByItemId(itemId));
    }

    // GET /api/pickups/pending
    // 查詢所有未取餐記錄（actual_time 係 NULL，按預計時間升序）
    @GetMapping("/pending")
    public ResponseEntity<List<PickupResponse>> getPending() {
        return ResponseEntity.ok(service.getPendingPickups());
    }

    // GET /api/pickups/overdue
    // 查詢所有逾時未取餐（已過預計時間但 actual_time 係 NULL）
    @GetMapping("/overdue")
    public ResponseEntity<List<PickupResponse>> getOverdue() {
        return ResponseEntity.ok(service.getOverduePickups());
    }

    // GET /api/pickups/unnotified
    // 查詢所有未通知 Admin 嘅記錄
    @GetMapping("/unnotified")
    public ResponseEntity<List<PickupResponse>> getUnnotified() {
        return ResponseEntity.ok(service.getUnnotified());
    }

    // PATCH /api/pickups/{pickupId}/notify
    // 標記已通知 Admin
    @PatchMapping("/{pickupId}/notify")
    public ResponseEntity<PickupResponse> markNotified(@PathVariable Long pickupId) {
        return ResponseEntity.ok(service.markAdminNotified(pickupId));
    }

    // POST /api/pickups/{pickupId}/call
    // 廚房叫號：廣播取餐通知至所有大螢幕（WebSocket 推播）
    @PostMapping("/{pickupId}/call")
    public ResponseEntity<PickupResponse> call(@PathVariable Long pickupId) {
        return ResponseEntity.ok(service.callPickup(pickupId));
    }
}
