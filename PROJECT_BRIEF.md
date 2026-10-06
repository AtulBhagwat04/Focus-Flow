# Project Brief: Focus & Digital-Wellbeing Android App

**Working title:** "the App" (final name to be decided)
**Owner:** Atul Bhagwat, solo developer
**Status:** Pre-development, planning complete
**Category reference:** Regain (Play Store id `ai.regainapp`). Use it only to understand the category. Never copy its name, logo, mascot, copy, screenshots or other assets.

How to use this file: read it fully before planning any work. `AGENTS.md` holds the short standing rules, and this file holds the reasoning and detail behind them. When the two disagree, ask the owner.

---

## 1. Idea in one page

**Problem.** Students and young professionals lose hours to short-form video feeds and social apps, even when they want to study or work.

**Solution.** An Android app that combines:
1. A focus timer (Pomodoro, stopwatch, custom sessions).
2. A reliable blocker for distracting apps, with schedules and daily limits.
3. An optional "advanced blocking" module that blocks YouTube Shorts and Instagram Reels, and a Study Mode that only allows educational YouTube channels.
4. Usage statistics that show where time goes.
5. Motivation features: streaks, XP, coins and levels.
6. Social accountability: friends, live focus rooms and leaderboards.

**Core promise.** "When I start a focus session, my phone stops pulling me away."

**Target users.** Students (exam prep, college), self-learners, and early-career professionals. Android only. India-first, with other regions possible later.

**Non-goals for version 1**
- iOS version.
- Free-text chat or any user-generated content.
- VPN-based website blocking.
- Device Admin or "uninstall protection".
- Medical or addiction-treatment claims. The App is a productivity tool.

---

## 2. Product principles

1. **The core works on standard APIs.** App blocking, limits, timer and stats must work with no Accessibility permission.
2. **Advanced features are optional and degrade gracefully.** If Accessibility is unavailable, the rest of the App keeps working and explains the state.
3. **Never trap the user.** Emergency calls, the dialer, system settings and the launcher can never be blocked. Every block has a controlled way out.
4. **Privacy by default.** Raw usage data and screen content stay on the device. Only aggregates and account data sync.
5. **Offline first.** Timer, blocking and local stats work without internet.
6. **Honest and calm.** No guilt mechanics, no dark patterns, no excessive notifications.

---

## 3. Feature catalog

Priority key: **P0** is MVP, **P1** is soon after, **P2** is later.

### 3.1 Onboarding and permissions (P0)
- Short value pitch, then goal selection (study, work, reduce social media).
- One permission per screen, each with a plain reason: Usage Access, Display over other apps, Notifications, battery-optimization exemption, and optional Accessibility.
- A dedicated disclosure and consent screen before Accessibility (required by Play policy). The service's own description text cannot replace it.
- Auto-detect when a permission is granted as the user returns from system settings.
- "Skip for now" on every optional step.
- A permanent **Permission Health** screen listing what is on, what is broken, and a one-tap fix. It also shows manufacturer-specific battery guidance.

### 3.2 Focus timer (P0)
- Modes: Pomodoro, custom focus/break, stopwatch, "until a chosen time".
- Subject tags (for example Maths, Coding, Revision) used to group stats.
- Optional ambient sounds.
- Persistent notification with pause and stop.
- End-of-session celebration, XP and coins, optional reflection prompt.
- Break reminders and a longer break after several sessions.

### 3.3 App blocking and limits (P0)
- Choose apps to block, with search, categories (Social, Games, Video) and suggestions.
- Blocking modes: during focus sessions, on a schedule, or always.
- Daily time limits and launch-count limits per app.
- A calm blocker screen with a message, a "Back to focus" button and the remaining session time.
- **Commitment mode** (P1): a cooldown or typed phrase before ending a session early, plus a limited number of daily passes. This replaces hard "strict mode".
- Emergency exit, gated behind a short cooldown and logged in stats.
- Handles apps installed or removed after setup, work profiles and secondary users, and split-screen or picture-in-picture windows.

### 3.4 Usage statistics (P0)
- Daily, weekly and monthly screen time, top apps, unlock and pickup counts.
- Focus time versus distraction time, streak calendar, daily goals and progress rings.
- Weekly report notification (P1).
- The App keeps its own aggregated history, because the system retains raw usage events only briefly.

### 3.5 Advanced blocking (P1, opt-in, uses Accessibility)
- Blocks YouTube Shorts and Instagram Reels while leaving the rest of those apps usable.
- Toggle per app, always on or only during focus sessions.
- Gentle exit action (go back or go to the app's home tab), never any other automated interaction.
- Remote kill switch and remotely updatable detection identifiers.

### 3.6 Study Mode (P1, opt-in, uses Accessibility)
- YouTube: only allowlisted educational channels may play. Others are blocked.
- Channel picker with search and thumbnails, plus a curated starter list by subject and exam.
- Setting for what happens when the channel cannot be read: fail open or fail closed.
- Browser Study Mode with a site allowlist is P2.

### 3.7 Gamification (P0 basic, P1 full)
- Streaks with a forgiving "freeze" mechanic, XP, coins, levels, badges.
- Daily and weekly challenges, a reward shop for themes and sounds, a mascot.

### 3.8 Social (P1)
- Google Sign-In (anonymous start allowed, linkable later).
- Friends, public and private focus rooms with live presence and shared timers.
- Weekly leaderboards for global and friends scopes, invite links.
- Presence-only. No chat in version 1, which avoids user-generated-content moderation duties.

### 3.9 Notifications (P0 basic)
- Session reminders, streak-at-risk nudges, daily goal reminders, weekly report, room invites.
- Quiet hours and per-type toggles. Ask for the notification permission at a sensible moment, not on first launch.

### 3.10 Settings, privacy and account (P0)
- Blocking preferences, themes, language.
- Data export and in-app account deletion (also required on the web if accounts can be created).
- Privacy dashboard that shows what is collected.

### 3.11 Premium (P2)
- Extra limits, advanced stats, more rooms, themes, cloud backup, via Google Play Billing.
- Safety-relevant features such as the emergency exit are never paywalled.

---

## 4. Platform and compatibility targets

| Item | Decision |
|---|---|
| Target SDK | **API 36 (Android 16)**. Google Play requires new apps and updates to target it. |
| Test against | Android 17 (API 37) from the start. Do not target 37 until planned. |
| Minimum SDK | 26 (Android 8) recommended. Confirm against current Firebase and Compose minimums before locking. |
| Form factors | Phones first. Layouts must still adapt on tablets and foldables, because Android 16 and 17 reduce orientation and resizability opt-outs on large screens. |

**Version-sensitive behaviours the agent must respect**
- Android 16+: edge-to-edge cannot be opted out of, and predictive back is on by default. Use proper window-inset handling and the predictive-back APIs.
- Google Play requires 16 KB page-size compatibility. Check every native library, including those inside dependencies.
- Android 14+: every foreground service needs a declared type and matching permission. Prefer types without runtime timeouts.
- Android 13+: notifications need a runtime permission. Sideloaded builds also have "restricted settings", which blocks enabling Accessibility until the user allows it in App Info.
- Android 11+: package visibility rules apply. Use launcher-intent queries and do not request the all-packages permission.
- Android 10+: starting activities from the background is restricted. Show the blocker as an overlay window, not a background-launched activity.
- Android 17: background audio needs a visible activity or a suitable foreground service. Ambient sounds must be started from the foreground and played from the focus service.
- Android 16/17 Advanced Protection Mode can block Accessibility for apps that are not accessibility tools. This App is not an accessibility tool and must not claim to be. Detect the state and show a friendly explanation.

---

## 5. Technology stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose with Material 3 |
| Navigation | Navigation Compose, single activity, type-safe routes, deep links for room invites |
| Architecture | MVVM + clean layering (UI, domain, data) |
| Async | Kotlin Coroutines and Flow |
| Dependency injection | Hilt |
| Local storage | Room (structured data), DataStore (settings) |
| Background work | A foreground service for live blocking and active sessions, WorkManager for sync and daily resets |
| Images | Coil |
| Charts | Vico |
| Backend | Firebase: Authentication, Firestore, Realtime Database (presence only), Cloud Functions, Cloud Messaging, Remote Config, App Check (Play Integrity), Analytics, Crashlytics, Performance Monitoring |
| External API | YouTube Data API v3 (channel picker) |
| Billing | Google Play Billing Library |
| Build | Gradle with Kotlin DSL and a version catalog, Compose BOM, Firebase BoM |
| Quality | JUnit, MockK, Turbine, Compose UI tests, Robolectric, LeakCanary, StrictMode, Macrobenchmark + Baseline Profiles, Detekt/ktlint |
| CI | GitHub Actions |
| Tooling | Firebase Emulator Suite for local backend testing, Firebase Test Lab |

Versions are pinned centrally in the version catalog and updated deliberately (Dependabot or Renovate). Do not add a dependency without noting why in the plan.

---

## 6. Architecture

### 6.1 Layers and dependency direction
- **UI layer:** Compose screens and ViewModels. Depends only on the domain layer.
- **Domain layer:** pure Kotlin use cases and rule logic. It has no Android framework classes, so everything in it is unit-testable.
- **Data layer:** repositories that coordinate local storage, remote services and system APIs behind interfaces.
- **System layer:** Android-specific components (foreground service, optional accessibility service, overlay manager, usage-events reader, notification manager, schedulers). The domain layer reaches them only through interfaces.

Dependencies point inward: UI → domain ← data ← system adapters. Nothing in the domain layer depends on the UI, Firebase or Android.

### 6.2 Module plan
Start as a single module with package-by-feature. Split into modules (core blocking, core data, core UI, and a module per feature) only when build time or clarity justifies it. Features: onboarding, timer, stats, limits, study mode, rooms, profile, premium.

### 6.3 Key system components (conceptual)
- **Focus service:** a foreground service that owns live monitoring while a session or blocking schedule is active. It shows the persistent notification.
- **Foreground-app detector:** uses system usage events to learn which app is on screen.
- **Rules engine:** pure and deterministic. It takes signals and returns a decision.
- **Enforcer:** shows the blocker screen as an overlay and records the outcome.
- **Optional accessibility module:** feeds extra signals (Shorts/Reels screen, YouTube channel) into the same rules engine. It is never the only path to blocking.
- **Health monitor:** continuously evaluates permissions, service state, battery optimization and Advanced Protection Mode, and exposes one health state to the UI.
- **Sync workers:** upload pending local changes and refresh remote data under network constraints.

---

## 7. Blocking engine design

**Signals in:** foreground app changes, time ticks, focus-session state, today's consumed limits, remaining passes, schedule, optional accessibility signals (Shorts/Reels screen detected, YouTube channel name).

**Decision out (one of):** Allow, Block with a reason (session, schedule, limit, feed, study mode), or Warn softly (approaching a limit).

**Priority order (highest first)**
1. Safe list (always allowed).
2. Active emergency pass.
3. Focus-session block.
4. Schedule block.
5. Time or launch limit exceeded.
6. Feed block (Shorts/Reels).
7. Study Mode channel check.

**Safe list (never blockable):** dialer, emergency calling, system settings, the launcher, the App itself, and apps the user marks as essential (for example the messaging app for emergencies).

**Behaviour rules**
- Debounce rapid app switches so the blocker does not flicker.
- Adapt monitoring frequency: faster when a blocked app is likely, slower otherwise, and paused when the screen is off or the device is locked.
- Never trust the device clock alone. Detect large clock or time-zone jumps and handle them.
- Keep identifiers for third-party app screens in remotely updatable configuration with a kill switch.
- Match on several signals for feed and channel detection, not a single identifier.
- Treat every permission as revocable at any moment. If a signal source disappears, degrade and report through the health monitor.
- Never read, log or store on-screen content beyond what a single decision needs, and never persist it.

---

## 8. State management

### 8.1 The five kinds of state
| Kind | Where it lives | Example |
|---|---|---|
| UI state | One ViewModel per screen | Selected tab, dialog open |
| Session and process state | A single repository, shared with the service | Current focus session, blocking status |
| Persistent state | Room and DataStore | Sessions, limits, settings |
| Remote state | Firestore and Realtime Database | Leaderboard, rooms, presence |
| System state | Observed from Android | Permissions, service enabled, Advanced Protection, battery optimization |

### 8.2 Rules
1. **Unidirectional data flow.** The UI sends events up. State flows down.
2. **One immutable state object per screen**, exposed as a stream and collected in a lifecycle-aware way.
3. **One-off effects** (navigation, toast, open settings) are modelled separately from state so they do not replay on rotation.
4. **Single source of truth per datum.** The focus session lives in one place, and both the UI and the service observe it.
5. **State hoisting.** Composables are stateless where possible.
6. **Explicit screen status:** loading, content, empty, error (with retry). No silent failures.
7. **Survive process death.** Save only the minimum needed to restore a screen, and rebuild everything else from persistent storage.
8. **Time as timestamps.** Never count ticks. Compute elapsed time from stored start and pause timestamps, and inject a clock so tests can control time.
9. **No global mutable singletons** for state. Use injected, scoped holders.

### 8.3 Focus session state machine
States: Idle, Running, Paused, Break, Completed, Cancelled, Aborted (killed unexpectedly). Transitions are defined in the domain layer. Every transition is persisted immediately, so after a crash or reboot the session is reconstructed from stored timestamps.

### 8.4 Permission and health state
A single aggregate (all permissions, service state, battery optimization, Advanced Protection) is re-evaluated when the app resumes and periodically while the service runs. It drives the Permission Health screen and any in-app banners.

---

## 9. Caching and offline strategy

**Principle:** local storage is the source of truth for anything the user can see or that affects blocking. The network only syncs and enriches.

| Data | Cache approach | Freshness and invalidation |
|---|---|---|
| Sessions, limits, allowlist, settings | Stored locally first, synced up later | Marked pending until acknowledged by the server |
| Own aggregated stats | Computed locally from stored aggregates | Recomputed on change, and daily rollups kept |
| Leaderboards | Short-lived cache | Starting value: about 5 to 15 minutes while visible, pull-to-refresh |
| Rooms and presence | Live listeners only while the screen is visible | Detach on leave, no persistent caching of presence |
| Remote Config (identifiers, flags) | Last fetched values kept locally | Reasonable fetch interval, always keep last-known-good |
| YouTube channel search | Cached by query | Long expiry (days). Cache channel details aggressively because search quota is limited |
| Images (avatars, thumbnails) | Coil memory and disk cache | Size-capped, cleared on logout where tied to an account |
| Usage events | Not retained long term by the system | Convert to daily aggregates promptly and keep those |

**Sync rules**
- Use WorkManager with a network constraint and back-off. Make uploads idempotent so retries cannot double-count.
- **Conflict policy:** the server wins for anything competitive (XP, streaks, leaderboards). Last write wins for personal settings.
- Enable Firestore's own offline persistence and keep listener usage tight to control cost.
- Cap cache sizes and define clear-on-logout behaviour.
- Anonymous-to-account linking must merge local data without loss.
- Cache metrics (hit rate, sync failures) are logged for debugging.

---

## 10. Backend responsibilities (conceptual)

- **Authentication:** Google Sign-In and optional anonymous start.
- **Firestore:** per-user data, friends, rooms, leaderboards. Security rules give least privilege, and clients can only touch their own data.
- **Realtime Database:** presence only (online, focusing, in a room), with automatic offline cleanup.
- **Cloud Functions:** validate completed sessions (plausible durations, rate limits), update streaks, XP and leaderboards, run weekly resets and streak checks, send notifications. Anything affecting fairness is server-side.
- **Remote Config:** detection identifiers, kill switches, feature flags, fail-open or fail-closed defaults.
- **App Check:** reject traffic that is not from the genuine app.
- **Cost control:** paginate, listen only on visible screens, set budget alerts, and keep a separate dev and production project.

---

## 11. Non-functional requirements

| Area | Requirement |
|---|---|
| Battery | Negligible drain from the blocker. No tight polling or wake locks. Measure on real devices. |
| Reliability | Survives reboot, process death, Doze, OEM task killers and permission revocation. |
| Performance | Fast cold start, smooth charts, small service memory footprint. |
| Security | No secrets in source control. API keys restricted. Rules tested with the emulator. R8 shrinking enabled. |
| Privacy | On-device processing of screen content. Data export and deletion. |
| Accessibility of the App's own UI | TalkBack labels, large-font support, contrast, adequate touch targets. |
| Localization | All strings in resources from day one. Support right-to-left. |
| Observability | Crash and ANR monitoring, onboarding funnel, permission drop-off, blocker-alive rate after 7 days. |

---

## 12. Play Store and compliance

- Target API 36, 16 KB-compatible libraries, edge-to-edge and predictive-back verified.
- Accessibility declaration with a demo video of the disclosure and consent flow. The App is not an accessibility tool and must not set that flag.
- Describe Accessibility use clearly in the store listing.
- Foreground service type declaration with a justification.
- Privacy policy and Data Safety form that match real behaviour.
- Do not request broad permissions that are not needed (all-packages, exact alarms, VPN).
- If minors may use the App, review children's-data rules (India's DPDP Act, GDPR-style rules, and Play's policies) before launch.
- Respect the YouTube API terms of service.
- Check current Play rules on closed-testing requirements for the developer account type.

---

## 13. Testing strategy

**Layers:** unit tests for the domain layer and rules engine (every priority combination), ViewModel tests with controlled time, repository tests with fake sources, Compose UI tests for key flows, Firebase rule tests with the emulator, and device tests.

**Scenarios that must be tested**
Reboot mid-session, force-stop, low memory, battery saver, airplane mode, permission revoked mid-session, Accessibility disabled, Advanced Protection on, time-zone and clock changes, daylight saving, split-screen, picture-in-picture, secondary user and work profile, app update during a session, safe-list apps never blocked, and a full-day battery test.

**Device matrix:** Samsung, Xiaomi/Redmi/Poco, Oppo/realme, Vivo, OnePlus and a Pixel or stock-like device. Android versions: the minimum, 13, 14, 15, 16 and 17.

---

## 14. Milestones and exit criteria

| # | Milestone | Done when |
|---|---|---|
| M1 | Foundation | Builds on API 36, CI green, Firebase connected, crash reporting live |
| M2 | Timer | Survives reboot and process death, notification controls work |
| M3 | Permissions and stats | Permission Health screen, accurate stats, app picker |
| M4 | App blocking | Blocks reliably for a full day on three or more OEM devices, safe list verified |
| M5 | Accounts and sync | Sign-in, offline-first sync, server-validated streaks |
| M6 | Social | Rooms, presence, leaderboards, abuse limits |
| M7 | Advanced blocking | Opt-in flow, kill switch, graceful degradation proven |
| M8 | Release | Policy declarations complete, closed test passed, staged rollout plan |

---

## 15. Risks

| Risk | Mitigation |
|---|---|
| Accessibility revoked or blocked | Core works without it, and the health monitor explains the state |
| YouTube or Instagram UI changes | Remote identifiers, multiple signals, kill switch |
| OEM kills the service | Per-brand guidance, health screen, real-device testing |
| Play rejection | Prominent disclosure, accurate listing, declaration video, no flag misuse |
| Firebase cost spikes | Pagination, scoped listeners, server aggregates, budget alerts |
| Leaderboard cheating | Server-side validation |
| Scope creep | Hold to the P0/P1/P2 priorities |

---

## 16. Open decisions for the owner

1. Final app name and branding (original, not derived from Regain).
2. Confirm minimum SDK after checking Firebase and Compose requirements.
3. Primary launch region and languages.
4. Intended minimum user age and whether minors are a target audience.
5. Monetization plan and timing.
6. Fail open or fail closed as the default for unreadable YouTube channels.
7. Whether to include Browser Study Mode in version 1.x.
