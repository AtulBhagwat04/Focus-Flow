---
name: firebase-backend
description: Use when designing or changing authentication, Firestore or Realtime Database usage, Cloud Functions, security rules, Remote Config, App Check, notifications, sync, or backend cost.
---

# Firebase backend engineering

## Principles
Server-authoritative for competitive values. Least-privilege rules. Idempotent writes. Listeners only while visible. Cost estimated before building.

## Procedure
1. Decide what stays on device only (screen content, raw usage) versus what syncs.
2. For each new data need, state who can read, who can write, and what validation the server applies.
3. Put validation and aggregation in Cloud Functions. Clients submit facts, the server computes totals.
4. Write security rules and tests (allowed and denied, cross-user) before client code.
5. Estimate reads, writes and listener time per active user per day. Add pagination and caching where needed.
6. Design for offline: pending-change queue, idempotency keys, conflict policy (server wins for competitive data, last write wins for settings).
7. Use Remote Config for flags, kill switches and identifiers, with safe defaults and last-known-good values.
8. Enable App Check and treat it as a speed bump.
9. Test account deletion across all stores.
10. Use dev, staging and production projects and never mix them.

## Review checklist
- Can a user read or write anything not theirs?
- Can a client inflate its own score?
- What happens if the same request arrives twice?
- What is the monthly cost at 1,000 and 100,000 users?
- What happens offline for a week, then online?
- Is anything stored that should stay on the device?

## Pitfalls
Unbounded queries, listeners left attached, trusting client timestamps, storing viewing history, forgetting leaderboard cleanup on deletion, hot documents with many writers, notifications without rate limits.
