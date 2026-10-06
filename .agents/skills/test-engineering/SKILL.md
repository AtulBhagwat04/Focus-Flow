---
name: test-engineering
description: Use when planning, writing, reviewing or debugging tests, defining coverage, handling time in tests, testing ViewModels, Compose UI, Firebase rules, or device scenarios.
---

# Test engineering

## Choose the level
- Pure logic: JVM unit test with fakes.
- State and effects: ViewModel test with controlled dispatchers and time, assert state sequences.
- Data and sync: repository tests with in-memory or fake sources, include duplicate delivery and offline cases.
- UI: Compose semantics tests, plus screenshot tests for the design system.
- Backend: emulator-based rules tests (allowed and denied) and Functions tests.
- Device: scenario scripts from `docs/TEST_STRATEGY.md`.

## Procedure
1. Turn each acceptance criterion into at least one test name written as behaviour.
2. Write the failing test first for logic and bugs.
3. Use hand-written fakes for repositories and platform services. Inject the clock and dispatchers.
4. Cover boundaries: zero, one, many, midnight, time-zone change, daylight saving, permission missing, offline.
5. Make tests deterministic: no sleeping, no real time, no network.
6. Run the suite and the relevant scenario scripts. Report exactly what was run and the outcome.

## Flaky tests
Reproduce, find the shared state or timing assumption, fix it. If it cannot be fixed immediately, quarantine it with an issue and a deadline of one week.

## Quality gates
Domain 90%, ViewModels 80%, overall 70% as floors. No new lint or accessibility errors. Crash-free target before rollout.

## Pitfalls
Over-mocking, asserting implementation details, tests that depend on execution order, forgetting illegal state transitions, forgetting reboot and process-death recovery, testing only the happy path.
