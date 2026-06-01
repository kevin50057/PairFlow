# PairFlow

> **讓情侶不只是聊天，而是一起生活、一起記錄、一起完成事情。**
> A **Couple OS / Relationship Workspace** — not another chat app, but a private space where
> couples plan, remember, and get things done together.

PairFlow combines a couple-only **Todo list**, a **shared calendar**, **anniversaries / countdowns**,
**mood check-ins**, **timed-unlock letters**, a **private album with "去年今天" memories**, and an
aggregated **daily home dashboard** — built around a strict one-couple-per-user privacy model.

---

## Tech stack

| Layer | Choice |
|---|---|
| Backend | **Java 21 · Spring Boot 3.4** (Web · Security · Data JPA · Validation) |
| Auth | **JWT** (HS256, `jjwt`) · BCrypt password hashing · stateless |
| DB | **H2** (file-based, zero-setup dev) — **Postgres-ready** (driver bundled, portable schema) |
| Build | **Maven** |
| Frontend | **Angular 21** (standalone) — *in progress* |

Design choices that keep the schema identical across H2 (dev) and Postgres (prod): string/UUID
primary keys, enums persisted as `@Enumerated(STRING)`, and `ddl-auto: update` for dev convenience
(swap to Flyway + `validate` for prod).

---

## Layout

```
pairflow/
└─ backend/                         # Spring Boot API
   ├─ pom.xml
   └─ src/main/java/com/pairflow/
      ├─ common/                    # BaseEntity (UUID + auditing), unified error shape, request-id, AppTime
      ├─ config/                    # SecurityConfig, JWT filter/service, CurrentUser, CORS
      ├─ auth/  · user/             # register/login/me, profile
      ├─ couple/                    # pairing (invite code / join / breakup) + CoupleContext membership guard
      ├─ todo/                      # tasks, checklist, comments, surprise-task visibility, goals
      ├─ anniversary/               # repeat + countdown (daysLeft)
      ├─ event/                     # shared calendar
      ├─ mood/                      # daily mood + partner reactions
      ├─ note/                      # notes / timed-unlock letters
      ├─ album/                     # albums, photos, media upload, "去年今天"
      ├─ memory/                    # MemoryProvider seam for the home memory card
      └─ home/                      # aggregated daily dashboard (GET /api/home)
```

---

## Quick start

**Prerequisites:** JDK 21 and Maven (`brew install openjdk maven`). No Docker/DB needed for dev.

```bash
cd backend
mvn spring-boot:run         # http://localhost:8080
```

```bash
# smoke test
curl http://localhost:8080/api/health

# register two users, pair them, and explore
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"kevin@pairflow.test","password":"secret123","displayName":"Kevin"}'
```

The H2 console is at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/pairflow`, user `sa`).

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
| Home | 🔒`GET /home` — aggregated dashboard |

Every couple-scoped request is authorized through `CoupleContext`, which resolves the caller's active
couple and rejects access to anyone outside it. All errors share one shape:
`{ "code", "message", "details": [] }`; every response carries an `x-request-id`.

### Privacy highlights
- **One active couple per user**; pairing requires an invite code.
- **Surprise tasks** are invisible to the partner (list *and* direct fetch) until their unlock time.
- **Future letters** stay hidden from the receiver until `unlockTime`.
- **Photos** are served only through an auth-gated endpoint scoped to the owning couple.

---

## Status / roadmap

**Done (verified end-to-end):** auth · couple pairing · todo (+checklist/comments/surprise/goals) ·
anniversary/countdown · calendar · mood + reactions · notes/letters · album/photo + memories ·
home aggregate.

**In progress:** question cards · wishlist · finance lite · date planner · repair mode · AI assistant ·
notifications · **Angular frontend** (Home / Todo / Calendar / Memories / Us).

Built from [`couple_app_spec_md`](.) — see the spec for the full product vision.
