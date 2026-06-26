# WhatsApp 取餐查詢 — Cloudflare Workers 方案

> 狀態：**後端已完成，等待 Meta 憑證**
> 更新日期：2026-06-10
> 功能描述：客人主動發 WhatsApp 輸入取餐碼，系統自動回覆訂單狀態

---

## 一、功能目標

客人持取餐碼（如 `0289-003`）主動查詢取餐狀態：

| 客人傳送 | 系統回覆 |
|---------|---------|
| `0289-003` | 取餐碼 (0289-003) 已完成，14:30 可取餐。 |
| `0289-003` | 取餐碼 (0289-003) 這單號還在製作中，請稍候。 |
| 無效碼 | 找不到取餐碼 (0289-003)，請確認後再試。 |

**注意：廚房不主動發送通知，由客人自行查詢。**

---

## 二、整合架構

```
客人發 WhatsApp（輸入取餐碼）
        ↓
Meta WhatsApp Business API Webhook
        ↓
Cloudflare Workers（免費，無記憶體壓力）
        ↓
GET https://your-domain/api/pickups/status?code=0289-003
        ↓
Java 回傳 { status, expectedTime }（輕量 JSON）
        ↓
Cloudflare Workers 組合回覆訊息
        ↓
Meta WhatsApp API 發回給客人
```

### 為何選 Cloudflare Workers
- 免費：100,000 requests/天
- 零記憶體壓力（不佔 Zeabur 2GB）
- 無需部署額外 server
- N8N Cloud 只有 14 天試用，不適合長期使用

---

## 三、後端（已完成 ✅）

### 新增 endpoint

```
GET /api/pickups/status?code=0289-003
```

**Response（輕量）：**
```json
{ "code": "0289-003", "status": "READY", "expectedTime": "14:30" }
{ "code": "0289-003", "status": "PENDING", "expectedTime": "14:30" }
{ "code": "0289-003", "status": "NOT_FOUND", "expectedTime": null }
```

### 修改的檔案
| 檔案 | 變動 |
|------|------|
| `MealPickupRepository.java` | 新增 `findByMethod(String method)` |
| `PickupStatusResp.java` | 新增 DTO（record，3 個欄位） |
| `MealPickupService.java` | 新增 `getStatusByCode(String code)` |
| `MealPickupController.java` | 新增 `GET /api/pickups/status` |

---

## 四、Cloudflare Workers（待部署）

### 需要準備的 Meta 憑證

| 項目 | 取得位置 | 備注 |
|------|---------|------|
| **Phone Number ID** | Meta Developer Console → WhatsApp → API Setup | 數字 ID，非電話號碼 |
| **Permanent Access Token** | Meta Developer Console → WhatsApp → API Setup → Generate token | 長期 token，勿使用臨時 token |
| **Webhook Verify Token** | 自行設定任意字串 | 例如 `my-canteen-secret-2026` |
| **WhatsApp Business Account ID** | Meta Developer Console | 設定 webhook 時需要 |

### Worker 程式碼（完整，填入憑證即可部署）

```javascript
const JAVA_API_BASE = "https://your-zeabur-domain.zeabur.app";
const WHATSAPP_TOKEN = "YOUR_PERMANENT_ACCESS_TOKEN";
const PHONE_NUMBER_ID = "YOUR_PHONE_NUMBER_ID";
const VERIFY_TOKEN = "YOUR_WEBHOOK_VERIFY_TOKEN";

export default {
  async fetch(request) {
    const url = new URL(request.url);

    // Meta webhook 驗證（初次設定時調用）
    if (request.method === "GET") {
      const mode      = url.searchParams.get("hub.mode");
      const token     = url.searchParams.get("hub.verify_token");
      const challenge = url.searchParams.get("hub.challenge");
      if (mode === "subscribe" && token === VERIFY_TOKEN)
        return new Response(challenge, { status: 200 });
      return new Response("Forbidden", { status: 403 });
    }

    // 收到 WhatsApp 訊息
    if (request.method === "POST") {
      const body = await request.json();
      const msg = body?.entry?.[0]?.changes?.[0]?.value?.messages?.[0];
      if (!msg || msg.type !== "text") return new Response("OK");

      const from = msg.from;                         // 客人電話
      const text = msg.text.body.trim().toUpperCase(); // 取餐碼

      // 查詢 Java API
      const apiRes = await fetch(
        `${JAVA_API_BASE}/api/pickups/status?code=${encodeURIComponent(text)}`
      );
      const data = apiRes.ok ? await apiRes.json() : null;

      // 組合回覆訊息
      let reply;
      if (!data || data.status === "NOT_FOUND") {
        reply = `找不到取餐碼 (${text})，請確認後再試。`;
      } else if (data.status === "READY") {
        reply = `取餐碼 (${data.code}) 已完成，${data.expectedTime ?? ""} 可取餐。`;
      } else {
        reply = `取餐碼 (${data.code}) 這單號還在製作中，請稍候。`;
      }

      // 發送 WhatsApp 回覆
      await fetch(
        `https://graph.facebook.com/v19.0/${PHONE_NUMBER_ID}/messages`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          },
          body: JSON.stringify({
            messaging_product: "whatsapp",
            to: from,
            type: "text",
            text: { body: reply },
          }),
        }
      );

      return new Response("OK");
    }

    return new Response("Method Not Allowed", { status: 405 });
  },
};
```

### 部署步驟
1. 登入 [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Workers & Pages → Create Worker
3. 貼上上方程式碼，填入 4 個憑證常數
4. Deploy → 取得 Worker URL（例如 `https://canteen-wa.yourname.workers.dev`）

---

## 五、Meta Webhook 設定步驟

1. Meta Developer Console → 你的 App → WhatsApp → Configuration
2. Webhook URL：填入 Cloudflare Worker URL
3. Verify Token：填入你設定的 `VERIFY_TOKEN`
4. 勾選 `messages` subscription
5. 點 Verify and Save

---

## 六、環境變數總覽（填入 Worker）

```
JAVA_API_BASE       = https://your-zeabur-domain.zeabur.app
WHATSAPP_TOKEN      = EAAxxxxxxx...（Permanent Token）
PHONE_NUMBER_ID     = 1234567890123（純數字）
VERIFY_TOKEN        = my-canteen-secret-2026（自訂）
```

---

## 七、測試方法

部署後，用手機發 WhatsApp 訊息給你的 Business 號碼：
- 輸入有效取餐碼 → 應收到狀態回覆
- 輸入無效碼 → 應收到「找不到取餐碼」

---

## 八、記憶體影響

| 服務 | RAM |
|------|-----|
| postgres | 400 MB |
| restaurant-app | 700 MB |
| nginx | ~50 MB |
| Cloudflare Workers | **0 MB**（雲端執行） |
| **合計** | **~1.15 GB** ✅ |

---

*更新：2026-06-10 | 後端 endpoint 已合併至 main branch（commit 642ddd1）*
