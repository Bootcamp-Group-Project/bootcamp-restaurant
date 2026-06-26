package com.restaurant.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository repository;

    /** 用戶提交回饋（無需登入） */
    @PostMapping
    public ResponseEntity<Map<String, String>> submit(@RequestBody FeedbackReq req) {
        if (req.content() == null || req.content().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "內容不能為空"));

        FeedbackEntity entity = new FeedbackEntity();
        String content = req.content().trim();
        entity.setContent(content.substring(0, Math.min(content.length(), 1000)));
        entity.setUserId(req.userId());
        entity.setPhone(req.phone());
        repository.save(entity);

        return ResponseEntity.ok(Map.of("message", "已收到您的回饋，感謝！"));
    }

    /** 未讀數量（Admin 用於顯示徽章） */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", repository.countByIsReadFalse()));
    }

    /** 全部回饋列表（Admin） */
    @GetMapping
    public ResponseEntity<List<FeedbackEntity>> getAll() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    /** 標記已讀（Admin） */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        repository.findById(id).ifPresent(f -> {
            f.setRead(true);
            repository.save(f);
        });
        return ResponseEntity.ok().build();
    }

    record FeedbackReq(String content, Long userId, String phone) {}
}
