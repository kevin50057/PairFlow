# PairFlow Plan

This file tracks the implementation plan for PairFlow. `spec.md` defines what the product must be; this file defines what to build next and how to know it is done.

## Current Status

PairFlow is functionally complete as a full-stack prototype:

- Spring Boot API with JWT auth, refresh tokens, PostgreSQL + Flyway, H2 fallback.
- Angular mobile-first frontend with five main tabs.
- Couple pairing, todos, calendar, anniversaries, mood, notes, album, daily questions, wishes, finance, date plans, repair, AI, notifications, audit log, export, and two-step breakup.
- Seeded demo couple: Kevin and 魚丸.
- Daily question catalog expanded to 1000 seeded cards.

## Milestone 1: Stabilize The Spec-Driven Base

Goal: make the existing product easier to evolve without losing behavioral rules.

Tasks:
- Keep `spec.md`, `data-model.md`, and `api.md` updated with every behavior change.
- Add tests for privacy gates:
  - surprise todo hidden from partner
  - future letter hidden before unlock time
  - daily question partner answer hidden until both answered
  - media endpoint rejects non-member access
  - couple-scoped resources reject outsiders
- Add tests for inclusive relationship day count.
- Add tests for question seeding top-up to 1000.
- Add API examples for high-risk flows in `api.md`.

Done criteria:
- `mvn test` covers the privacy gates above.
- New changes that touch domain rules update docs in the same PR.
- Local seed data works on empty PostgreSQL and H2 profiles.

## Milestone 2: Product UX Tightening

Goal: make daily use clearer, calmer, and more polished.

Tasks:
- Continue reducing emoji-heavy UI in core navigation and operational screens.
- Give every major empty state a useful next action.
- Make daily home the primary "what matters today" surface.
- Improve onboarding:
  - explain pairing
  - explain invite code expiry
  - explain one active couple rule
- Improve daily question experience:
  - show answer progress clearly
  - clarify both-answer unlock rule
  - avoid pressure in copy
- Improve repair flow:
  - slower, clearer steps
  - stronger safety copy when flagged
  - clear cancel affordance

Done criteria:
- Mobile 390px and desktop browser screenshots have no clipped primary text.
- First-time user can pair and reach home without reading README.
- Sensitive flows show clear next actions and safe exits.

## Milestone 3: Production Hardening

Goal: make PairFlow safe to run beyond local demo.

Tasks:
- Replace local file photo storage with S3-compatible object storage.
- Configure real FCM credentials and push notification delivery.
- Add database backup/restore notes.
- Add production environment variable checklist.
- Add rate limiting for auth and invite code endpoints.
- Add stronger JWT secret validation in production.
- Add structured logs for critical flows.

Done criteria:
- App can run with production env vars only.
- Photos are no longer stored only on local disk.
- Push sender works with a real service account.
- Deployment docs describe backup and secret rotation.

## Milestone 4: Native Wrapper

Goal: package PairFlow for mobile usage.

Tasks:
- Add Capacitor.
- Configure iOS and Android builds.
- Verify push notifications on devices.
- Verify safe-area layout with bottom navigation.
- Add app icons and splash assets.

Done criteria:
- App runs on iOS simulator and Android emulator.
- Login, notification, upload, and bottom nav work on native shells.

## Milestone 5: Relationship Intelligence

Goal: make the app more helpful without becoming invasive.

Tasks:
- Improve AI nudges using recent context:
  - overdue tasks
  - partner mood
  - upcoming anniversary
  - unanswered daily question
- Add user controls for AI usage.
- Add AI safety and privacy copy in settings.
- Keep the default stub provider deterministic for local dev.

Done criteria:
- AI features are useful with stub provider.
- Anthropic provider is optional and never required for core app use.
- Users can understand what data may be used by AI.

## Current Sprint Candidate

Recommended next sprint:

1. Add backend tests for privacy gates.
2. Add seed verification test for 1000 question cards.
3. Polish daily question UI copy and state handling.
4. Add `decisions.md` for architecture/product decisions if scope continues to grow.

## Risks

- Too many relationship features can make the app feel busy instead of supportive.
- Sensitive data requires careful access rules and test coverage.
- AI copy can sound overconfident if not constrained.
- Local photo storage is not production-safe.
- Push notifications can become noisy if preferences are not respected.

## Definition Of Done

For feature work:
- Behavior is represented in `spec.md` if it affects product rules.
- Data changes are represented in `data-model.md`.
- API changes are represented in `api.md`.
- Backend compiles with `mvn test`.
- Frontend compiles with `npm run build` when frontend code changes.
- Privacy-sensitive behavior has a regression test.

