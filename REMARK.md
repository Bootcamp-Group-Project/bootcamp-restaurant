# 新手指南：Spring Boot 系統建立流程

## 一、整體架構概念

HTTP 請求從前端進來，經過以下層級處理，再把結果回傳：

```
前端 (瀏覽器)
    ↓ HTTP Request
[ Controller 層 ]  → 接收請求，呼叫 Service
[ Service 層    ]  → 執行業務邏輯（驗證、計算）
[ Repository 層 ]  → 對資料庫進行讀寫
[ Entity 層     ]  → 對應資料庫的表格結構
    ↓
資料庫 (PostgreSQL)
    ↑
[ Mapper        ]  → Entity 轉成 DTO 再回傳給前端
```

---

## 二、各層說明（以 Wallet 為例）

### 1. `entity/` — 資料庫表格對應

**檔案：** `WalletEntity.java`

每一個 Entity 類就是資料庫裡的一張表格。每個欄位就是表格的一個 Column。

- `@Entity` → 告訴 Spring 這個類對應一張資料庫表格
- `@Table(name = "wallets")` → 指定表格名稱
- `@Id` + `@GeneratedValue` → 主鍵，由資料庫自動遞增
- `@Column` → 可指定欄位名稱、長度、精度等
- `@ManyToOne` → 多對一關聯（例如：多筆交易屬於同一個錢包）
- `@JoinColumn` → 指定外鍵欄位名稱

**Lombok 注解（自動生成程式碼）：**

| 注解 | 自動幫你生成 |
|------|------------|
| `@Getter` | 所有 `getXxx()` 方法 |
| `@Setter` | 所有 `setXxx()` 方法 |
| `@NoArgsConstructor` | 無參數建構子（JPA 必須有） |
| `@AllArgsConstructor` | 所有欄位的建構子 |
| `@Builder` | 鏈式建立物件：`WalletEntity.builder().walletId(1L).build()` |

---

### 2. `repository/` — 資料庫操作

**檔案：** `WalletRepository.java`

繼承 `JpaRepository` 後，以下方法全部自動生成，**完全不需要寫 SQL**：

| 方法 | 對應 SQL |
|------|---------|
| `findById(id)` | `SELECT * WHERE wallet_id = ?` |
| `findAll()` | `SELECT * FROM wallets` |
| `save(entity)` | `INSERT` 或 `UPDATE` |
| `deleteById(id)` | `DELETE WHERE wallet_id = ?` |

**命名查詢（自訂方法）：**
```java
Optional<WalletEntity> findByUserId(Long userId);
// 自動生成：SELECT * FROM wallets WHERE user_id = ?
```
方法名稱遵守 `findBy + 欄位名稱` 規則，Spring 自動解析成 SQL。

`Optional<>` 表示結果可能為空（找不到資料），強制開發者處理 null 的情況。

---

### 3. `model/` — 請求格式（Request）

**檔案：** `WalletAdjustReq.java`

`record` 是 Java 14+ 的語法，用來定義**不可變的資料容器**，適合作為 API 的請求物件。
前端送來的 JSON 會自動對應到這個 record 的欄位。

```java
// 前端送來的 JSON：
// { "adminId": 1, "amount": 200, "description": "top-up", "idempotencyKey": "key-001" }
public record WalletAdjustReq(Long adminId, BigDecimal amount, String description, String idempotencyKey) {}
```

---

### 4. `dto/` — 回應格式（Response）

**檔案：** `WalletResp.java`

DTO (Data Transfer Object) 是回傳給前端的資料格式。
**不直接回傳 Entity**，原因：
- Entity 可能有敏感欄位不應該暴露
- Entity 結構跟前端需要的格式不一定相同

```
Entity（完整資料庫欄位）  →  Mapper  →  DTO（只有前端需要的欄位）
```

---

### 5. `mapper/` — 資料轉換

**檔案：** `WalletMapper.java`

負責把 Entity 轉換成 DTO。兩者欄位不一定相同，由 Mapper 逐一對應。

```java
// Entity → DTO 轉換
public WalletResp map(WalletEntity entity) {
    return WalletResp.builder()
            .walletId(entity.getWalletId())
            .userId(entity.getUserId())
            .balance(entity.getBalance())
            .build();
}
```

`@Component` 讓 Spring 管理這個物件，可以在其他類用 `@Autowired` 注入。

---

### 6. `service/` — 業務邏輯介面

**檔案：** `WalletService.java`

Interface 只定義「有哪些功能」，不寫實作。

**為什麼要用 Interface？**
- 可以有多種實作（例如：正式版、測試版）
- 測試時可以用 Mock 替換，不需要真實資料庫
- 降低各層之間的耦合

---

### 7. `service/impl/` — 業務邏輯實作

**檔案：** `WalletServiceImpl.java`

實作 Service Interface，在這裡寫真正的業務邏輯：
- 參數驗證（amount 必須大於 0）
- 查詢資料庫（呼叫 Repository）
- 計算（加減餘額）
- 拋出例外（餘額不足）

`@Service` → 讓 Spring 管理，標示這是業務邏輯層。
`@Transactional` → 確保資料庫操作全部成功或全部失敗（不會只存一半）。
`@Autowired` → 讓 Spring 自動注入依賴的物件（不需要 `new`）。

---

### 8. `controller/` — API 端點介面

**檔案：** `WalletOperation.java`

同樣是 Interface，定義有哪些 API 端點。

---

### 9. `controller/impl/` — API 端點實作

**檔案：** `WalletController.java`

接收 HTTP 請求，呼叫 Service，回傳結果。

| 注解 | 說明 |
|------|------|
| `@RestController` | 這是 REST API Controller，回傳 JSON |
| `@GetMapping("/path")` | 處理 HTTP GET 請求 |
| `@PostMapping("/path")` | 處理 HTTP POST 請求 |
| `@PathVariable` | 從 URL 路徑取值，例如 `/wallets/{walletId}` 中的 `walletId` |
| `@RequestBody` | 從 HTTP Body 取 JSON，自動轉成 Java 物件 |

---

### 10. `exception/` — 例外處理

**檔案：** `GlobalExceptionHandler.java` / `ResourceNotFoundException.java`

`@RestControllerAdvice` 讓這個類攔截**所有 Controller 拋出的例外**，統一轉成 JSON 格式的錯誤回應。

```
Service 拋出 ResourceNotFoundException
    ↓
GlobalExceptionHandler 攔截
    ↓
回傳 { "status": 404, "message": "...", "timestamp": "..." }
```

| 例外類型 | HTTP 狀態碼 | 使用時機 |
|---------|-----------|---------|
| `ResourceNotFoundException` | 404 | 找不到資料（例如：walletId 不存在） |
| `IllegalArgumentException` | 400 | 請求參數不合法（例如：amount <= 0） |
| `RuntimeException` | 500 | 其他未預期的錯誤 |

---

## 三、建立新功能的標準流程

每次新增一個功能（例如新增「退款」功能），都按照以下順序建立：

```
1. enums/         → 新增需要的列舉值（如 TransactionType.REFUND）
2. entity/        → 確認資料庫欄位夠用，不夠就新增
3. repository/    → 新增需要的查詢方法
4. model/         → 新增請求格式（Request record）
5. dto/           → 新增回應格式（Response class）
6. mapper/        → 新增 Entity → DTO 的轉換方法
7. service/       → 在 Interface 新增方法簽名
8. service/impl/  → 實作業務邏輯
9. controller/    → 在 Interface 新增端點簽名
10. controller/impl/ → 實作 HTTP 端點
```

---

## 四、跨系統 FK 規則

本系統只負責 **System 4（Wallet & Order）**。
其他系統（User、Menu 等）的表格由其他開發者負責。

**規則：跨系統只存 Long ID，不用 `@ManyToOne`**

```java
// ✅ 正確：只存 userId（Long），不關聯 UserEntity
private Long userId;

// ❌ 錯誤：不能 @ManyToOne UserEntity，因為 UserEntity 不在本模組
@ManyToOne
private UserEntity user;
```

**同系統內的 FK 才用 `@ManyToOne`：**
```java
// WalletTransactionEntity → WalletEntity（同屬 System 4，可以關聯）
@ManyToOne
@JoinColumn(name = "wallet_id")
private WalletEntity walletEntity;
```

---

## 五、常用指令

```bash
# 進入 backend 目錄
cd backend

# 編譯 + 執行所有測試
mvn test

# 執行單一測試類
mvn test -Dtest=WalletServiceTest

# 執行單一測試方法
mvn test -Dtest=WalletServiceTest#recharge_whenValid_shouldIncreaseBalance

# 啟動後端伺服器
mvn spring-boot:run
```
