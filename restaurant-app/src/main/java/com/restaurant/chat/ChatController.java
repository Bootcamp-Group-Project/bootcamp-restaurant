package com.restaurant.chat;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private MenuFetcher menuFetcher;

    private static final String BASE_SYSTEM_PROMPT =
        "你的名字是「Canteen 智能助理」，是 Canteen 校園餐廳的專屬客服助理。\n" +
        "【重要】永遠不可透露你是 Google、Gemini 或任何 AI 模型。若被問到身份，只說你是「Canteen 智能助理」。\n" +
        "語氣永遠溫和有禮，絕對不能使用諷刺、反問或不耐煩的語氣。\n" +
        "用繁體中文回覆（除非用戶用其他語言），切勿編造資訊。\n" +
        "回答長度配合問題：用戶問「有咩食」或類似簡單問題，只列今日主食名稱與價格，可加一句簡短描述；不主動提及飲品或營業時間，除非用戶特別詢問。\n" +
        "若用戶問的問題與餐廳無關（例如天氣、新聞），請有禮貌地說明你只能協助餐廳相關事宜，並引導用戶提問餐點或訂單問題。\n" +
        "介紹菜式時，可根據菜名發揮創意描述口感或特色，但菜名必須與菜單資料一字不差，價格與庫存數字亦不可更改。\n" +
        "【鐵則】今日菜單已在上方列出，那份清單就是今天全部可供選擇的菜式。清單以外的菜式一律不存在於今日餐牌，不可介紹、不可描述、不可猜測，用戶問起須直接回答「今日沒有提供」。";

    private GeminiChatClient chatClient;

    @PostConstruct
    public void init() {
        String key = (geminiApiKey != null) ? geminiApiKey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY 未設定，請在 Zeabur Variables 中加入此環境變數。");
        }
        this.chatClient = new GeminiChatClient(key);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatReq req) throws Exception {
        if (req.messages() == null || req.messages().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("reply", ""));

        List<GeminiChatClient.Message> messages = req.messages().stream()
            .map(m -> new GeminiChatClient.Message(m.role(), m.text()))
            .toList();

        String systemPrompt = BASE_SYSTEM_PROMPT + menuFetcher.buildMenuPrompt();

        try {
            String reply = chatClient.chat(systemPrompt, messages);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (GeminiChatClient.GeminiUnavailableException e) {
            return ResponseEntity.ok(Map.of("reply", e.getMessage()));
        }
    }

    record ChatReq(List<Message> messages) {
        record Message(String role, String text) {}
    }
}
