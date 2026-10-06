# Security and Privacy

## 1. Principles
Data minimisation, on-device processing of sensitive signals, least privilege everywhere, server authority for anything competitive, transparency to users.

## 2. Data classification
| Class | Examples | Handling |
|---|---|---|
| Sensitive, device-only | Screen content seen by the accessibility module, raw usage events, installed-app list | Never leaves the device. Never logged. Discarded after the decision, or aggregated. |
| Personal, synced | Account identity, display name, photo, friends, room membership | Stored in the backend, protected by rules, deletable by the user |
| Derived, synced | Daily focus totals, streaks, XP, leaderboard entries | Written by the server after validation |
| Operational | Crash reports, performance, analytics events | No personal data and no screen content |
| Secrets | API keys, signing keys | Never in source control or the app bundle where avoidable. Restricted by package and certificate. |

## 3. Threat model (summary)
| Threat | Example | Controls |
|---|---|---|
| User bypasses the blocker | Force-stop, clock change, revoke permission | Health monitor, timestamp validation, commitment mode, honest messaging that the app supports intent rather than enforcing it absolutely |
| Cheating on leaderboards | Fake sessions, scripted clients | Server-side validation, rate limits, App Check, anomaly flags |
| Account or data access abuse | Reading another user's data | Least-privilege rules, rules tests, no trust in client claims |
| Reverse engineering | Extracting identifiers | R8, remote configuration, no secrets in the client |
| Malicious overlay or permission abuse | Our own overlay misused | Minimal overlay scope, no input capture, clear disclosure |
| Supply-chain risk | Vulnerable dependency | Pinned versions, vulnerability scanning, review of new dependencies |
| Privacy leak | Logs or analytics containing content | Logging policy, event allowlist, reviews |

## 4. Required controls
- Backend access rules grant users access only to their own data. Rooms and leaderboards expose only the minimum public fields.
- Enable App Check with Play Integrity. Treat it as a speed bump, not proof of trust.
- All network traffic over TLS. Follow the platform's network security configuration defaults.
- Store tokens and sensitive local settings using the platform-recommended secure storage (check current Jetpack security guidance because some older libraries are deprecated).
- Enable R8 shrinking and obfuscation in release. Verify the release build still works.
- Restrict every API key by package name and signing certificate. Set quotas and budget alerts.
- Secrets scanning and dependency vulnerability scanning in CI.
- Separate development and production backends and keys.

## 5. Accessibility service rules (security view)
- Listen only to the specific packages that need it.
- Never read, store or transmit text from other apps beyond the single value needed for a decision (for example a channel name), and discard it right after.
- Perform only the exit action. No clicking, typing or reading of other content.
- Show the prominent disclosure and record the user's consent.
- Honour Advanced Protection Mode and the user's decision to disable.

## 6. Privacy requirements
- Privacy policy matches actual behaviour and is linked in the app and the store listing.
- Play Data Safety form is accurate and updated with each release that changes data practices.
- In-app account and data deletion, with a web path if accounts can be created. Define retention and deletion timelines.
- Data export for the user.
- Analytics consent and a way to opt out.
- If minors may use the app: review children's data rules (India's DPDP Act, GDPR-style rules, Play's policies) before launch, and consider an age gate and parental-consent design. Verify current legal requirements with a qualified source, since this document is not legal advice.
- Social features: profile visibility controls, ability to leave leaderboards, block or hide users, report mechanism if any user-generated content is ever added.

## 7. Logging policy
Allowed: event names, anonymised identifiers, OS version, OEM, health state, error categories. Forbidden: screen text, app contents, channel viewing history, contacts, precise location, any credential.

## 8. Incident response (solo-developer version)
1. Detect through crash, vitals or user report.
2. Contain with a Remote Config kill switch or halting the rollout.
3. Fix and verify.
4. Release a patch with a staged rollout.
5. If personal data was exposed, assess notification duties and notify affected users and regulators where required.
6. Record the cause and prevention in an ADR or post-mortem note.

## 9. Pre-release security checklist
Rules tests passing, no secrets in repository history, keys restricted, release build obfuscated and tested, permissions minimal and justified, disclosures present, logs reviewed for personal data, dependencies scanned, bypass review completed.
