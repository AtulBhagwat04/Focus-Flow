# Rule: Firebase and backend

Related: `docs/ARCHITECTURE.md` section 9, `docs/SECURITY_PRIVACY.md`.

- Server-authoritative for XP, streaks, leaderboards and any competitive value. Clients submit sessions, and Cloud Functions validate and update totals.
- Uploads are idempotent. Every client write that may be retried carries an idempotency key.
- Security rules grant least privilege and are tested with the emulator for allowed and denied access, including cross-user access.
- Use Firestore for durable data, Realtime Database only for presence, and keep presence data minimal.
- Attach listeners only while a screen is visible, and detach on leave. Paginate lists. Avoid unbounded reads.
- Design queries and listeners with cost in mind. State the expected reads and writes per active user per day when adding a feature.
- Enable App Check. Do not rely on it as the only protection.
- Remote Config holds flags, kill switches and identifiers with safe local defaults. Always keep last-known-good values.
- Separate development, staging and production projects. Never point a debug build at production.
- Deleting an account deletes or anonymises the user's data across all stores, including leaderboards and rooms. Test it.
- Do not store anything the privacy rules classify as device-only.
- Cloud Functions are small, idempotent and observable. Log outcomes without personal data.
- Notifications are rate-limited and user-controllable.
