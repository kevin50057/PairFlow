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

## Tech stack

| Layer | Choice |
|---|---|
| Frontend | **Angular 21** (standalone components, signals, lazy routes) · mobile-first SCSS theme |
| Backend | **Java 21 · Spring Boot 3.4** (Web · Security · Data JPA · Validation) |
| Auth | **JWT** (HS256, `jjwt`) · BCrypt · stateless · HTTP interceptor on the client |
| DB | **H2** (file-based, zero-setup dev) — **Postgres-ready** (driver bundled, portable schema) |
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

**Prerequisites:** JDK 21 + Maven, and Node ≥ 20. No Docker/DB needed for dev.

```bash
# 1) API  → http://localhost:8080
cd backend && mvn spring-boot:run

# 2) Web  → http://localhost:4200   (in another terminal)
cd frontend && npm install && npm start
```

Open **http://localhost:4200**, register two accounts, pair them with an invite code, and you're in.
The H2 console is at `http://localhost:8080/h2-console` (JDBC `jdbc:h2:file:./data/pairflow`, user `sa`).

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

**Complete and verified end-to-end** (backend via curl, web via browser): auth · couple pairing ·
todo · anniversary · calendar · mood · notes · album · home aggregate · questions · wishlist ·
finance · date planner · repair · AI assistant · notifications · Angular UI (5 tabs + onboarding).

**Next (productionization):** Flyway migrations · Postgres + object storage · refresh tokens ·
real FCM push · Capacitor wrapper for native iOS/Android.

Built from the product spec — a **Couple OS**, not another chat app.
