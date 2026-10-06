# Workflow: Yearly platform update

Run each spring when new Android betas appear, and finish before the Play target deadline.

1. Read the Android behaviour-change pages for all apps and for apps targeting the new level. Read the current Google Play target API requirement and policy updates.
2. Create a list of changes that affect this app: services, permissions, overlays, usage access, accessibility, notifications, audio, layout, and security.
3. Update `docs/DEVICE_COMPATIBILITY.md`, `docs/RELEASE_AND_COMPLIANCE.md` and `AGENTS.md` where facts changed. Mark what was verified and the date.
4. Plan the migration as an implementation plan using `/plan-feature`.
5. Update compile and target SDK, dependencies, and tooling in a dedicated branch.
6. Fix deprecations and breaking behaviour. Add regression tests.
7. Run the full scenario scripts on the new OS version and an older one.
8. Release through the normal checklist and monitor by OS version.
