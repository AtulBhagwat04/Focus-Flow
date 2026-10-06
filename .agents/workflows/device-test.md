# Workflow: Device test pass

Run before milestones M4 onward and before every release. The owner performs the physical steps. The agent prepares the sheet and records results.

1. Create a test report listing device model, OEM, Android version, build number, and settings (battery saver, dark theme, font scale, TalkBack).
2. For each scenario in `docs/TEST_STRATEGY.md` section 6, provide step-by-step instructions, expected result, and a pass/fail/notes field.
3. Include the OEM-specific battery steps from `docs/DEVICE_COMPATIBILITY.md` and record whether the blocker survived after them.
4. Record battery drop and service wake-ups over a defined period with the screen mostly off.
5. Record any crash, ANR, or unexpected block of a safe-list app as severity S1.
6. Summarise results by device and by scenario, list failures with severity per section 12 of the strategy, and propose fixes or tasks.
7. Update the compatibility notes with anything new that was learned.
