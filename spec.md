# PairFlow Spec

PairFlow is a Couple OS: a private relationship workspace for two people to plan, remember, care, decide, repair, and follow through together. It is not a chat app and should not compete with chat. It should turn relationship intentions into shared context, gentle prompts, and small concrete actions.

## Product Principles

- Couple-first, not social-first. Data belongs to one active couple and is never public.
- Daily-use over ceremony. The home screen should answer what matters today.
- Gentle by default. The app should reduce pressure, not create more relationship admin.
- Mutual consent matters. Pairing, breakup, answer reveal, future letters, and sensitive repair flows must respect both people.
- Records should feel human. Tasks, notes, moods, wishes, and memories are relationship artifacts, not enterprise tickets.
- AI assists tone and ideation only. It must not replace consent, therapy, emergency help, or personal agency.

## Users

### Primary Users

- Partner A and Partner B in a monogamous pair workspace.
- Each user may have at most one active couple.
- A user can register independently, then create or join a couple with an invite code.

### System Actors

- Notification scheduler creates reminders and in-app notifications.
- AI provider generates suggestions, softening text, nudges, and summaries.
- Audit logger records sensitive operations.

## Core Flows

### Account And Pairing

1. User registers or logs in.
2. If unpaired, user creates an invite code or joins with one.
3. Once paired, couple-scoped features become available.
4. All couple data is guarded by membership checks.

Acceptance criteria:
- A user outside a couple cannot access couple-scoped data.
- A user already in an active couple cannot join or create another active couple.
- Invite codes expire after the configured TTL.

### Daily Home

The home screen aggregates the relationship day:
- inclusive days together
- partner mood
- today's todos
- next anniversary
- today's calendar events
- memory prompt
- AI nudges
- active wishes

Acceptance criteria:
- Relationship day count is inclusive: start date is day 1.
- Home should be useful without requiring every module to have data.
- Missing non-critical sections should not break the page.

### Todos

Couple todos support:
- assignee: me, partner, both, unassigned
- status: pending, done
- priority
- type
- due date or timeless "想到再做"
- checklist items
- comments
- surprise tasks
- goal progress

Acceptance criteria:
- Surprise tasks are invisible to the partner in list and direct fetch until visible/unlocked.
- Undated tasks can be queried as first-class "想到再做" tasks.
- Completing wishes can optionally convert to todos.

### Calendar And Anniversaries

Calendar events support shared events and date planning output. Anniversaries support repeat rules, countdowns, and reminders.

Acceptance criteria:
- Date plans can finalize into a calendar event and preparation todos.
- Anniversary countdown uses the next occurrence.
- Notifications can be generated for configured reminder days.

### Mood

Each user can create today's mood with emoji, note, and whether they need a response. The partner can react with predefined response types.

Acceptance criteria:
- One current mood per user per day is shown in the daily context.
- Partner reactions are attached to a mood entry.
- Mood is private to the couple.

### Notes And Future Letters

Notes include normal notes and future letters. Future letters unlock at a configured time.

Acceptance criteria:
- Receiver cannot read a future letter before unlock time.
- Read state and favorite state are tracked.
- Notes remain scoped to the active couple.

### Memories And Album

Couples can create albums and upload photos. Media is served through authenticated endpoints. Memory provider can surface "去年今天" style prompts.

Acceptance criteria:
- Uploaded files are not public static assets.
- Media access requires couple membership.
- Photos can be filtered by album.

### Daily Questions

Daily questions use a shared global question catalog. Each couple receives one question per date. Each partner can answer. Partner answers unlock only after both have answered.

Data rules:
- `question_cards` is a global catalog.
- `daily_questions` maps a couple and date to one card.
- `question_answers` stores each user's answer.
- `unique(couple_id, date)` prevents multiple daily questions per couple per day.
- `unique(daily_question_id, user_id)` prevents duplicate answers by the same user.

Acceptance criteria:
- A partner answer is hidden until both partners have answered.
- Today's question is stable for the same couple and date.
- The seeded catalog should contain 1000 questions.
- Favorite state is stored on the daily question instance.

### Future Together / Wishes

Wishes are shared future experiences or goals, renamed in UI as "未來一起做的事".

Acceptance criteria:
- Wishes can be active or completed.
- Wishes can be converted into todos.
- Completed wishes contribute to shared progress.

### Finance

Finance is lightweight expense tracking, not a settlement engine.

Acceptance criteria:
- Users can create, update, delete expenses.
- Expenses support payer and split type.
- Summary answers who paid and approximate shared totals.
- UI tone should avoid blame.

### Date Planner

Date planning creates a plan, collects candidates, accepts votes, then finalizes a chosen candidate.

Acceptance criteria:
- Both partners can vote on candidates.
- Finalization creates a calendar event.
- Finalization may create preparation todos.

### Conflict Repair

Repair flow helps partners move from heated wording into safer communication:
1. Create repair session with current state and raw feeling.
2. AI softens the message if safe.
3. Sender sends.
4. Partner responds.
5. Follow-up tasks can be created.

Safety rules:
- Self-harm or violence signals must be flagged and return human-help resources.
- AI should not pretend to diagnose or mediate as a therapist.
- Sensitive flows should be cancellable and clearly worded.

### AI Assistant

AI capabilities:
- todo breakdown
- date suggestions
- anniversary messages
- soften text
- memory summaries
- relationship nudges

Acceptance criteria:
- Stub provider works offline by default.
- Anthropic provider can be enabled through environment variables.
- Safety guard applies to softening and repair-related flows.

### Notifications

Notifications include in-app notifications and FCM-ready push support.

Acceptance criteria:
- Users can read one notification or mark all read.
- Users can disable notification types.
- Device registration is per user.

### Breakup And Data Handling

Breakup is a two-step flow:
1. One partner initiates breakup with intended data handling.
2. Other partner confirms within the TTL.
3. Request can be cancelled before confirmation.

Acceptance criteria:
- Breakup requires explicit confirmation.
- Pending breakup expires.
- Sensitive action is audit logged.

## Privacy Rules

- Every couple-scoped entity must include or resolve a `coupleId`.
- Couple membership must be checked before reads and writes.
- Future letters are hidden before unlock time.
- Daily question partner answers are hidden until both answered.
- Surprise todos are hidden from partner.
- Photos are served through authenticated media endpoints.
- Audit logs record sensitive relationship operations.

## Data And Time Rules

- IDs are UUID strings.
- Timestamps use server time and are audited on base entities.
- App local date uses `Asia/Taipei` through `AppTime`.
- Relationship `daysTogether` is inclusive.
- PostgreSQL + Flyway is the default DB path; H2 is a fallback development profile.

## Non-Goals

- Public social feed.
- Multi-couple group workspace.
- Real payment settlement.
- Replacing emergency, legal, medical, or therapeutic help.
- End-to-end encrypted messaging in the current version.

## Demo Data

When the database has no users, dev seeding creates:
- `kevin@pairflow.test` / `secret123`
- `ying@pairflow.test` / `secret123`

The demo users are paired as Kevin and 魚丸.

