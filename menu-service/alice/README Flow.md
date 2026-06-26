# Canteen 業務流程說明

本專案包含 **Spring Boot 後端 API**（PostgreSQL + JPA）與 **靜態 Demo 前台**（`src/main/resources/static/`，以 `localStorage` 同步菜單與訂單）。  
啟動後預設：`http://localhost:8080`

---

## A. 業務流程（文字流程圖）

### 1. Admin 建立與發布菜單

1. Admin 開啟 `admin.html`，編輯 **Dish / Drink**（名稱、價格、庫存、Special Request 選項、照片）。
2. 可按 **新增 / 刪除** Dish 或 Drink。
3. 按 **「儲存全部資料」** → 呼叫 `saveCanteenStateAndSync()`：
   - 先清空 `teacherPublished` / `studentPublished`
   - 再寫入完整快照（含 `imageStorageKey`，圖片存於 `canteen_demo_images_v1`）
4. 未上傳新照片時，保留既有 `imageStorageKey`，不會被預設 Unsplash 圖覆蓋。

### 2. Student / Teacher 點餐（`index.html`）

1. 讀取已發布菜單：`loadFrontendMenu('student' | 'teacher')`（Teacher 用 `?role=teacher`）。
2. **訂購時段**（與後端一致）：`Asia/Hong_Kong` **11:00–14:30**（含起迄）。  
   - 前端 `canteen-store.js` → `CANTEEN_ORDER_WINDOW.enforced`  
   - 目前為 **`false`**（測試全天可點）；上線請改 **`true`**
3. 畫面：正方形圖、每列 4 格；價格與 **尚餘** 字級加大；Drink **不顯示**「無限」標籤。
4. 依 `balance` 顯示 **ON_LIST / SOLD_OUT**；Drink 無庫存上限。
5. 加入購物車 → 預覽 → **確認送出訂單**：
   - `validateOrderStock()` 檢查庫存
   - `deductStockForOrder()` 扣減 Dish `balance`
   - `appendOrder()` 寫入 `canteen_demo_orders_v1`（`PENDING`）
6. **飲品計價**：
   - 有 Dish：免費杯數 ≤ Dish 份數（依加入順序）
   - 飲品數 > Dish 數：超出部分 **單點** 付費
   - 僅飲品：全部付費
7. 右側 **我的訂單**：列出今日該角色訂單與狀態。

### 3. Kitchen 出餐與管理（`kitchen.html`）

1. **今日訂單列表**：顯示今日訂單；**完成訂單** → `completeOrder()`：
   - 狀態 `COMPLETED`
   - `statusMessage`：「廚房已完成您的訂單，請取餐。謝謝﹗」
   - Student/Teacher 分頁透過 `storage` / `canteen-store-updated` 更新 **我的訂單**
2. **備貨量確認**：各 Dish **尚餘**（唯讀）。
3. **今日銷售報告**：總單數、已完成、待處理、營業額、最熱賣 Dish、SOLD_OUT 品項數。
4. **Dish 菜單管理**（**無價格欄、無照片欄**）：
   - 可改：名稱、庫存、Special Request
   - 可刪除 Dish（新增 Dish 僅 Admin）
   - **儲存全部資料** → 同步 Student / Teacher（不覆蓋 Admin 設定的價格與照片）

### 4. 狀態同步規則

- `balance > 0` → `ON_LIST`
- `balance <= 0` → `SOLD_OUT`（`StockStatusUtil` / `refreshDishStatuses`）
- 後端 `OrderValidationService` 於 API 下單時可再驗證（防超賣）

---

## B. Demo 前台頁面

| 頁面 | URL | 角色 |
|------|-----|------|
| 點餐主頁 | `/index.html`（Teacher：`?role=teacher`） | STUDENT / TEACHER |
| Admin | `/admin.html` | ADMIN_USER |
| Kitchen | `/kitchen.html` | KITCHEN_USER |
| 角色選擇 | `/demo-role-board.html` | 導覽 |
| API 測試（舊） | `/demo/student.html` 等 | 僅輸出 JSON，非主流程 |

### 共用模組 `canteen-store.js`

| Key / 函式 | 用途 |
|------------|------|
| `canteen_demo_state_v1` | 菜單工作區 + `teacherPublished` / `studentPublished` |
| `canteen_demo_images_v1` | 上傳圖片（壓縮最寬 800px） |
| `canteen_demo_orders_v1` | 訂單（PENDING / COMPLETED） |
| `syncToFrontend()` | 清空後全量同步至 Student/Teacher |
| `saveCanteenStateAndSync()` | Admin/Kitchen 儲存並同步 |
| `deductStockForOrder()` | 送出訂單扣 Dish 庫存 |
| `completeOrder()` | Kitchen 完成訂單 + 通知訊息 |
| `getTodayOrdersForRole()` | 依角色篩選今日訂單 |
| `assertOrderWindowOpen()` | 訂購時段檢查（`enforced` 開關） |

---

## C. 主要 REST API（後端）

> Demo 模式：`SecurityConfig` 對靜態頁與 `/api/**` 為 `permitAll`；Controller 仍保留 `@PreAuthorize` 供正式環境。

### Student / Teacher

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/dishes/today` | 今日可訂 Dish（訂購時段內） |
| GET | `/api/drinks/today` | 今日可訂 Drink |
| GET | `/api/menus/today` | 今日 Menu |

### Kitchen_User

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/dishes/{id}/balance` | 查庫存 |
| PATCH | `/api/dishes/{id}/balance` | 改庫存 |
| PATCH | `/api/dishes/{id}/special-request-options` | 改 Special Request |
| GET | `/api/dishes/{id}` | 單筆 Dish |
| GET | `/api/dishes?date=` | 依日期列表 |

### Admin_User

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/dishes` | 建立 Dish（multipart + 圖片） |
| PUT | `/api/dishes/{id}` | 更新 Dish |
| DELETE | `/api/dishes/{id}` | 刪除 Dish |
| PATCH | `/api/dishes/{id}/basic-info` | 名稱、價格 |
| POST | `/api/dishes/{id}/image` | 上傳圖片 |
| PATCH | `/api/dishes/{id}/special-request-options` | Special Request |
| PATCH | `/api/dishes/{id}/balance` | 庫存 |

Drink / Menu：見 `DrinkController`、`MenuController`（CRUD、`/today`、圖片等）。

---

## D. 下單驗證邏輯（後端）

`OrderValidationService`：

- `validateDishAvailable` / `validateDrinkAvailable` / `validateOrderItems`
- 數量必須 > 0
- `SOLD_OUT` 不可下單
- `balance < quantity` 不可下單
- 項目 type 須為 `dish` 或 `drink`
- ID 不存在 → `ResourceNotFoundException`

`OrderWindowService`：非 11:00–14:30（`Asia/Hong_Kong`）呼叫 `/today` 等會拋 `OrderWindowClosedException`。

---

## E. 設定檔 `application.yml` 摘要

| 項目 | 值 |
|------|-----|
| 資料庫 | PostgreSQL `jdbc:postgresql://localhost:5432/canteen_db` |
| 訂購時段 | `11:00` – `14:30`，`Asia/Hong_Kong` |
| 圖片目錄 | `uploads/menu`，縮放 600–800px（`LocalImageService`） |
| 排程 | `DailyStockResetScheduler` 每日重置庫存 |
| 埠 | `8080` |

---

## F. 端到端 Demo 測試路徑（建議）

1. `admin.html` → 編輯菜單、上傳圖 → **儲存全部資料**
2. `index.html` → 下單 → 確認 **尚餘** 減少、**我的訂單** 出現
3. `kitchen.html`（另一分頁）→ **完成訂單** → 回 `index.html` 見已完成訊息
4. `kitchen.html` → 調整 Dish 庫存 → **儲存全部資料**

---

## G. 與 `canteen-frontend` 的關係

- **demo_canteen**：本 repo，Spring Boot + 內建靜態 Demo（目前功能主體）。
- **canteen-frontend**：另目錄 React + Vite，**不會自動同步**；若要以 React 上線需自行接 API 與狀態邏輯。
