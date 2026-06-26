# EC2 部署學習指南

## 1. Git SSH Key

SSH Key 讓你 `git clone` / `git push` 時不需要每次輸入密碼。

### 產生金鑰（在任何電腦上執行）
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
# 按 Enter 三次（接受預設值，不設密碼）
```
會產生兩個檔案：
- `~/.ssh/id_ed25519` — **私鑰**（絕對不要分享）
- `~/.ssh/id_ed25519.pub` — **公鑰**（給 GitHub / EC2 用）

### 加入 GitHub
```bash
cat ~/.ssh/id_ed25519.pub   # 複製輸出內容
```
GitHub → Settings → SSH and GPG keys → **New SSH key** → 貼上 → 儲存

### 加入 EC2（這樣 SSH 登入不需要密碼）
```bash
# 在 Windows 上，把公鑰複製到 EC2
ssh-copy-id -i ~/.ssh/id_ed25519.pub ubuntu@EC2_IP

# 或手動：把公鑰貼入 EC2 的 authorized_keys
cat ~/.ssh/id_ed25519.pub | ssh ubuntu@EC2_IP "cat >> ~/.ssh/authorized_keys"
```

### 用 SSH 方式 clone（設定好金鑰後）
```bash
git clone git@github.com:USERNAME/REPO.git
```

### 為什麼 EC2 上 SSH clone 會失敗
EC2 沒有設定 GitHub 的 SSH Key。  
解決方法：把 EC2 的公鑰加到 GitHub，或改用 **HTTPS clone**：
```bash
git clone https://github.com/USERNAME/REPO.git
```

---

## 2. Docker

### 基本概念
| 名詞 | 說明 |
|------|------|
| **Image（映像檔）** | 藍圖 — 建一次，到處用 |
| **Container（容器）** | Image 的執行實例 |
| **Dockerfile** | 建立 Image 的食譜 |
| **Docker Hub** | 公開的 Image 倉庫 |
| **docker compose** | 同時管理多個 Container |

### 在本機建立 Image
```powershell
# 格式：docker build -t 帳號/映像名稱 ./來源資料夾
docker build -t kimcheung20212/canteen-app ./restaurant-app
```
- `-t` = 為 Image 命名（tag）
- `./restaurant-app` = 包含 Dockerfile 的資料夾

### 推送到 Docker Hub
```powershell
docker login                                   # 登入一次
docker push kimcheung20212/canteen-app         # 上傳 Image
```

### 在 EC2 拉取 Image
```bash
docker pull kimcheung20212/canteen-app         # 下載 Image
```
使用 `docker compose` 時會自動拉取。

### Docker Compose 常用指令
```bash
docker compose up -d          # 在背景啟動所有服務
docker compose down           # 停止所有服務
docker compose ps             # 查看狀態
docker compose logs -f        # 即時查看 log
docker compose pull           # 拉取最新 Image
docker compose up -d --build  # 重新建立並重啟（程式碼有改動時）
```

### 更新 EC2 上的應用程式（推送新 Image 後）
```bash
docker compose pull           # 取得新 Image
docker compose up -d          # 用新 Image 重啟
```

### 其他常用指令
```bash
docker images                        # 列出所有本地 Image
docker ps                            # 列出執行中的 Container
docker logs CONTAINER_NAME           # 查看 log
docker exec -it CONTAINER_NAME bash  # 進入 Container 的 shell
```

---

## 3. Cyberduck

Cyberduck 是圖形介面的檔案傳輸工具，用來透過 SFTP 上傳檔案到 EC2。

### 連線到 EC2
1. 開啟 Cyberduck → **Open Connection**（左上角）
2. 填入：
   - Protocol：**SFTP (SSH File Transfer Protocol)**
   - Server：`EC2 公開 IP`
   - Port：`22`
   - Username：`ubuntu`  ← Ubuntu EC2 預設帳號
   - SSH Private Key：點選下拉 → 選取 `canteen.pem` 檔案
3. 點 **Connect**

### 傳輸檔案
- 從 Windows 檔案總管拖曳檔案到 Cyberduck 視窗
- 或右鍵 → Upload

### 部署到 EC2 需要上傳的檔案
```
/home/ubuntu/canteen/
  docker-compose.yml
  .env                  ← 上傳前先填入機密值
  nginx/
    nginx.conf
```

### 從 Cyberduck 開啟終端機
連線後：**Go** 選單 → **Open in Terminal**  
這樣會直接 SSH 進入 EC2。

### .pem 金鑰檔案注意事項
- 把 `canteen.pem` 放在安全的地方（例如 `D:\AWS\canteen.pem`）
- Windows 可能會抱怨權限問題，用以下指令修正：
  ```powershell
  icacls "D:\AWS\canteen.pem" /inheritance:r /grant:r "$($env:USERNAME):(R)"
  ```
- 絕對不要把 `.pem` 檔案 commit 到 Git

---

## 4. Zeabur Proxy 設定（保留 canteen.zeabur.app 網域 → EC2）

目標：讓 `canteen.zeabur.app` 繼續可用，做法是在 Zeabur 部署一個輕量 nginx 代理，把所有流量轉發到 EC2。

### 需要的檔案（已在 repo 中）
```
zeabur-proxy/
  Dockerfile              ← 建立含 envsubst 的 nginx:alpine
  nginx.conf.template     ← 代理設定，用 ${EC2_IP} 作為佔位符
```

### 步驟一 — 在 Zeabur 建立新服務
1. Zeabur 控制台 → 你的專案 → **Add Service** → **Git**
2. 選擇 `bootcamp-restaurant` 這個 repo
3. **Root Directory** 填入：`zeabur-proxy`
4. Zeabur 會自動偵測 Dockerfile 並建立

### 步驟二 — 設定環境變數
在新服務的 **Variables** 頁籤 → 新增：
```
EC2_IP = 54.92.206.8
```
**在第一次部署前先設定**（或設定後重新部署）。

### 步驟三 — 修正 Port（重要！）
Zeabur 預設 Port 是 `8080`，但 nginx 監聽的是 `80`，這會導致 502 錯誤。

**Networking** 頁籤 → 把 exposed port 從 `8080` 改成 `80` → 儲存。  
Zeabur 會自動重新部署。

### 步驟四 — 指定網域
1. 新代理服務的 **Networking** 頁籤 → **Add Domain**
2. 加入 `canteen.zeabur.app`（如需要，先從舊的 canteen 服務移除）

### 步驟五 — 停止舊的 Zeabur canteen 服務
確認代理正常運作後，到舊的 canteen 服務 → **Suspend** 或 **Delete**。

### 原理說明
```
使用者瀏覽器
    ↓ HTTPS
canteen.zeabur.app  （Zeabur 代理 — nginx:alpine，約 26 MB，幾乎免費）
    ↓ HTTP
http://54.92.206.8  （EC2 nginx → Spring Boot :8080）
    ↓
Neon PostgreSQL  （外部資料庫，不需更動）
```

---

## 完整部署流程（總覽）

```
Windows 本機                     Docker Hub                EC2
-----------                      ----------                ---
docker build → image             
docker push  ────────────────→   儲存 image
                                                           docker compose up
                                                           docker pull ←── 拉取 image
                                                           容器啟動 ✓

Cyberduck 上傳 ───────────────────────────────────────→  docker-compose.yml
                                                           nginx/nginx.conf
                                                           .env
```

```
使用者瀏覽器
    ↓ https
canteen.zeabur.app  （Zeabur 代理 — 保留網域 + SSL）
    ↓ http
EC2 公開 IP:80  （nginx → Spring Boot）
    ↓
Neon PostgreSQL  （外部資料庫，不需更動）
```
