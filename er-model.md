# PairFlow ER Model

This ER model is generated from the current `pairflow` PostgreSQL schema plus logical relationships inferred from column names and application code.

Important note: the current database has primary keys and unique constraints, but no physical foreign key constraints. The relationships below are logical application-level relationships.

## Core Relationship Map

```mermaid
erDiagram
  users ||--o{ refresh_tokens : "user_id"
  users ||--o{ user_devices : "user_id"
  users ||--o{ notification_preferences : "user_id"
  users ||--o{ notifications : "recipient_id"
  users ||--o{ audit_logs : "actor_id"

  users ||--o{ couples : "useraid"
  users ||--o{ couples : "userbid"
  users ||--o{ couple_invites : "inviter_user_id"
  users ||--o{ couple_invites : "accepted_by_user_id"
  couples ||--o{ couple_invites : "couple_id"
  couples ||--o{ pending_breakups : "couple_id"
  users ||--o{ pending_breakups : "initiator_id"
  users ||--o{ pending_breakups : "confirmed_by_id"

  couples ||--o{ todos : "couple_id"
  users ||--o{ todos : "created_by"
  users ||--o{ todos : "assignee_user_id"
  todos ||--o{ todo_checklist_items : "todo_id"
  todos ||--o{ todo_comments : "todo_id"
  users ||--o{ todo_comments : "author_id"

  couples ||--o{ anniversaries : "couple_id"
  users ||--o{ anniversaries : "created_by"
  couples ||--o{ events : "couple_id"
  users ||--o{ events : "created_by"
  events ||--o{ todos : "related_event_id"
  anniversaries ||--o{ todos : "related_anniversary_id"

  couples ||--o{ mood_entries : "couple_id"
  users ||--o{ mood_entries : "user_id"
  mood_entries ||--o{ mood_reactions : "mood_entry_id"
  users ||--o{ mood_reactions : "user_id"

  couples ||--o{ notes : "couple_id"
  users ||--o{ notes : "sender_id"
  users ||--o{ notes : "receiver_id"

  couples ||--o{ albums : "couple_id"
  users ||--o{ albums : "created_by"
  albums ||--o{ photos : "album_id"
  couples ||--o{ photos : "couple_id"
  users ||--o{ photos : "uploader_id"

  question_cards ||--o{ daily_questions : "question_card_id"
  couples ||--o{ daily_questions : "couple_id"
  daily_questions ||--o{ question_answers : "daily_question_id"
  users ||--o{ question_answers : "user_id"

  couples ||--o{ wishes : "couple_id"
  users ||--o{ wishes : "added_by"
  todos ||--o{ wishes : "converted_todo_id"

  couples ||--o{ expenses : "couple_id"
  users ||--o{ expenses : "created_by"
  users ||--o{ expenses : "paid_by_user_id"
  events ||--o{ expenses : "related_event_id"

  couples ||--o{ date_plans : "couple_id"
  users ||--o{ date_plans : "created_by"
  date_plans ||--o{ date_candidates : "plan_id"
  users ||--o{ date_candidates : "added_by"
  date_candidates ||--o{ date_votes : "candidate_id"
  users ||--o{ date_votes : "user_id"
  date_candidates ||--o{ date_plans : "chosen_candidate_id"
  events ||--o{ date_plans : "scheduled_event_id"

  couples ||--o{ repair_sessions : "couple_id"
  users ||--o{ repair_sessions : "initiator_id"
  users ||--o{ repair_sessions : "responder_id"

  couples ||--o{ notifications : "couple_id"
  couples ||--o{ audit_logs : "couple_id"
```

## Auth And Couple

```mermaid
erDiagram
  users {
    varchar id PK
    varchar email UK
    varchar display_name
    varchar password_hash
    varchar timezone
    date birthday
    varchar gender
    varchar avatar_url
    varchar bio
    timestamptz created_at
    timestamptz updated_at
  }

  refresh_tokens {
    varchar id PK
    varchar user_id
    varchar token_hash UK
    timestamptz expires_at
    boolean revoked
    timestamptz revoked_at
  }

  couples {
    varchar id PK
    varchar useraid
    varchar userbid
    date relationship_start_date
    varchar status
    varchar data_handling
    timestamptz ended_at
  }

  couple_invites {
    varchar id PK
    varchar code UK
    varchar inviter_user_id
    varchar accepted_by_user_id
    varchar couple_id
    varchar status
    timestamptz expires_at
  }

  pending_breakups {
    varchar id PK
    varchar couple_id
    varchar initiator_id
    varchar confirmed_by_id
    varchar data_handling
    boolean confirmed
    boolean cancelled
    timestamptz expires_at
  }

  users ||--o{ refresh_tokens : owns
  users ||--o{ couples : useraid
  users ||--o{ couples : userbid
  users ||--o{ couple_invites : creates
  users ||--o{ couple_invites : accepts
  couples ||--o{ couple_invites : creates
  couples ||--o{ pending_breakups : has
  users ||--o{ pending_breakups : initiates
  users ||--o{ pending_breakups : confirms
```

## Daily Work And Calendar

```mermaid
erDiagram
  todos {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar assignee_user_id
    varchar title
    varchar status
    varchar type
    varchar priority
    timestamptz due_date
    boolean assigned_to_both
    boolean secret
    timestamptz secret_unlock_at
    double goal_target
    double goal_current
    varchar goal_unit
    varchar related_event_id
    varchar related_anniversary_id
  }

  todo_checklist_items {
    varchar id PK
    varchar todo_id
    varchar title
    boolean completed
    int sort_order
  }

  todo_comments {
    varchar id PK
    varchar todo_id
    varchar author_id
    varchar content
  }

  anniversaries {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar title
    date date
    varchar repeat_type
    varchar reminder_days_before_csv
  }

  events {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar title
    varchar event_type
    timestamptz start_time
    timestamptz end_time
    varchar location_name
    varchar related_album_id
    varchar related_todo_id
  }

  couples ||--o{ todos : owns
  users ||--o{ todos : creates
  users ||--o{ todos : assigned
  todos ||--o{ todo_checklist_items : has
  todos ||--o{ todo_comments : has
  users ||--o{ todo_comments : writes
  couples ||--o{ anniversaries : owns
  users ||--o{ anniversaries : creates
  couples ||--o{ events : owns
  users ||--o{ events : creates
  events ||--o{ todos : related
  anniversaries ||--o{ todos : related
```

## Emotional And Memory Layer

```mermaid
erDiagram
  mood_entries {
    varchar id PK
    varchar couple_id
    varchar user_id
    varchar mood
    varchar emoji
    varchar note
    boolean need_response
  }

  mood_reactions {
    varchar id PK
    varchar mood_entry_id
    varchar user_id
    varchar reaction
  }

  notes {
    varchar id PK
    varchar couple_id
    varchar sender_id
    varchar receiver_id
    varchar note_type
    varchar title
    varchar content
    timestamptz unlock_time
    boolean is_read
    boolean is_favorite
    timestamptz read_at
  }

  albums {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar title
    varchar cover_photo_url
  }

  photos {
    varchar id PK
    varchar couple_id
    varchar album_id
    varchar uploader_id
    varchar image_url
    varchar storage_key
    varchar thumbnail_url
    timestamptz taken_at
    boolean is_favorite
  }

  couples ||--o{ mood_entries : owns
  users ||--o{ mood_entries : writes
  mood_entries ||--o{ mood_reactions : has
  users ||--o{ mood_reactions : reacts
  couples ||--o{ notes : owns
  users ||--o{ notes : sends
  users ||--o{ notes : receives
  couples ||--o{ albums : owns
  users ||--o{ albums : creates
  albums ||--o{ photos : contains
  couples ||--o{ photos : owns
  users ||--o{ photos : uploads
```

## Daily Questions

```mermaid
erDiagram
  question_cards {
    varchar id PK
    varchar text
    varchar category
    varchar sensitivity
  }

  daily_questions {
    varchar id PK
    varchar couple_id
    varchar question_card_id
    date date
    boolean is_favorite
  }

  question_answers {
    varchar id PK
    varchar daily_question_id
    varchar user_id
    varchar answer
  }

  couples ||--o{ daily_questions : receives
  question_cards ||--o{ daily_questions : selected
  daily_questions ||--o{ question_answers : has
  users ||--o{ question_answers : writes
```

Constraints:
- `daily_questions`: `unique(couple_id, date)`
- `question_answers`: `unique(daily_question_id, user_id)`
- `question_cards` is a global catalog, currently seeded to 1000 cards.

## Planning, Money, Repair, Notification

```mermaid
erDiagram
  wishes {
    varchar id PK
    varchar couple_id
    varchar added_by
    varchar title
    varchar category
    varchar status
    varchar priority
    varchar converted_todo_id
    timestamptz completed_at
  }

  expenses {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar paid_by_user_id
    varchar related_event_id
    varchar category
    varchar split_type
    double amount
    double custom_payer_ratio
    timestamptz spent_at
  }

  date_plans {
    varchar id PK
    varchar couple_id
    varchar created_by
    varchar title
    varchar date_type
    varchar budget_level
    varchar status
    varchar chosen_candidate_id
    varchar scheduled_event_id
  }

  date_candidates {
    varchar id PK
    varchar plan_id
    varchar added_by
    varchar title
    varchar location
  }

  date_votes {
    varchar id PK
    varchar candidate_id
    varchar user_id
    varchar vote
  }

  repair_sessions {
    varchar id PK
    varchar couple_id
    varchar initiator_id
    varchar responder_id
    varchar state
    varchar status
    boolean flagged
    varchar feelings
    varchar softened_message
    varchar response_type
  }

  notifications {
    varchar id PK
    varchar couple_id
    varchar recipient_id
    varchar type
    varchar title
    varchar related_type
    varchar related_id
    boolean is_read
    timestamptz read_at
  }

  notification_preferences {
    varchar id PK
    varchar user_id UK
    varchar disabled_types_csv
  }

  audit_logs {
    varchar id PK
    varchar actor_id
    varchar couple_id
    varchar action
    varchar target_type
    varchar target_id
    varchar ip_address
  }

  couples ||--o{ wishes : owns
  users ||--o{ wishes : adds
  todos ||--o{ wishes : converted_from
  couples ||--o{ expenses : owns
  users ||--o{ expenses : creates
  users ||--o{ expenses : pays
  events ||--o{ expenses : related
  couples ||--o{ date_plans : owns
  users ||--o{ date_plans : creates
  date_plans ||--o{ date_candidates : has
  users ||--o{ date_candidates : adds
  date_candidates ||--o{ date_votes : receives
  users ||--o{ date_votes : votes
  date_candidates ||--o{ date_plans : chosen
  events ||--o{ date_plans : scheduled
  couples ||--o{ repair_sessions : owns
  users ||--o{ repair_sessions : initiates
  users ||--o{ repair_sessions : responds
  couples ||--o{ notifications : scopes
  users ||--o{ notifications : receives
  users ||--o{ notification_preferences : configures
  users ||--o{ audit_logs : acts
  couples ||--o{ audit_logs : scopes
```

Constraints:
- `date_votes`: `unique(candidate_id, user_id)`
- `notification_preferences`: `unique(user_id)`
- `users`: `unique(email)`
- `couple_invites`: `unique(code)`
- `refresh_tokens`: `unique(token_hash)`

