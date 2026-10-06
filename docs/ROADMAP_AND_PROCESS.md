# Roadmap and Working Process

## 1. Way of working (solo, senior discipline)
- **Plan, build, verify, document** in small vertical slices.
- Trunk-based development with short-lived branches and pull requests, reviewed against `.agents/workflows/code-review.md` even when working alone.
- Conventional commit messages. Tag releases.
- ADR for every hard-to-reverse decision, stored in `docs/adr/` using a short template: context, decision, alternatives, consequences.
- Keep a running `docs/CHANGELOG.md` for user-visible changes.

## 2. Definition of ready
Acceptance criteria written, UI states designed, analytics events named, permissions and disclosures identified, test approach outlined, risks noted.

## 3. Definition of done
Builds on API 36, unit and ViewModel tests added, UI semantics tested, accessibility checked, works offline where required, permission-revoked and low-battery behaviour handled, strings extracted, privacy and permission disclosures updated if needed, docs updated, code review checklist completed, no new lint or static-analysis issues.

## 4. Milestones with work breakdown
Durations are rough guidance for one developer working part time and must be re-estimated after M1.

### M1 Foundation (about 1 week)
Project setup, version catalog, CI, code style tooling, design system skeleton, Firebase dev and staging projects, crash reporting, base navigation, theme with light and dark. **Done when:** a hello-screen builds on API 36, CI passes, crash reporting receives a test event.

### M2 Focus timer (about 2 weeks)
State machine, persistence, foreground service, notification controls, session history, subject tags, ambient audio (foreground-service aware for Android 17). **Done when:** session survives reboot, process death and Doze in scenario scripts.

### M3 Permissions and stats (about 2 weeks)
Onboarding with disclosure screens, permission and health checker, usage reader with daily aggregation, app picker, stats screens and charts. **Done when:** Permission Health shows accurate states, stats match the system's figures within an explained tolerance.

### M4 App blocking (about 3 weeks)
Rules engine with exhaustive tests, foreground-app detector, overlay enforcer, safe list, limits and schedules, commitment mode, emergency exit. **Done when:** full-day reliability on three OEM devices and safe-list manual verification passes.

### M5 Accounts and sync (about 2 weeks)
Sign-in, anonymous linking, pending-change queue, server-validated sessions and streaks, security rules and tests, data export and deletion. **Done when:** offline-then-online scenarios pass and rules tests are green.

### M6 Social (about 3 weeks)
Presence, rooms, friends, leaderboards, notifications, abuse limits, privacy controls. **Done when:** two devices see each other's presence reliably and costs per active user are measured.

### M7 Advanced blocking and Study Mode (about 3 weeks)
Accessibility module with disclosure, Shorts and Reels signals, channel allowlist, remote identifiers, kill switch, Advanced Protection handling. **Done when:** feature degrades safely in every failure scenario and a remote switch disables it.

### M8 Polish and release (about 3 weeks)
Premium (optional), localisation, accessibility pass, performance and battery pass, store assets, declarations, closed test, staged rollout. **Done when:** release checklist in `RELEASE_AND_COMPLIANCE.md` is complete.

## 5. Risk register (review at each milestone)
Accessibility restrictions, third-party UI changes, OEM background kills, policy rejection, cost growth, scope creep, burnout. Mitigation owners and triggers are recorded in the brief.

## 6. Time-boxing and scope control
- Anything beyond the milestone's exit criteria goes to a parking-lot list.
- If a task exceeds twice its estimate, stop and reassess scope or approach.
- Ship the smallest version of each feature that meets its acceptance criteria.

## 7. Pull request template (contents)
What and why, screenshots or recordings for UI, test evidence, accessibility notes, permission or policy impact, risk and rollback, checklist link.
