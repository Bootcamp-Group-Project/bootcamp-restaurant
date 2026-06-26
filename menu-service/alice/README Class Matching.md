# Class / 檔案對照表

本文件對照 **demo_canteen** 專案目前的程式結構（Spring Boot 後端 + 靜態 Demo 前台）。

---

## 1. 核心實體（Entity）

| Class | 路徑 | 說明 |
|-------|------|------|
| `Dish` | `entity/Dish.java` | 菜品：名稱、價格、庫存 balance、圖片 URL、Special Request 選項、狀態 |
| `Drink` | `entity/Drink.java` | 飲品：名稱、價格、balance、圖片、Special Request、狀態 |
| `Menu` | `entity/Menu.java` | 套餐：關聯多個 Dish / Drink、訂購時段、庫存 |
| `Status` | `entity/Status.java` | `ON_LIST`、`SOLD_OUT` |

---

## 2. 資料存取（Repository）

| Class | 路徑 |
|-------|------|
| `DishRepository` | `repository/DishRepository.java` |
| `DrinkRepository` | `repository/DrinkRepository.java` |
| `MenuRepository` | `repository/MenuRepository.java` |

---

## 3. 業務邏輯（Service）

| Class | 路徑 | 職責 |
|-------|------|------|
| `DishService` | `service/DishService.java` | Dish CRUD、庫存、基本資訊、Special Request、圖片 |
| `DrinkService` | `service/DrinkService.java` | Drink CRUD、圖片 |
| `MenuService` | `service/MenuService.java` | Menu CRUD、關聯 Dish/Drink、庫存 |
| `OrderWindowService` | `service/OrderWindowService.java` | 訂購時段 11:00–14:30（`Asia/Hong_Kong`） |
| `OrderValidationService` | `service/OrderValidationService.java` | 下單前庫存 / 狀態驗證 |
| `LocalImageService` | `service/LocalImageService.java` | 本機圖片上傳、縮放 600–800px |
| `ImageService` | `service/ImageService.java` | 圖片服務介面 |

---

## 4. API 入口（Controller）

| Class | 路徑 | Base path |
|-------|------|-----------|
| `DishController` | `controller/DishController.java` | `/api/dishes` |
| `DrinkController` | `controller/DrinkController.java` | `/api/drinks` |
| `MenuController` | `controller/MenuController.java` | `/api/menus` |
| `DemoPageController` | `controller/DemoPageController.java` | `/` → 導向 Demo |

---

## 5. DTO（請求 / 回應）

| Class | 用途 |
|-------|------|
| `DishRequest`, `DishResponse` | Dish 建立 / 更新 / 查詢 |
| `DrinkRequest`, `DrinkResponse` | Drink |
| `MenuRequest`, `MenuResponse` | Menu |
| `BalanceUpdateRequest` | 更新 balance |
| `DishBasicUpdateRequest` | 更新名稱、價格 |
| `DishBalanceResponse` | 查庫存回應 |
| `SpecialRequestOptionsUpdateRequest` | 更新 Special Request 選項（Dish） |
| `OrderItemRequest` | 下單項目（type: dish/drink、數量） |

---

## 6. 設定、安全、排程

| Class | 路徑 | 說明 |
|-------|------|------|
| `SecurityConfig` | `config/SecurityConfig.java` | In-memory 四角色；Demo 模式 `permitAll` 靜態頁與 `/api/**` |
| `CanteenProperties` | `config/CanteenProperties.java` | `canteen.*` 設定綁定 |
| `WebConfig` | `config/WebConfig.java` | Web / 靜態資源 |
| `DailyStockResetScheduler` | `scheduler/DailyStockResetScheduler.java` | 每日重置庫存 |
| `DemoCanteenApplication` | `DemoCanteenApplication.java` | 啟動類別 |

---

## 7. 工具與例外

| Class | 路徑 | 說明 |
|-------|------|------|
| `StockStatusUtil` | `util/StockStatusUtil.java` | `balance > 0` → `ON_LIST`，否則 `SOLD_OUT` |
| `GlobalExceptionHandler` | `exception/GlobalExceptionHandler.java` | 統一錯誤回應 |
| `ResourceNotFoundException` | `exception/ResourceNotFoundException.java` | 404 業務錯誤 |
| `OrderWindowClosedException` | `exception/OrderWindowClosedException.java` | 非訂購時段 |

---

## 8. 靜態 Demo 前台（與後端並存）

| 檔案 | 路徑 | 角色 / 用途 |
|------|------|-------------|
| `index.html` | `static/index.html` | STUDENT / TEACHER 點餐、購物車、我的訂單 |
| `admin.html` | `static/admin.html` | ADMIN：Dish + Drink 全欄位管理 |
| `kitchen.html` | `static/kitchen.html` | KITCHEN：訂單、銷售報告、Dish（無價格/照片欄） |
| `canteen-store.js` | `static/canteen-store.js` | 共用狀態、同步、訂單、時段、扣庫存 |
| `demo-role-board.html` | `static/demo-role-board.html` | 角色導覽 |
| `demo/*.html` | `static/demo/` | 舊版 API 測試頁（JSON 輸出） |

### `canteen-store.js` 主要函式

| 函式 | 說明 |
|------|------|
| `loadCanteenState` / `saveCanteenState` | 菜單工作區讀寫 |
| `syncToFrontend` / `saveCanteenStateAndSync` | 全量同步至 Student / Teacher |
| `loadFrontendMenu(role)` | 讀已發布菜單 |
| `persistMenuItemImage` / `resolveItemImage` | 圖片壓縮與顯示 |
| `appendOrder` / `completeOrder` / `getTodayOrders` | 訂單流程 |
| `deductStockForOrder` / `validateOrderStock` | 送出訂單扣 Dish 庫存 |
| `assertOrderWindowOpen` | 前端訂購時段（`enforced` 開關） |

### localStorage Keys

| Key | 內容 |
|-----|------|
| `canteen_demo_state_v1` | dishes、drinks、teacherPublished、studentPublished |
| `canteen_demo_images_v1` | 上傳圖片 base64 |
| `canteen_demo_orders_v1` | 訂單（PENDING / COMPLETED） |

---

## 9. 與 `canteen-frontend` 的關係

| 專案 | 技術 | 是否自動同步 |
|------|------|----------------|
| **demo_canteen**（本 repo） | Spring Boot + `static/*` | — |
| **canteen-frontend**（另目錄） | React + Vite | **否**，需自行對接 `/api/**` |
