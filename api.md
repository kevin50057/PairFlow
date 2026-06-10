# PairFlow API

Base URL: `/api`

Authentication: use `Authorization: Bearer <accessToken>` for protected endpoints.

Error shape:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message",
  "details": []
}
```

Every response includes an `x-request-id` header.

## Auth

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/auth/register` | No | Create account |
| POST | `/auth/login` | No | Login and receive access + refresh tokens |
| POST | `/auth/refresh` | No | Exchange refresh token for new access token |
| POST | `/auth/logout` | Yes | Revoke refresh token |
| GET | `/auth/me` | Yes | Current user |

Common request examples:

```json
POST /auth/login
{
  "email": "kevin@pairflow.test",
  "password": "secret123"
}
```

```json
POST /auth/refresh
{
  "refreshToken": "..."
}
```

## Users

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/users/me` | Yes | Current user profile |
| PATCH | `/users/me` | Yes | Update profile |
| POST | `/users/me/device` | Yes | Register push device |
| DELETE | `/users/me/device` | Yes | Remove push device |

Privacy:
- Users cannot update another user's profile.
- Device registration is scoped to current user.

## Couples

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/couples/invite` | Yes | Create invite code |
| POST | `/couples/join` | Yes | Join couple by code |
| GET | `/couples/me` | Yes | Current active couple |
| PATCH | `/couples/{coupleId}` | Yes | Update couple settings |
| POST | `/couples/{coupleId}/breakup` | Yes | Initiate breakup |
| POST | `/couples/{coupleId}/breakup/confirm` | Yes | Confirm breakup |
| DELETE | `/couples/{coupleId}/breakup` | Yes | Cancel pending breakup |
| GET | `/couples/{coupleId}/breakup/status` | Yes | Pending breakup status |
| GET | `/couples/{coupleId}/export` | Yes | Export couple data |

Rules:
- User may only access a couple they belong to.
- One user may only have one active couple.
- Breakup requires two-step confirmation.
- `daysTogether` is inclusive.

## Home

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/home` | Yes | Aggregated daily dashboard |

Returns:
- couple summary
- partner mood
- today's todos
- next anniversary
- today's events
- memory prompt

Rules:
- Non-critical sections may be absent.
- All data is current couple-scoped.

## Todos

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/todos` | Yes | List todos |
| POST | `/todos` | Yes | Create todo |
| GET | `/todos/{id}` | Yes | Get todo detail |
| PATCH | `/todos/{id}` | Yes | Update todo |
| POST | `/todos/{id}/complete` | Yes | Complete todo |
| DELETE | `/todos/{id}` | Yes | Delete todo |
| POST | `/todos/{id}/comments` | Yes | Add comment |
| POST | `/todos/{id}/checklist` | Yes | Add checklist item |
| PATCH | `/todos/{id}/checklist/{itemId}` | Yes | Update checklist item |
| DELETE | `/todos/{id}/checklist/{itemId}` | Yes | Delete checklist item |

List filters include:
- `status`
- `type`
- `dueFrom`
- `dueTo`
- `undated=true`

Rules:
- Surprise todos are hidden from partner in list and detail.
- Checklist and comments inherit parent todo access.

## Anniversaries

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/anniversaries` | Yes | List anniversaries |
| POST | `/anniversaries` | Yes | Create anniversary |
| PATCH | `/anniversaries/{id}` | Yes | Update anniversary |
| DELETE | `/anniversaries/{id}` | Yes | Delete anniversary |

Rules:
- Countdown is derived from next occurrence.
- Reminder days are stored as CSV.

## Events

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/events` | Yes | List events by date range |
| POST | `/events` | Yes | Create event |
| GET | `/events/{id}` | Yes | Get event |
| PATCH | `/events/{id}` | Yes | Update event |
| DELETE | `/events/{id}` | Yes | Delete event |

Common query:

```txt
GET /events?from=2026-06-01T00:00:00Z&to=2026-07-01T00:00:00Z
```

## Moods

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/moods/today` | Yes | Today's moods for me and partner |
| POST | `/moods` | Yes | Create/update mood |
| GET | `/moods/history` | Yes | Mood history |
| POST | `/moods/{moodId}/reactions` | Yes | React to partner mood |

Rules:
- Mood entries are couple-private.
- Reactions inherit mood access.

## Notes

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/notes` | Yes | List notes |
| POST | `/notes` | Yes | Create note or future letter |
| GET | `/notes/{id}` | Yes | Read note |
| POST | `/notes/{id}/read` | Yes | Mark read |
| POST | `/notes/{id}/favorite` | Yes | Toggle favorite |

Rules:
- Future letters are hidden from receiver before unlock time.
- Notes are couple-scoped.

## Albums And Photos

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/albums` | Yes | List albums |
| POST | `/albums` | Yes | Create album |
| PATCH | `/albums/{id}` | Yes | Update album |
| DELETE | `/albums/{id}` | Yes | Delete album |
| POST | `/photos` | Yes | Upload photo multipart |
| GET | `/photos` | Yes | List photos |
| PATCH | `/photos/{id}` | Yes | Update photo metadata |
| DELETE | `/photos/{id}` | Yes | Delete photo |
| GET | `/media/{filename}` | Yes | Serve uploaded media |

Rules:
- Media is auth-gated.
- Photo access requires couple membership.

## Daily Questions

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/questions/today` | Yes | Get today's daily question |
| POST | `/questions/today/answer` | Yes | Submit answer |
| GET | `/questions/history` | Yes | List daily question history |
| POST | `/questions/{id}/favorite` | Yes | Toggle favorite |

Answer request:

```json
{
  "answer": "..."
}
```

Rules:
- Today's question is created on first access.
- Partner answer is returned only when both partners answered.
- Question catalog is seeded to 1000 cards.

## Wishes / Future Together

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/wishes` | Yes | List wishes |
| POST | `/wishes` | Yes | Create wish |
| PATCH | `/wishes/{id}` | Yes | Update wish |
| POST | `/wishes/{id}/complete` | Yes | Mark completed |
| POST | `/wishes/{id}/to-todo` | Yes | Convert wish to todo |
| DELETE | `/wishes/{id}` | Yes | Delete wish |

Optional query:
- `status=ACTIVE`
- `status=COMPLETED`

Rules:
- Wish can be converted into a todo.
- Completed wishes contribute to progress UI.

## Expenses

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/expenses` | Yes | List expenses |
| GET | `/expenses/summary` | Yes | Expense summary |
| POST | `/expenses` | Yes | Create expense |
| PATCH | `/expenses/{id}` | Yes | Update expense |
| DELETE | `/expenses/{id}` | Yes | Delete expense |

Rules:
- Finance is lightweight tracking.
- No payment settlement is enforced by the app.

## Date Plans

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/date-plans` | Yes | List date plans |
| POST | `/date-plans` | Yes | Create date plan |
| GET | `/date-plans/{id}` | Yes | Get date plan |
| POST | `/date-plans/{id}/candidates` | Yes | Add candidate |
| POST | `/date-plans/{id}/candidates/{candidateId}/vote` | Yes | Vote candidate |
| POST | `/date-plans/{id}/finalize` | Yes | Finalize plan |
| DELETE | `/date-plans/{id}` | Yes | Delete plan |

Rules:
- Votes are one per user per candidate.
- Finalize can create event and preparation todos.

## Repair

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/repair` | Yes | List repair sessions |
| POST | `/repair` | Yes | Create repair session |
| GET | `/repair/{id}` | Yes | Get repair session |
| POST | `/repair/{id}/send` | Yes | Send softened message |
| POST | `/repair/{id}/respond` | Yes | Partner response |
| POST | `/repair/{id}/follow-ups` | Yes | Create follow-up tasks |

Rules:
- AI softening uses safety guard.
- Flagged content should return safe-help notice.

## AI

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/ai/todo-breakdown` | Yes | Break text into todos |
| POST | `/ai/date-suggestions` | Yes | Suggest date ideas |
| POST | `/ai/anniversary-message` | Yes | Draft anniversary message |
| POST | `/ai/soften` | Yes | Soften conflict text |
| POST | `/ai/memory-summary` | Yes | Summarize memory context |
| GET | `/ai/nudges` | Yes | Relationship nudges |

Rules:
- Stub provider is default.
- Anthropic provider is optional.
- Softening can be flagged by safety guard.

## Notifications

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/notifications` | Yes | List notifications |
| GET | `/notifications/unread-count` | Yes | Count unread |
| POST | `/notifications/{id}/read` | Yes | Mark one read |
| POST | `/notifications/read-all` | Yes | Mark all read |
| GET | `/notifications/preferences` | Yes | Get preferences |
| PUT | `/notifications/preferences` | Yes | Update preferences |

Rules:
- Notifications are per user.
- Disabled types should not produce push noise.

## Audit

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/audit-log/me` | Yes | Current user's audit entries |
| GET | `/audit-log/couple` | Yes | Couple audit entries |

Rules:
- Sensitive operations should be audit logged.
- Audit access is still couple/user scoped.

## Health

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| GET | `/health` | No | API health check |
