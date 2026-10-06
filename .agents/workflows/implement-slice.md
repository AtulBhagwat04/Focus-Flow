# Workflow: Implement a slice

Use after a plan is approved. One slice at a time.

1. Restate the slice and its acceptance criteria. Confirm the files and layers you will touch.
2. Write failing tests first for domain logic and ViewModel behaviour.
3. Implement the minimum code to pass, respecting all rules in `.agents/rules`.
4. Add or update UI with all states, previews and accessibility semantics.
5. Extract all strings and use design tokens.
6. Run the build, lint, static analysis and the full relevant test suite. Fix everything introduced by the change.
7. If the slice touches the timer, blocking, permissions, time handling or sync, run the matching scenario scripts from `docs/TEST_STRATEGY.md` and report results.
8. Check disclosures and docs: update permission or privacy text, ADRs and the changelog if needed.
9. Run `/code-review` on your own change and resolve findings.
10. Summarise: what changed, what was tested (with results), what was not tested and why, follow-ups.

Stop and ask if: the work needs a new permission, dependency, module, or data collection, or if requirements conflict with a rule.
