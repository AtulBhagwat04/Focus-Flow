# Workflow: Plan a feature

Use before any non-trivial work. Do not write code in this workflow.

1. Read `PROJECT_BRIEF.md`, `AGENTS.md`, and the docs and rules relevant to the feature.
2. Restate the goal and the user value in two sentences.
3. List acceptance criteria that are testable and unambiguous.
4. Identify affected layers, components, permissions, data kept on device versus synced, and any Play or privacy impact.
5. Identify UI states needed (loading, empty, offline, error, permission problem) and accessibility needs.
6. Identify risks and unknowns. Propose how to resolve each (spike, owner decision, ADR).
7. Break the work into small vertical slices, each buildable and testable on its own, with a suggested order.
8. Write the test plan: unit, ViewModel, UI, backend, device scenarios to run.
9. Estimate cost impact for backend work (reads, writes, listeners per active user per day) and battery impact for background work.
10. Output an implementation plan and wait for explicit owner approval before proceeding.

Output format: goal, acceptance criteria, design notes, slices, test plan, risks, open questions.
