
  http://localhost:8080/admin.html
http://localhost:8080/kitchen.html
http://localhost:8080/index.html


---

## 1. 測試帳號（HTTP Basic Auth）

| 帳號 | 密碼 | 角色 |
|------|------|------|
| `student` | `student123` | STUDENT |
| `teacher` | `teacher123` | TEACHER |
| `kitchen_user` | `kitchen123` | KITCHEN_USER |
| `admin_user` | `admin123` | ADMIN_USER |

> **Demo 模式**：`SecurityConfig` 已對 `/index.html`、`/admin.html`、`/kitchen.html`、`/demo/**`、`/api/**` 設為 `permitAll`，瀏覽靜態頁**不必登入**。  
> 使用 `/demo/student.html` 等呼叫 API 時，仍建議帶 Basic Auth 以符合 `@PreAuthorize` 設計。

---

## 2. Demo 前台頁面（主要操作流程）

| 角色 | 頁面 | URL |
|------|------|-----|
| STUDENT | 點餐主頁 | `/index.html` |
| TEACHER | 點餐主頁 | `/index.html?role=teacher` |
| KITCHEN_USER | Kitchen 後台 | `/kitchen.html` |
| ADMIN_USER | Admin 後台 | `/admin.html` |
| 導覽 | 角色選擇 | `/demo-role-board.html` |

資料透過瀏覽器 **localStorage** 共用（同一 `localhost:8080` 同源），詳見 `canteen-store.js`。

---

## 3. STUDENT / TEACHER

### Demo 頁（`index.html`）可做

- 查看 Admin 發布後的 **今日 Dish / Drink**（正方形縮圖、每列 4 個）
- 價格與 **尚餘** 庫存字級加大顯示
- **Special Request**：僅能勾選 Admin 設定的選項，不可改選項文字
- 加入購物車 → 預覽 → **確認送出訂單**
- 送出後 **扣減 Dish balance**；售完顯示 SOLD_OUT
- 右側 **我的訂單**：顯示今日訂單；Kitchen 完成後顯示綠色提示與「廚房已完成您的訂單…」

### 飲品計價（前台邏輯）

- 訂單內 **有 Dish**：依加入順序，最多 **免費杯數 = Dish 份數**
- **飲品數 > Dish 數**：多出的飲品標示 **單點**，需付費
- **只點飲品、無 Dish**：全部飲品付費

### 訂購時段

- 設定與後端相同：**11:00–14:30**（`Asia/Hong_Kong`）
- 前端 `CANTEEN_ORDER_WINDOW.enforced`：
  - `false`（目前）：測試用，全天可點
  - `true`：時段外按「加入」會提示不可訂購

### REST API（整合用）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/dishes/today` | 今日可訂 Dish（時段內） |
| GET | `/api/drinks/today` | 今日可訂 Drink |
| GET | `/api/menus/today` | 今日 Menu |

---

## 4. KITCHEN_USER

### Demo 頁（`kitchen.html`）可做

| 區塊 | 功能 |
|------|------|
| **今日訂單列表** | 顯示今日所有訂單；**完成訂單** → 狀態 `COMPLETED`，並通知 Student/Teacher「我的訂單」 |
| **備貨量確認** | 各 Dish **尚餘** 庫存（唯讀快覽） |
| **今日銷售報告** | 總單數、已完成、待處理、營業額、最熱賣 Dish、SOLD_OUT 數 |
| **Dish 菜單管理** | 可改：名稱、**庫存**、Special Request；可 **刪除** Dish（新增 Dish 僅 Admin）；**儲存全部資料** 同步前台 |
| | **不可**改價格、**不可**上傳照片（由 Admin 負責） |

### REST API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/dishes/{id}/balance` | 查庫存 |
| PATCH | `/api/dishes/{id}/balance` | 更新庫存 |
| PATCH | `/api/dishes/{id}/special-request-options` | 更新 Special Request |
| GET | `/api/dishes/{id}` | 單筆 Dish |
| GET | `/api/dishes?date=` | 依日期列表 |

更新 balance 後：`balance <= 0` → 自動 `SOLD_OUT`；`balance > 0` → `ON_LIST`。

---

## 5. ADMIN_USER

### Demo 頁（`admin.html`）可做

- **Dish / Drink** 完整表格：名稱、價格、庫存（Dish）、Special Request、**照片上傳**、刪除
- **新增** Dish / Drink
- **儲存全部資料**：`syncToFrontend()` 清空後全量寫入 Student / Teacher 已發布菜單
- 未重選照片時保留 `imageStorageKey`，不會被預設圖覆蓋

### REST API（摘要）

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/dishes` | 建立 Dish（multipart + 圖片） |
| PUT | `/api/dishes/{id}` | 更新 Dish |
| DELETE | `/api/dishes/{id}` | 刪除 |
| PATCH | `/api/dishes/{id}/basic-info` | 名稱、價格 |
| POST | `/api/dishes/{id}/image` | 上傳圖片 |
| PATCH | `/api/dishes/{id}/balance` | 庫存 |
| PATCH | `/api/dishes/{id}/special-request-options` | Special Request |

Drink、Menu 見 `DrinkController`、`MenuController`（結構類似）。

---

## 6. 狀態規則（全系統一致）

- `balance > 0` → **ON_LIST**（可訂）
- `balance <= 0` → **SOLD_OUT**（不可訂）
- Drink 在 Demo 前台 **無庫存上限**（不扣 balance）
- 後端 `OrderValidationService` 在下單 API 整合時會再驗證，防止超賣

---

## 7. 建議日常營運流程

1. **Admin**（`admin.html`）建立 / 調整今日 Dish、Drink、照片、價格、初始庫存 → **儲存全部資料**
2. **11:00** 起 **Student / Teacher**（`index.html`）點餐下單
3. **Kitchen**（`kitchen.html`）處理訂單 → **完成訂單**；必要時調整 Dish **庫存** 或 Special Request
4. 庫存歸零自動 **SOLD_OUT**；Student 頁面即時反映 **尚餘** 數量

---

## 8. 給同事的重點話術

- 「**ON_LIST** 才可下單；**SOLD_OUT** 由庫存自動計算。」
- 「廚房在 **kitchen.html** 改 **庫存** 即可；不必手改狀態。」
- 「管理員改 **價格與照片**；廚房改 **庫存與出餐**；學生老師 **只能點餐**。」
- 「完成訂單後，學生在手機點餐頁 **我的訂單** 會看到已完成訊息。」
- 「有主餐時，飲品可免費份數 = 主餐份數；多點的飲品要加錢。」

---

## 9. 獨立前端專案

**canteen-frontend**（React + Vite）與本 repo **不會自動同步**。若以 React 上線，需自行呼叫本專案 `/api/**` 並實作與 `canteen-store.js` 相同的業務規則（或改為純後端狀態）。
