# Rule: Blocking safety (hard constraints)

These rules protect users and the app's Play standing. Do not relax them without explicit owner approval and an ADR.

1. **Safe list is absolute.** Dialer, emergency calling, system settings, launcher, this app, and user-marked essentials can never be blocked. Any change to the safe list or the rules engine needs tests for every priority combination.
2. **Core works without Accessibility.** App blocking, limits, schedules and the timer must function with only Usage Access, overlay and notification permissions.
3. **Accessibility is optional and degradable.** It adds signals to the same rules engine. If it disappears, the health monitor reports it and everything else continues.
4. **Never trap the user.** Every block has a visible way back and a controlled emergency exit. The blocker never disables system navigation.
5. **Treat permissions as revocable.** Re-check on resume and periodically. Degrade gracefully and never crash.
6. **Overlay, not background activity launch.** The blocker is an overlay window.
7. **Battery discipline.** Pause monitoring when the screen is off or the device is locked. No tight polling loops. No wake locks without justification.
8. **Do not trust the device clock alone.** Detect large clock or time-zone jumps.
9. **Remote identifiers with a kill switch.** Third-party screen identifiers come from remote configuration, with multiple signals and a remote kill switch. Never hard-code a single identifier as the only signal.
10. **Minimal actions.** The only automated actions are going back or going to the target app's home area. No clicking or typing in other apps.
11. **Honest messaging.** Never claim the app can absolutely prevent bypassing. It supports the user's intent.
12. **Decision priority order:** safe list, emergency pass, session block, schedule block, limit exceeded, feed block, study mode.
