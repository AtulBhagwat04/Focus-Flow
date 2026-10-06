# Rule: Testing

Source of truth: `docs/TEST_STRATEGY.md`.

- No feature or bug fix is complete without tests at the right level.
- Write domain tests first for rules, state machines and calculations.
- The rules engine requires exhaustive tests across every priority combination and the edge cases listed in the strategy.
- Prefer hand-written fakes over mocks for repositories and platform services. Use mocks sparingly.
- Inject time. Tests never sleep and never read the real clock.
- ViewModel tests assert state sequences and one-off effects using controlled dispatchers.
- Compose tests assert semantics (labels, roles, states), not pixel positions.
- Backend security rules and Cloud Functions are tested with the emulator, including denied access cases.
- Every bug fix begins with a failing regression test.
- Tests must be deterministic. Quarantine a flaky test immediately and fix it within a week.
- Name tests by behaviour so a failure reads as a sentence.
- Coverage floors: domain 90%, ViewModels 80%, overall 70%. Coverage is a floor, not the goal.
- When touching the timer, blocking, permissions or sync, run the relevant scenario scripts from the strategy and report which were run.
- Never disable or delete a failing test to make a build pass without explicit owner approval.
