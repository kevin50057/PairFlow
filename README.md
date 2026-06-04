# PairFlow

> **讓情侶不只是聊天，而是一起生活、一起記錄、一起完成事情。**
> A **Couple OS / Relationship Workspace** — not another chat app, but a private space where
> couples plan, remember, and get things done together.

PairFlow is a full-stack couples app: a couple-only **Todo list**, **shared calendar**,
**anniversaries / countdowns**, **mood check-ins**, **timed-unlock letters**, a **private album**
with "去年今天" memories, **daily questions**, **wishlist**, **lite expense tracking**,
**date planning** (vote → auto-schedule), a gentle **conflict-repair** flow, an **AI assistant**,
and **notifications** — all behind a strict one-couple-per-user privacy model, surfaced through an
aggregated **daily home dashboard**.

---

## 🚀 怎麼啟動 / 怎麼使用（繁中）

### 最快：一鍵啟動
在專案根目錄執行：
```bash
./start.sh
```
它會自動依序啟動 **PostgreSQL → 後端 (:8080) → 前端 (:4200)**，完成後印出網址。
打開瀏覽器到 **http://localhost:4200** 就能用了。
要結束就在這個終端機按 **Ctrl + C**（或另開一個視窗執行 `./stop.sh`）。

### 登入（內建示範帳號）
| 欄位 | 值 |
|---|---|
| 帳號 | `kevin@pairflow.test` |
| 密碼 | `secret123` |

另一半是「**魚丸**」。你也可以自己註冊兩個帳號，用邀請碼把它們配成一對。

### 怎麼用（五個分頁）
- **首頁**：今日任務 / 行程 / 對方心情 / 下一個紀念日，一眼看完。
- **任務**：兩人的共同待辦；「**想到再做**」分頁＝不需要時間的 to-do list。
- **行事曆**：共同行程。
- **回憶**：相簿 +「去年今天」。
- **我們**：心情打卡、小紙條／未來信、**每日問題**、**未來一起做的事**（願望清單）、記帳、約會投票、和好模式、通知設定。

> 每天第一次進來會跳出「**今日問題**」，兩人都回答後才會解鎖看到對方的答案 💌

### 第一次需要先裝的工具（只做一次）
```bash
brew install openjdk@21 maven node postgresql@16
```
首次執行 `./start.sh` 時會自動幫你建立資料庫與帳號；若失敗，請改用下方「手動啟動」。

### 手動啟動（想分開看三個服務時）
詳見下方英文版 **Quick start**：開三個終端機分別跑 PostgreSQL、`cd backend && mvn spring-boot:run`、`cd frontend && npm start`。

### 沒有 PostgreSQL？用免安裝的 H2
```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 疑難排解
- 後端起不來 → 看 `backend/.run.log`；前端起不來 → 看 `frontend/.run.log`。
- macOS 若 Postgres 出現 `postmaster became multithreaded` → 用
  `LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 brew services restart postgresql@16`。
- Port 8080 / 4200 被占用 → 先 `./stop.sh` 再 `./start.sh`。

---

## Tech stack

| Layer | Choice |
|---|---|
| Frontend | **Angular 21** (standalone components, signals, lazy routes) · mobile-first SCSS theme |
| Backend | **Java 21 · Spring Boot 3.4** (Web · Security · Data JPA · Validation) |
| Auth | **JWT** (HS256, `jjwt`) · BCrypt · stateless · HTTP interceptor on the client |
| DB | **PostgreSQL 16 + Flyway** migrations (`ddl-auto=validate`). **H2** retained as a zero-setup fallback profile |
| AI | Pluggable provider — deterministic **stub** by default, **Anthropic Claude** when configured |
| Build | Maven (API) · Angular CLI / esbuild (web) |

Schema stays identical across H2 (dev) and Postgres (prod): UUID string PKs, enums via
`@Enumerated(STRING)`, `ddl-auto: update` for dev (swap to Flyway + `validate` for prod).

---

## Layout

```
pairflow/
├─ backend/                          # Spring Boot API
│  └─ src/main/java/com/pairflow/
│     ├─ common/ · config/           # BaseEntity, unified errors, request-id, AppTime · JWT + Security + CORS
│     ├─ auth/ · user/               # register/login/me, profile
│     ├─ couple/                     # invite-code pairing, breakup + CoupleContext membership guard
│     ├─ todo/                       # tasks, checklist, comments, surprise-task visibility, goals
│     ├─ anniversary/ · event/       # repeat + countdown · shared calendar
│     ├─ mood/ · note/               # daily mood + reactions · notes / timed-unlock letters
│     ├─ album/ · memory/            # photos + media upload + "去年今天" (MemoryProvider seam)
│     ├─ question/ · wishlist/       # daily Q dual-answer unlock · wishes (→ todo)
│     ├─ finance/ · dateplan/        # lite expenses · date voting → auto Event + prep Todos
│     ├─ repair/ · ai/               # conflict-repair + safety guard · stub/Anthropic provider
│     ├─ notification/               # in-app feed + per-type prefs + FCM-ready push
│     └─ home/                       # aggregated daily dashboard (GET /api/home)
└─ frontend/                         # Angular 21
   └─ src/app/
      ├─ core/                       # api, auth, couple store, guards, JWT interceptor, models, labels
      └─ pages/                      # auth (login/register/onboarding), shell (bottom tabs),
                                     #   home, todos, calendar, memories, us + 10 "us" sub-pages
```

---

## Quick start

**Prerequisites:** JDK 21 + Maven, Node ≥ 20, and PostgreSQL 16.

```bash
# 0) PostgreSQL (macOS / Homebrew — no Docker needed)
brew install postgresql@16 && brew services start postgresql@16
createdb pairflow
psql -d pairflow -c "CREATE ROLE pairflow LOGIN PASSWORD 'pairflow'; \
  ALTER DATABASE pairflow OWNER TO pairflow; ALTER SCHEMA public OWNER TO pairflow;"

# 1) API  → http://localhost:8080   (Flyway runs the migrations on boot)
cd backend && mvn spring-boot:run

# 2) Web  → http://localhost:4200   (in another terminal)
cd frontend && npm install && npm start
```

Open **http://localhost:4200** and sign in with the seeded demo account
**kevin@pairflow.test / secret123** (or register two accounts and pair them with an invite code).

**No Postgres handy?** Run the API on the zero-setup **H2** fallback profile instead:
```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=h2   # H2 console at /h2-console
```

### Optional: real AI
By default the AI features use an offline stub. To use Claude:
```bash
export PAIRFLOW_AI_PROVIDER=anthropic
export ANTHROPIC_API_KEY=sk-ant-...
```

---

## API overview (base `/api`)

🔒 = requires `Authorization: Bearer <token>`.

| Group | Endpoints |
|---|---|
| Auth | `POST /auth/register` · `/auth/login` · 🔒`/auth/me` · 🔒`/auth/logout` · 🔒`PATCH /users/me` |
| Couple | 🔒`POST /couples/invite` · `/couples/join` · `GET /couples/me` · `PATCH /couples/{id}` · `POST /couples/{id}/breakup` |
| Todo | 🔒`GET/POST /todos` · `GET/PATCH/DELETE /todos/{id}` · `POST /todos/{id}/complete` · `/comments` · `/checklist` |
| Anniversary | 🔒`GET/POST /anniversaries` · `PATCH/DELETE /anniversaries/{id}` |
| Calendar | 🔒`GET/POST /events` · `GET/PATCH/DELETE /events/{id}` |
| Mood | 🔒`GET /moods/today` · `POST /moods` · `GET /moods/history` · `POST /moods/{id}/reactions` |
| Notes | 🔒`GET/POST /notes` · `GET /notes/{id}` · `POST /notes/{id}/read` · `/favorite` |
| Album | 🔒`GET/POST /albums` · `POST /photos` (multipart) · `GET /photos` · `GET /media/{file}` |
| Questions | 🔒`GET /questions/today` · `POST /questions/today/answer` · `GET /questions/history` · `/{id}/favorite` |
| Wishlist | 🔒`GET/POST /wishes` · `PATCH/DELETE /wishes/{id}` · `POST /wishes/{id}/complete` · `/to-todo` |
| Finance | 🔒`GET/POST /expenses` · `GET /expenses/summary` · `PATCH/DELETE /expenses/{id}` |
| Date Planner | 🔒`GET/POST /date-plans` · `/{id}/candidates` · `/candidates/{cid}/vote` · `/{id}/finalize` |
| Repair | 🔒`GET/POST /repair` · `/{id}/send` · `/{id}/respond` · `/{id}/follow-ups` |
| AI | 🔒`POST /ai/todo-breakdown` · `/date-suggestions` · `/anniversary-message` · `/soften` · `GET /ai/nudges` |
| Notifications | 🔒`GET /notifications` · `/unread-count` · `/{id}/read` · `/read-all` · `GET/PUT /preferences` |
| Home | 🔒`GET /home` — aggregated dashboard |

Every couple-scoped request is authorized through `CoupleContext`, which resolves the caller's active
couple and rejects anyone outside it. All errors share one shape `{ code, message, details[] }`;
every response carries an `x-request-id`.

### Privacy & safety highlights
- **One active couple per user**; pairing requires an invite code, breakup needs explicit confirmation.
- **Surprise tasks** are invisible to the partner (list *and* direct fetch) until they unlock.
- **Future letters** stay hidden from the receiver until `unlockTime`; **daily answers** unlock only after both reply.
- **Photos** are served via an auth-gated endpoint scoped to the owning couple.
- The repair / AI-softening flow has a **safety guard**: messages signalling self-harm or violence stop and surface human-help resources instead.

---

## Status

**Complete and verified end-to-end** (backend via curl, web via browser): auth (JWT + refresh) ·
couple pairing · todo (incl. timeless "想到再做") · anniversary · calendar · mood · notes · album ·
home aggregate · questions · "未來一起做的事" · finance · date planner · repair · AI assistant ·
notifications · data export · two-step breakup · **PostgreSQL + Flyway** · Angular UI (5 tabs + onboarding).

**Next (productionization):** object-storage adapter for photos · real FCM credentials ·
Capacitor wrapper for native iOS/Android · automated test suite.

Built from the product spec — a **Couple OS**, not another chat app.
