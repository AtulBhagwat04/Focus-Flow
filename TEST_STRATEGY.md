# Test Strategy

Goal: be able to change blocking, timer and sync code with confidence. The riskiest behaviours (blocking, time, permissions, sync) get the deepest testing.

## 1. Test pyramid
| Level | Scope | Tools | Share |
|---|---|---|---|
| Unit | Domain logic, rules engine, mappers, use cases | JUnit, MockK or hand-written fakes, Turbine for flows | Largest |
| ViewModel | State transitions, effects, error mapping | Coroutine test utilities, controlled clock, Turbine | Large |
| Data and integration | Repositories with fake or in-memory sources, database, sync | Room testing support, Robolectric where suitable | Medium |
| UI | Compose screens, navigation, accessibility semantics | Compose UI tests | Medium |
| Backend | Security rules, Cloud Functions | Firebase Emulator Suite, Functions test tooling | Medium |
| Device | Service, overlay, permissions, OEM behaviour | Real devices, Firebase Test Lab, manual scripts | Focused |
| Non-functional | Performance, battery, accessibility, security | Macrobenchmark, Android Studio profilers, Accessibility Scanner, TalkBack | Per release |

## 2. Principles
1. Test behaviour, not implementation.
2. Prefer fakes over mocks for repositories and platform services.
3. Time is injected. No real sleeping and no real clock in tests.
4. One logical assertion per test, named by behaviour (given/when/then style).
5. Tests are deterministic. A flaky test is a bug and is fixed or quarantined within a week.
6. Every bug fix adds a regression test first.

## 3. Priority coverage
**Rules engine (must be exhaustive).** Every priority level against every other: safe list, emergency pass, session, schedule, limit, feed block, study mode. Edge cases: boundary of a schedule, midnight rollover, limit exactly reached, rapid app switching, missing signals, unknown package, work profile.

**Focus session state machine.** All transitions, including illegal ones, pause and resume accumulation, abort and reconstruction from persisted timestamps after process death.

**Time handling.** Time-zone change, daylight saving, device clock moved forward and backward, midnight reset, week boundaries for leaderboards.

**Permission and health.** Each permission missing, revoked mid-session, restored, Advanced Protection on, accessibility disabled by system.

**Sync.** Offline then online, duplicate delivery, partial failure, conflict resolution, anonymous-to-account merge, large backlog.

**Gamification.** Streak increments, freeze usage, XP rules, server validation rejects implausible sessions.

## 4. Starting quality gates (tune with data)
- Domain layer line and branch coverage: 90% or higher.
- ViewModels: 80% or higher.
- Overall: 70% or higher. Coverage is a floor, not the goal.
- Zero new lint, static-analysis or accessibility-scanner errors.
- Crash-free sessions target before wide rollout: 99.5% or higher in closed testing.

## 5. Device and version matrix
| Axis | Coverage |
|---|---|
| Android versions | minimum supported, 13, 14, 15, 16, 17 |
| OEMs | Samsung, Xiaomi/Redmi/Poco, Oppo/realme, Vivo, OnePlus, Pixel or stock-like |
| Form factors | small phone, large phone, tablet or foldable (at least emulator) |
| Settings | battery saver on, data saver on, large font, dark theme, TalkBack on, reduced motion |

## 6. Mandatory scenario scripts (manual or device automation)
1. Start session, reboot phone, confirm session state and blocking recover.
2. Force-stop app mid-session, reopen, confirm consistent state.
3. Revoke each permission mid-session, confirm graceful degradation and health banner.
4. Disable accessibility service, confirm core blocking continues.
5. Enable Advanced Protection Mode (Android 16 or later), confirm explanation and fallback.
6. Open emergency dialer, system settings, launcher during a session, confirm never blocked.
7. Split-screen and picture-in-picture with a blocked app.
8. Secondary user and work profile.
9. Change time zone, clock and cross daylight saving.
10. Airplane mode through a full session and sync afterwards.
11. App update installed mid-session.
12. Full-day battery run with blocking active and screen mostly off.
13. Low memory kill, then return.
14. Rotate, fold and multi-window during the blocker screen.
15. Uninstall and reinstall, then sign in, confirm restore behaviour.

## 7. UI and accessibility testing
- Compose tests assert semantics (labels, roles, states), not pixels.
- Screenshot tests (for example with a screenshot-testing library) guard the design system across light, dark, large font and tablet.
- Before each release: TalkBack walk-through of onboarding, timer, blocker, Stats and Settings, plus Accessibility Scanner on every screen.

## 8. Performance and battery
- Macrobenchmark for cold start and key navigation, with baseline profiles.
- Measure service CPU, wakeups and battery over a full day on at least two devices.
- Budget: service must stay idle-light when the screen is off. Any regression is a release blocker.
- Track block-decision latency so the blocker appears quickly after a blocked app opens.

## 9. Backend testing
- Security rules: every collection tested for allowed and denied access, including cross-user access.
- Functions: validation of sessions (plausible durations, rate limits), idempotency, streak logic across time zones.
- Cost guard: tests or checks that listeners detach and queries are paginated.

## 10. Security testing
Dependency vulnerability scanning, secrets scanning in CI, manual review against OWASP MASVS categories, and a tamper and bypass review of the blocker (force-stop tricks, clock changes, task-switching).

## 11. Release testing
Closed-test cohort across the device matrix, Play pre-launch report review, staged rollout with live monitoring of crash and ANR rates by device brand and OS version.

## 12. Bug triage
- **S1** blocks emergency or system apps, data loss, crash loop: stop release.
- **S2** blocker fails or battery regression on a major device: fix before release.
- **S3** feature degraded with a workaround: schedule.
- **S4** cosmetic: backlog.

## 13. Definition of test-done for a feature
Acceptance criteria mapped to tests, domain and ViewModel tests passing, UI semantics tested, accessibility checked, regression tests for known bugs, scenario scripts relevant to the feature executed on at least two OEM devices.
