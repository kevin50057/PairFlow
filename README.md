# 雷評 / 踩雷餐廳 (Thunder Eats)

A mobile-first **"bad-restaurant discovery"** app. Instead of recommending good food,
it helps people **avoid bad restaurants** via a 1–5 **雷度 (thunder score)**.

> **MVP status — core flow + member system + moderation, verified end-to-end:**
> 附近餐廳 → 餐廳詳情（含地圖）→ 登入 → 我要踩雷（1~5 雷 + 標籤 + 文字 + 照片）→ 送出 → 平均雷度即時重算。
> Plus: 註冊/登入（JWT）、個人檔案、我的雷評、編輯/刪除、檢舉、Admin 審核。
> One codebase ships both the **web app** and the **iPhone app** (Capacitor).

---

## Tech stack

| Layer | Choice |
|---|---|
| Mobile + Web | **Ionic React + Capacitor + Vite** (one codebase → web now, iOS later) |
| API | **NestJS** (controller / service / DTO separation, global validation + error filter) |
| DB | **Prisma ORM** — **SQLite for zero-setup dev**, Postgres-ready for prod |
| Lang | TypeScript end-to-end |
| Monorepo | pnpm workspaces |

## Layout

```
thunder-eats/
├─ apps/
│  ├─ api/                    # NestJS + Prisma
│  │  ├─ prisma/
│  │  │  ├─ schema.prisma     # users, restaurants, reviews, review_tags, …
│  │  │  ├─ migrations/       # DB migration scripts
│  │  │  └─ seed.ts           # admin + 5 users, 16 restaurants, tags, reviews
│  │  └─ src/
│  │     ├─ common/           # api-error, exception filter, geo, pagination, enums
│  │     ├─ prisma/           # PrismaService (global module)
│  │     ├─ auth/             # JWT register/login, guards (Jwt + Roles)
│  │     ├─ user/             # profile + my reviews
│  │     ├─ restaurant/       # nearby / search / detail
│  │     ├─ review/           # list / create / edit / delete (+ live aggregation)
│  │     ├─ review-tag/       # predefined tags
│  │     ├─ report/           # user reports
│  │     ├─ media/            # image upload (local-disk storage abstraction)
│  │     └─ admin/            # moderation: reports queue, hide/restore
│  └─ mobile/                 # Ionic React + Capacitor
│     ├─ capacitor.config.ts
│     └─ src/
│        ├─ api/              # typed fetch client (+ token), endpoints, DTO types
│        ├─ auth/             # AuthContext (login state, token bootstrap)
│        ├─ components/       # ThunderScore, RestaurantCard, TagChips
│        ├─ hooks/            # useGeolocation (Capacitor + fallback)
│        ├─ pages/            # Home, Detail, WriteReview, Login, Profile,
│        │                   #   MyReviews, AdminReports
│        └─ theme/            # tokens + styles (light & dark)
└─ pnpm-workspace.yaml
```

---

## Quick start

**Prerequisites:** Node ≥ 20 and `pnpm` (`npm i -g pnpm`). No Docker/Postgres needed for dev.

```bash
# 1) install everything
pnpm install

# 2) create the SQLite DB, run migrations, and seed it
pnpm --filter @thunder/api exec prisma migrate dev --name init   # first time
#   (re-seed anytime with a clean slate:)
pnpm --filter @thunder/api db:reset

# 3) run the API  (http://localhost:3000/api/v1)
pnpm dev:api

# 4) in another terminal, run the web/mobile app  (http://localhost:5173)
pnpm dev:mobile
```

Open **http://localhost:5173**. Allow location for real "nearby", or deny it —
the app falls back to Taipei and prompts you to search instead.

### Seed / test data
- Admin: `admin@thunder.test` (role ADMIN)
- Users: `user1@thunder.test` … `user5@thunder.test`
- 16 restaurants around Taipei 101 + predefined tags + ~45 reviews
- Passwords are placeholders until the auth module lands (see "Roadmap").

---

## API (base `/api/v1`)

🔒 = requires `Authorization: Bearer <token>`. 👑 = ADMIN only.

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | liveness |
| POST | `/auth/register` · `/auth/login` | create account / sign in → `{ token, user }` |
| GET | 🔒 `/auth/me` | current profile |
| GET | `/restaurants/nearby?lat&lng&radius&page&size` | nearby, sorted by distance |
| GET | `/restaurants/search?q&page&size` | name search (exact > prefix > substring) |
| GET | `/restaurants/:id` | detail + tag distribution + latest reviews |
| GET | `/restaurants/:id/reviews?page&size` | reviews for a restaurant |
| POST | 🔒 `/restaurants/:id/reviews` | create review (recomputes 雷度 aggregate) |
| GET · PUT · DELETE | 🔒 `/reviews/:id` | get / edit / delete own review (owner only) |
| GET | 🔒 `/users/me/reviews` | my reviews |
| PATCH | 🔒 `/users/me` | update profile (nickname/avatar) |
| POST | 🔒 `/reports` | report a review |
| POST | 🔒 `/media/review-images` | upload image (jpg/png/webp ≤ 5MB) → `{ url }` |
| GET | 👑 `/admin/reports?status&page&size` | moderation queue |
| PATCH | 👑 `/admin/reviews/:id/hide` · `/restore` | hide / restore a review |
| PATCH | 👑 `/admin/reports/:id` | resolve / reject a report |
| GET | `/review-tags` | predefined tag list |

Auth is **JWT** (Bearer). Passwords are hashed with **bcrypt**. Auth/report
endpoints are rate-limited. All errors share one shape:
`{ "code", "message", "details": [] }` (`VALIDATION_ERROR`, `NOT_FOUND`,
`CONFLICT`, `UNAUTHORIZED`, `FORBIDDEN`, `RATE_LIMITED`, …). Every response
carries an `x-request-id`.

```bash
# try it:
curl "http://localhost:3000/api/v1/restaurants/nearby?lat=25.033&lng=121.5654&radius=1500&size=3"
```

---

## iPhone app

Same codebase. Once you're on a Mac with Xcode:

```bash
pnpm --filter @thunder/mobile build      # produces dist/
pnpm --filter @thunder/mobile ios:add    # npx cap add ios  (first time)
pnpm --filter @thunder/mobile ios:open   # opens Xcode → run on simulator/device
```

`appId`/`appName` live in `apps/mobile/capacitor.config.ts`.

---

## Design / decisions

- **Distance**: simple lat/lng bounding-box pre-filter + Haversine in app code
  (per spec: start simple, add spatial indexing later).
- **Aggregation**: `averageThunderScore` + `reviewCount` recomputed transactionally
  on every review write.
- **One review per (restaurant, user)** enforced by a DB unique constraint.
- **Status/role as strings** (not enums) so the identical schema runs on SQLite
  (dev) and Postgres (prod). Allowed values centralised in `src/common/enums.ts`.

### Auth
JWT Bearer tokens (7-day expiry), bcrypt password hashing, and a `JwtAuthGuard`
that loads the live user per request. `RolesGuard` + `@Roles('ADMIN')` protect the
admin endpoints. Guests can browse; submitting/editing/reporting requires login.
The web client stores the token in `localStorage` (persists in the iOS WKWebView).

---

## Roadmap (per spec phases)

- **Phase 1 ✅** restaurants · nearby · search · detail · review create/list · tags · aggregation
- **Phase 2 ✅** auth (JWT + bcrypt) · review edit/delete · image upload (media module)
- **Phase 3 ✅** report system · admin moderation (API + web panel) · profile & "my reviews"
- **Next** refresh tokens · push notifications · object-storage adapter for images · richer admin dashboard (search, all-reviews view)

---

## Useful scripts

```bash
pnpm dev:api                                   # run API (watch)
pnpm dev:mobile                                # run web app (Vite)
pnpm db:reset                                  # wipe + remigrate + reseed
pnpm --filter @thunder/api exec tsc --noEmit   # type-check API
pnpm --filter @thunder/mobile build            # type-check + production web build
```
