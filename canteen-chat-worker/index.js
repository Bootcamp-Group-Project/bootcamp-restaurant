const SYSTEM_PROMPT = `現在你正透過一個網頁 HTML 聊天介面，直接為用戶提供即時的諮詢與協助。

# Core Persona (核心人格)
- 語氣特質：溫和、真誠、謙遜且極具同理心。
- 溝通風格：條理分明、邏輯嚴謹。回答時不說廢話，直奔主題，但保持高度客氣。
- 身份認同：當被問及身份時，堅定表明自己是 Claude。

# Workflow & Constraints (工作流程與限制)
1. 介面適配：由於你處於一個寬度較窄的 HTML 網頁對話框中，請避免輸出長篇大論。每段回答應保持簡短、精煉，多用換行與列點（Bullet Points）來提升手機或網頁端的易讀性。
2. 語言一致：用戶使用什麼語言（如繁體中文、英文），你就必須使用相同的語言與字體回覆。
3. 誠實原則：遇到不知道的資訊或超出知識範圍的即時動態，請直接、誠實地表明無法提供，切勿編造事實。
4. 本地指令協同：前端介面已內建簡單固定指令（例如：幫助、關於、聯絡）。如果用戶詢問這類基礎商務資訊，你可以用溫和的語氣引導他們，或者直接給出精簡的對應解答。

# Output Style (輸出範例格式)
- 重要關鍵字使用**粗體**標記。
- 步驟或多項觀點請使用 - 或 1. 進行排版。
- 句尾可適度使用溫和的標點符號，不需使用過多誇張的表情符號。

# 關於本餐廳
你是 Canteen 校園餐廳的智能助理。餐廳提供午餐訂餐服務，學生和教職員可透過網頁點餐，使用電子錢包付款。如需查詢取餐狀況，用戶只需提供取餐碼（格式如 0289-018），你會即時查詢並回覆。`;

const PICKUP_CODE_RE = /\b([0-9]{4})-([0-9]{3})\b/;

async function fetchPickupStatus(code, javaBase) {
  try {
    const res = await fetch(`${javaBase}/api/pickups/status?code=${encodeURIComponent(code)}`);
    if (!res.ok) return null;
    return await res.json();
  } catch (_) {
    return null;
  }
}

export default {
  async fetch(request, env) {
    // CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type",
        },
      });
    }

    if (request.method !== "POST") return new Response("Method Not Allowed", { status: 405 });

    let body;
    try {
      body = await request.json();
    } catch (_) {
      return new Response("Bad Request", { status: 400 });
    }

    const messages = Array.isArray(body.messages) ? body.messages : [];
    if (!messages.length) return new Response(JSON.stringify({ reply: "" }), {
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    });

    // Detect pickup code in the latest user message and inject status
    const lastUserMsg = [...messages].reverse().find(m => m.role === "user")?.content ?? "";
    let systemExtra = "";
    const codeMatch = lastUserMsg.match(PICKUP_CODE_RE);
    if (codeMatch) {
      const code = `${codeMatch[1]}-${codeMatch[2]}`;
      const status = await fetchPickupStatus(code, env.JAVA_API_BASE);
      if (status) {
        const statusText = status.status === "READY"
          ? `已完成，可以取餐`
          : status.status === "PENDING"
          ? `製作中，請稍候`
          : `找不到此取餐碼`;
        systemExtra = `\n\n[系統資訊] 取餐碼 ${code} 的即時狀態：${statusText}。預計時間：${status.expectedTime ?? "未設定"}。請根據此資訊回答用戶。`;
      }
    }

    const claudeRes = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-api-key": env.ANTHROPIC_API_KEY,
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        model: "claude-haiku-4-5-20251001",
        max_tokens: 512,
        system: SYSTEM_PROMPT + systemExtra,
        messages,
      }),
    });

    if (!claudeRes.ok) {
      const err = await claudeRes.text();
      console.log(`Claude error: ${claudeRes.status} ${err}`);
      return new Response(JSON.stringify({ reply: "抱歉，服務暫時出現問題，請稍後再試。" }), {
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      });
    }

    const claudeData = await claudeRes.json();
    const reply = claudeData.content?.[0]?.text ?? "";

    return new Response(JSON.stringify({ reply }), {
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    });
  },
};
