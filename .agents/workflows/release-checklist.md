# Workflow: Release checklist

1. Confirm target and compile SDK, 16 KB compatibility, and edge-to-edge and predictive-back behaviour.
2. Confirm all tests, lint and static analysis pass in CI on the release commit.
3. Build the release bundle. Install it on a clean device and run the first-run flow end to end, including permission and disclosure screens.
4. Verify the safe list manually on the release build.
5. Verify Remote Config defaults and kill switches work.
6. Run the device test pass on at least three OEM families.
7. Review privacy policy, Data Safety answers, permission declarations, foreground service declaration and the accessibility declaration video against the actual build. Fix any mismatch.
8. Review store listing text for accurate claims and no copied assets.
9. Confirm analytics, crash alerts and dashboards are live.
10. Prepare release notes and the rollback plan.
11. Promote through internal, closed, then a staged production rollout. Define the metrics and thresholds that pause the rollout.
12. After release: watch crash, ANR, onboarding drop-off, blocker-alive rate and cost for the first 72 hours, and record learnings.
