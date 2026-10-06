# Rule: Architecture

Applies to all code changes. Full reasoning: `docs/ARCHITECTURE.md`.

- Keep the layering UI, domain, data, system adapters. Dependencies point inward only.
- The domain layer is pure Kotlin. It must not import Android, Compose, Room or Firebase classes.
- Access platform and backend services through interfaces defined in the domain layer so tests can substitute fakes.
- Package by feature under the planned structure. Do not create Gradle modules unless the task says so or an ADR approves it.
- Use constructor injection with Hilt. No service locators, no static access to singletons.
- Inject dispatchers and the clock. Never call the system clock or hard-code a dispatcher in logic.
- Use structured concurrency. No global scope. The service owns a supervised scope cancelled on destroy.
- Expected failures return explicit results. Map error categories (network, permission, policy, validation, conflict, unknown) to user messages and recovery actions.
- Do not swallow exceptions. Report unexpected ones to crash monitoring without personal data.
- One source of truth per datum. The focus session lives in one repository observed by both the UI and the service.
- Time is stored as timestamps. Never count ticks.
- Local storage is the source of truth. The network syncs and enriches.
- New permissions, dependencies of substance, data-retention changes, or module splits require an ADR in `docs/adr/` before implementation.
- Keep functions small and files focused. If a class has more than one reason to change, split it.
- Never introduce the anti-patterns listed in section 14 of `docs/ARCHITECTURE.md`.
