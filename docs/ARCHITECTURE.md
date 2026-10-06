# Architecture

Owner decisions live in `PROJECT_BRIEF.md`. This document explains how the system is structured and why. Material changes to it require an ADR in `docs/adr/`.

## 1. Quality attributes (in priority order)
1. **Blocking correctness and safety:** never block emergency or system-critical apps, never trap the user.
2. **Reliability:** survives reboot, process death, Doze, OEM task killers and revoked permissions.
3. **Battery efficiency:** monitoring costs must be negligible.
4. **Privacy:** screen content and raw usage stay on the device.
5. **Testability:** all decision logic runs in plain JVM tests.
6. **Maintainability:** a solo developer must be able to change any part in a day.
7. **Cost control:** Firebase usage stays predictable.

## 2. Layering
```
UI (Compose screens, ViewModels)
        |  depends on
Domain (use cases, rules engine, models, repository interfaces)  <- pure Kotlin
        ^  implemented by
Data (repositories, local store, remote services, mappers)
        ^  uses
System adapters (foreground service, optional accessibility service, overlay,
usage-events reader, notifications, schedulers, permission checkers)
```
Dependencies point inward. The domain layer imports no Android, Compose, Firebase or Room classes.

## 3. Package structure (single module first)
```
app/            Application class, DI wiring, navigation host, manifest
core/common     result types, dispatchers, clock, extensions
core/designsystem  theme, tokens, shared components
core/domain     shared models and interfaces
core/data       local store, sync, mappers
core/system     service, overlay, usage reader, permission and health checkers
feature/onboarding | timer | stats | limits | studymode | rooms | profile | premium
```
Split into Gradle modules only when build time, ownership or compile-time boundaries justify it. Record the decision in an ADR.

## 4. Runtime components
- **Focus service (foreground):** owns live monitoring during sessions or active schedules. One notification channel for the service, separate channels for reminders, social and reports.
- **Optional accessibility service:** runs only when the user opts in. It emits signals to the rules engine and performs only the exit action.
- **Workers:** sync, daily rollup, midnight limit reset, weekly report.
- **Receivers:** boot completed, time or time-zone change, package added or removed. Re-arm blocking using mechanisms currently allowed by Android (verify foreground-service start restrictions for boot receivers on each new OS release).
- **Overlay manager:** renders the blocker window. Never starts a blocker activity from the background.

## 5. Dependency injection
Scopes: application singleton (repositories, clock, dispatchers), ViewModel, and a service scope for the focus service. Everything with side effects is behind an interface so tests substitute fakes. No service locators and no static access to singletons.

## 6. Concurrency
- Dispatchers are injected, never hard-coded.
- Structured concurrency only. No global scope.
- The service owns a supervised scope that is cancelled on destroy.
- Flows are cold by default. Shared streams use explicit sharing policies chosen to stop upstream work when nobody is listening.
- Nothing blocking runs on the main thread. StrictMode is enabled in debug builds.

## 7. Error handling
- Domain functions return explicit success/failure results rather than throwing for expected failures.
- Error taxonomy: network, permission missing, policy or feature unavailable, validation, conflict, unknown.
- Each category maps to a user-facing message and a recovery action (retry, open settings, explain).
- Unexpected exceptions are reported to crash monitoring with context keys (OS version, OEM, health state) and never include personal data.

## 8. State and caching
See sections 8 and 9 of `PROJECT_BRIEF.md`. In short: unidirectional data flow, one immutable state object per screen, a single repository owns the focus session, local storage is the source of truth, time is stored as timestamps.

## 9. Sync design
- Local changes go to a pending-changes queue and are marked synced only after server acknowledgement.
- Every upload carries an idempotency key so retries cannot double-count.
- Server wins for XP, streaks and leaderboards. Last write wins for settings.
- Anonymous-to-account linking merges local history without loss and is tested.
- Retries use exponential back-off with network constraints.

## 10. Configuration and flags
- Remote Config holds detection identifiers, kill switches, feature flags and fail-open or fail-closed defaults, with safe local defaults for first launch.
- Build variants: debug, staging (separate Firebase project), release. No secrets in source. Keys are restricted by package name and signing certificate.

## 11. Logging and observability
- Structured logs, stripped or reduced in release. No personal data and no screen content.
- Analytics events (names only here): onboarding step completed, permission granted or denied, session started or completed or aborted, block shown, emergency exit used, feature-broken detected, health state changed, sync failed.
- Performance traces: cold start, service start latency, block-decision latency, sync duration.

## 12. Build and release engineering
Version catalog, central dependency governance (Dependabot or Renovate), R8 minification, baseline profiles, Play App Signing, CI stages: lint and static analysis, unit tests, instrumented tests on emulators, rules tests, build artifacts.

## 13. Evolution
- ADR for every decision that is hard to reverse (module split, new dependency of substance, new permission, data retention change).
- Deprecation policy: remove dead code within one milestone of replacement.

## 14. Anti-patterns (do not introduce)
God ViewModels, business logic in composables, domain code touching Android classes, hard-coded third-party view identifiers as the only signal, polling loops without screen-state awareness, client-authoritative scores, silent catch-all exception handlers, tick-counting timers, global mutable state.
