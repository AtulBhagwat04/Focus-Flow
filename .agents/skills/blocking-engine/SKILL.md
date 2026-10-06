---
name: blocking-engine
description: Use when designing, changing or testing the app-blocking pipeline, rules engine, safe list, limits, schedules, focus service, overlay blocker, or the optional accessibility module.
---

# Blocking engine engineering

## Mental model
Signals go into a pure rules engine, which returns a decision, which an enforcer applies. The engine knows nothing about Android. Adapters feed it signals and apply decisions.

## Signals
Foreground app change, time tick, focus session state, today's consumed limits, remaining passes, schedule, and optional accessibility signals (feed screen detected, channel name read).

## Decisions
Allow, block with reason (session, schedule, limit, feed, study mode), or soft warning near a limit.

## Priority order
Safe list, emergency pass, session, schedule, limit, feed, study mode. Higher priority always wins.

## Procedure for any change
1. State which signals and priorities are affected.
2. Write exhaustive domain tests first: every priority pair, boundaries, midnight rollover, unknown package, work profile, missing signals, rapid switching.
3. Change the rules engine only in the domain layer. Adapters stay thin.
4. Verify the safe list cannot be blocked by any combination.
5. Check battery: monitoring pauses when the screen is off or locked, no tight polling, adaptive frequency.
6. Check degradation: what happens if Usage Access, overlay or accessibility disappears mid-session. The health monitor must report it, and nothing crashes.
7. Run the device scenarios for blocking from `docs/TEST_STRATEGY.md`.

## Accessibility module rules
Optional, opt-in, target packages only, minimal data, exit action only, remote identifiers with multiple signals and a kill switch, honour Advanced Protection Mode, show the disclosure before enabling.

## Pitfalls
Flicker on rapid app switches (debounce), blocker launched as a background activity (use an overlay), trusting the device clock, hard-coded third-party identifiers, unbounded logging, forgetting split-screen and picture-in-picture, forgetting secondary users and work profiles.
