package com.restaurant.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Gemini 原生 API 客戶端。
 *
 * 端點：POST /v1/models/gemini-1.5-flash:generateContent?key={key}
 * 認證：?key= query string
 *
 * 重試策略：
 *   503 → 等 1s 重試 → 等 2s 重試 → 拋出 GeminiUnavailableException
 *   非 200（含 system_instruction 格式錯誤）→ 自動降級：移除 system_instruction，
 *     改將 system prompt 置入第一條 user message 重試一次
 */
public class GeminiChatClient {

    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent";

    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GeminiChatClient(String apiKey) {
        this.apiKey  = apiKey;
        this.http    = HttpClient.newHttpClient();
        this.mapper  = new ObjectMapper();
    }

    public String chat(String systemPrompt, List<Message> messages) throws Exception {

        // ── 503 退避重試（最多 3 次）──────────────────────────
        HttpResponse<String> res = callWithSystemInstruction(systemPrompt, messages);

        if (res.statusCode() == 503) {
            Thread.sleep(1_000);
            res = callWithSystemInstruction(systemPrompt, messages);
        }
        if (res.statusCode() == 503) {
            Thread.sleep(2_000);
            res = callWithSystemInstruction(systemPrompt, messages);
        }
        if (res.statusCode() == 503) {
            throw new GeminiUnavailableException("Gemini 服務目前壅塞，請稍後再試。");
        }

        // ── 若非 200，移除 system_instruction 降級重試 ────────
        // 原因：部分帳號或版本對 system_instruction 欄位格式不相容
        if (res.statusCode() != 200) {
            System.out.println("[GeminiClient] 降級：移除 system_instruction，改用純 contents 重試");
            res = callContentsOnly(systemPrompt, messages);
        }

        return parseReply(res);
    }

    // ── 完整請求：含 system_instruction ──────────────────────

    private HttpResponse<String> callWithSystemInstruction(String systemPrompt,
                                                           List<Message> messages) throws Exception {
        ObjectNode body = mapper.createObjectNode();

        // system_instruction 必須在 JSON 第一層，且包含 parts 陣列
        ObjectNode sysInstruction = mapper.createObjectNode();
        ArrayNode sysParts = mapper.createArrayNode();
        sysParts.add(mapper.createObjectNode().put("text", systemPrompt));
        sysInstruction.set("parts", sysParts);
        body.set("system_instruction", sysInstruction);

        body.set("contents", buildContents(messages));
        body.set("generationConfig", buildGenConfig());

        return send(body);
    }

    // ── 降級請求：無 system_instruction，system prompt 併入第一條 user message ──

    private HttpResponse<String> callContentsOnly(String systemPrompt,
                                                  List<Message> messages) throws Exception {
        ObjectNode body = mapper.createObjectNode();

        ArrayNode contents = mapper.createArrayNode();

        // 將 system prompt 作為第一條 user message 前綴，確保模型仍能接收指令
        if (!messages.isEmpty()) {
            Message first = messages.get(0);
            String combined = "[系統指令] " + systemPrompt + "\n\n" + first.text();
            contents.add(buildContent("user", combined));
            for (int i = 1; i < messages.size(); i++) {
                contents.add(buildContent(messages.get(i).role(), messages.get(i).text()));
            }
        }

        body.set("contents", contents);
        body.set("generationConfig", buildGenConfig());

        return send(body);
    }

    // ── 共用工具方法 ──────────────────────────────────────────

    private ArrayNode buildContents(List<Message> messages) {
        ArrayNode contents = mapper.createArrayNode();
        for (Message m : messages) {
            contents.add(buildContent(m.role(), m.text()));
        }
        return contents;
    }

    private ObjectNode buildContent(String role, String text) {
        ObjectNode content = mapper.createObjectNode();
        // Google 原生 API：role 只接受 "user" / "model"
        content.put("role", "assistant".equals(role) ? "model" : role);
        ArrayNode parts = mapper.createArrayNode();
        parts.add(mapper.createObjectNode().put("text", text));
        content.set("parts", parts);
        return content;
    }

    private ObjectNode buildGenConfig() {
        ObjectNode genConfig = mapper.createObjectNode();
        genConfig.put("maxOutputTokens", 800);
        return genConfig;
    }

    private HttpResponse<String> send(ObjectNode body) throws Exception {
        String url = ENDPOINT + "?key=" + apiKey;
        System.out.println("Calling URL: " + url.replace(apiKey, "REDACTED"));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private String parseReply(HttpResponse<String> res) throws Exception {
        if (res.statusCode() != 200) {
            JsonNode err = mapper.readTree(res.body());
            String msg = err.path("error").path("message").asText(res.body());
            return "[Gemini錯誤] " + msg;
        }
        return mapper.readTree(res.body())
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("服務暫時無法回應，請稍後再試。");
    }

    static class GeminiUnavailableException extends RuntimeException {
        GeminiUnavailableException(String msg) { super(msg); }
    }

    record Message(String role, String text) {}
}
