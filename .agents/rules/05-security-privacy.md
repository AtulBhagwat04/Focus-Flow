# Rule: Security and privacy

Source of truth: `docs/SECURITY_PRIVACY.md`.

- Never commit secrets, keys or config files that contain them. Use restricted API keys and the staging project for development.
- Never log, store or transmit screen content, other apps' text, viewing history or personal data. The logging policy allows event names and anonymised technical context only.
- The accessibility module listens only to the specific target packages, reads only the single value needed for a decision, discards it immediately, and performs only the exit action.
- This app is not an accessibility tool. Never set the accessibility-tool flag.
- Add no permission, SDK or data collection without an ADR and an update to the disclosures, privacy policy and Data Safety notes.
- Anything affecting fairness (XP, streaks, leaderboards) is validated server-side. Never trust client-supplied scores.
- Backend rules grant least privilege and have tests for allowed and denied access.
- Use the platform-recommended secure storage for tokens and sensitive settings. Check current guidance for deprecations.
- Release builds use R8 shrinking and obfuscation and must be tested after enabling.
- Review new dependencies for maintenance status, license and vulnerabilities before adding.
- Minors: do not add features that collect data from minors without reviewing `docs/SECURITY_PRIVACY.md` section 6.
- If you discover a possible vulnerability or privacy leak, stop and tell the owner before continuing.
