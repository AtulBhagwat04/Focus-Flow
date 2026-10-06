# Workflow: Senior code review

Review the diff as a skeptical senior Android engineer. Report findings grouped by severity: blocker, major, minor, nit.

**Correctness and safety**
- Does any change affect the safe list, rules engine priority, or emergency exit? Are tests exhaustive?
- Can any path trap the user or block an emergency or system app?
- Time handling: timestamps, time zone, clock changes, midnight rollover.
- Process death, reboot and permission revocation handled?

**Architecture**
- Layer boundaries respected, domain free of Android classes, dependencies injected, scopes correct.
- One source of truth, no duplicated state, no global mutable state.

**Concurrency**
- Dispatchers injected, no blocking on main, cancellation handled, no leaked scopes or listeners.

**UI and UX**
- All states present, design tokens used, strings extracted.
- Accessibility: labels, order, contrast, 48 dp targets, 200% font, reduced motion.
- Dark theme, large screens, edge-to-edge and predictive back.

**Testing**
- Tests at the right level, deterministic, meaningful assertions, regression test for bug fixes.

**Security and privacy**
- No secrets, no personal data in logs, accessibility module limits respected, disclosures updated.

**Performance and battery**
- No polling without screen-state awareness, no heavy work on the main thread, no recomposition traps, reasonable listener and query costs.

**Backend**
- Server-authoritative competitive values, idempotency, rules tests, cost estimate.

**Maintainability**
- Naming, size, comments, dead code, dependency additions justified.

End with: verdict (approve, approve with changes, request changes) and the three highest-risk items.
