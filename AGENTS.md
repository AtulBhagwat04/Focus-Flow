# AGENTS.md

Standing instructions for every agent working in this repository. For full context, read `PROJECT_BRIEF.md` before planning. If the two disagree, stop and ask the owner.

## Project
Android focus and digital-wellbeing app: focus timer, app blocking with limits and schedules, usage stats, optional Shorts/Reels blocking and YouTube Study Mode, gamification, and social focus rooms. Solo developer. Android only. Category reference is Regain, but never copy its name, logo, mascot, copy or assets.

## Stack
Kotlin, Jetpack Compose with Material 3, Navigation Compose (single activity), Coroutines and Flow, Hilt, Room, DataStore, WorkManager, Coil, Vico. Firebase: Auth, Firestore, Realtime Database (presence only), Cloud Functions, Cloud Messaging, Remote Config, App Check, Analytics, Crashlytics, Performance. YouTube Data API v3. Play Billing. Gradle Kotlin DSL with a version catalog.

## Targets
- targetSdk 36. Test on Android 17. minSdk 26 unless the owner changes it.
- Respect edge-to-edge, predictive back, adaptive layouts, 16 KB page size, and foreground service type declarations.

## Architecture rules
- Layers: UI, domain, data, plus system adapters. Dependencies point inward.
- The domain layer is pure Kotlin with no Android or Firebase classes.
- Package by feature. Split into Gradle modules only when asked.
- Platform services are accessed through interfaces so they can be faked in tests.

## State management rules
- Unidirectional data flow. One immutable UI state object per screen, collected lifecycle-aware.
- One-off effects are separate from state.
- A single source of truth per datum. The focus session lives in one repository shared by the UI and the service.
- Time is stored as timestamps, never as tick counts. Inject a clock.
- Persist every focus-session transition so it can be rebuilt after process death or reboot.
- No global mutable singletons for state.

## Caching and offline rules
- Local storage is the source of truth for anything visible or that affects blocking. The network syncs and enriches.
- Sync through WorkManager with network constraints. Uploads must be idempotent.
- Conflicts: server wins for XP, streaks and leaderboards. Last write wins for personal settings.
- Cache the YouTube search aggressively because the quota is small.
- Firestore listeners only while a screen is visible. Cap cache sizes and clear account-tied caches on logout.

## Blocking safety rules (hard constraints)
- The safe list can never be blocked: dialer, emergency calling, system settings, launcher, this app, and user-marked essentials. Any change to safe-list or rules-engine code needs tests for every priority combination.
- The core features must work without the Accessibility permission. Accessibility is an optional module that feeds extra signals into the same rules engine.
- Treat every permission as revocable at any time. Degrade, report through the health monitor, and never crash.
- This app is not an accessibility tool. Never set the accessibility-tool flag.
- Never log, store or transmit on-screen content. Never use the accessibility service for anything except exiting a blocked screen (go back or go to the app's home tab).
- Detection identifiers for third-party app screens come from remote configuration with a kill switch. Do not hard-code them as the only signal.
- Show the blocker as an overlay, not a background-launched activity.
- Pause monitoring when the screen is off or the device is locked.

## Backend rules
- Anything affecting fairness (XP, streaks, leaderboards) is validated server-side.
- Security rules grant least privilege. Test them with the emulator.
- Keep separate dev and production Firebase projects. Never commit keys or config files containing secrets.
- No free-text chat or user-generated content in version 1.

## Quality rules
- Every feature ships with unit tests for domain logic and ViewModels. Compose UI tests for key flows.
- Test the scenarios in section 13 of `PROJECT_BRIEF.md` when touching the timer, blocking or permissions.
- Strings go in resources. Support large fonts and TalkBack labels.
- Do not add a dependency without stating why in the plan.
- Do not make medical or addiction-treatment claims in any UI copy.

## How to work
1. Plan first. Produce an implementation plan and wait for approval before large changes.
2. Work in small vertical slices that build and pass tests at each step.
3. Prefer the simplest standard API. Ask before using restricted or high-risk permissions.
4. If a requirement is ambiguous or conflicts with these rules, ask the owner instead of guessing.
5. Summarise what changed and what was tested at the end of each task.

## Out of scope for version 1
iOS, VPN website blocking, Device Admin or uninstall protection, chat, root or hidden-API techniques.

## Definition of done
Builds on API 36, tests pass, no new lint or Detekt issues, works offline where required, permission-revoked behaviour handled, and any user-facing change reflected in the privacy and permission disclosures if relevant.
