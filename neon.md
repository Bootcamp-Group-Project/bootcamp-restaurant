# Neon Database Setup & Migration

## Quick Reference — Dump & Restore

Run these in **Git Bash**:

```bash
# 1. Dump from Zeabur PostgreSQL
docker run --rm postgres:18 pg_dump "postgresql://postgres:password@xxx.ap-east-1.aws.zeabur.com:12345/restaurant_db" --no-owner --no-acl > dump.sql

# 2. Restore to Neon
docker run --rm -i postgres:18 psql "postgresql://neondb_owner:password@ep-xxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require" < dump.sql

# 3. Verify
docker run --rm postgres:18 psql "postgresql://neondb_owner:password@ep-xxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require" -c "\\dt"
```

> No `$` prefix — that is the shell prompt, not part of the command.

---

## 1. Create Neon Database

1. Sign up at https://neon.tech
2. **New Project** → name: `canteen`
3. **Database name**: `restaurant_db`
4. Region: pick closest to Zeabur deployment (e.g. `aws-ap-southeast-1` for Singapore)
5. Go to **Dashboard → Connection Details** → switch to **JDBC** tab
6. Copy the connection string — looks like:
   ```
   jdbc:postgresql://ep-xxx-xxx.ap-southeast-1.aws.neon.tech/restaurant_db?sslmode=require
   ```
   Also note the **psql** connection string (needed for data import):
   ```
   postgresql://kim:password@ep-xxx-xxx.ap-southeast-1.aws.neon.tech/restaurant_db?sslmode=require
   ```

---

## 2. Set Zeabur Environment Variables

Zeabur Dashboard → your service → **Variables** → add:

| Key | Value |
|-----|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-xxx...neon.tech/restaurant_db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | your Neon username |
| `SPRING_DATASOURCE_PASSWORD` | your Neon password |

Redeploy → Spring Boot `ddl-auto: update` auto-creates all tables on first boot.

---

## 3. Dump Existing Data (from Zeabur)

Go to **Zeabur Dashboard → your PostgreSQL service → Connection** and copy the connection string. It looks like:

```
postgresql://postgres:password@xxx.ap-east-1.aws.zeabur.com:12345/restaurant_db
```

Then run (no local PostgreSQL needed — uses a temporary Docker container):

```powershell
docker run --rm postgres:18 pg_dump "postgresql://postgres:password@xxx.ap-east-1.aws.zeabur.com:12345/restaurant_db" --no-owner --no-acl > dump.sql
```

`dump.sql` will be created in your current directory.

---

## 4. Restore Data to Neon

### Option A — Docker (no local psql needed, recommended on Windows)

```powershell
# Use a temporary postgres container to push dump.sql into Neon
# Replace the URL with your actual Neon psql connection string
docker run --rm -i postgres:18 psql "postgresql://kim:password@ep-xxx.ap-southeast-1.aws.neon.tech/restaurant_db?sslmode=require" < dump.sql
```

### Option B — local psql (if already installed)

```powershell
psql "postgresql://kim:password@ep-xxx.ap-southeast-1.aws.neon.tech/restaurant_db?sslmode=require" < dump.sql
```

Check if psql is installed:
```powershell
psql --version
```

---

## 5. Verify

Connect to Neon and check tables exist.

**Git Bash (MINGW64) — use `\\dt` to avoid path conversion:**
```bash
docker run --rm postgres:18 psql "postgresql://neondb_owner:password@ep-xxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require" -c "\\dt"
```

**PowerShell:**
```powershell
docker run --rm postgres:18 psql "postgresql://neondb_owner:password@ep-xxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require" -c "\dt"
```

> Note: no `-it` flag — that requires `winpty` in Git Bash. `-c "..."` is non-interactive so it's not needed.

You should see all tables: `base_users`, `dishes`, `drinks`, `menus`, `orders`, `order_items`, `wallets`, `wallet_transactions`, etc.

---

## Memory Freed on Zeabur

| Before | After |
|--------|-------|
| postgres 400m + app 700m + nginx 50m | app 700m + nginx 50m |
| ~1.15 GB | ~750 MB |
| ~850 MB headroom | ~1.25 GB headroom |

---

## Local Dev (unchanged)

`docker-compose.override.yml` adds postgres back automatically when running locally:

```powershell
docker-compose up -d   # still works, postgres starts locally as before
```

I've deployed a restaurant-db service on Zeabur. Here are the instructions: 

## Connection String

postgresql://root:WAN3s09qKIUSGk2D51O4CwRamhE768Py@43.156.108.60:32682/zeabur

## PostgreSQL Connect Command

psql "postgresql://root:WAN3s09qKIUSGk2D51O4CwRamhE768Py@43.156.108.60:32682/zeabur"

## PostgreSQL username

root

## PostgresSQL password

WAN3s09qKIUSGk2D51O4CwRamhE768Py

## PostgresSQL database

zeabur

## PostgreSQL host

43.156.108.60

## PostgreSQL port

32682


