# Device and Android Version Compatibility

Rows marked (verified Oct 2026) were checked against current Android and Google Play sources. Others are established platform behaviour that you must re-verify on the current Android documentation and on real devices. OEM setting names change between OS versions.

## 1. Android version behaviours that affect this app
| Android | Impact on this app | Required handling |
|---|---|---|
| 8 to 9 (API 26 to 28) | Background service limits, notification channels | Foreground service with channels, overlay window type for the blocker |
| 10 to 11 (API 29 to 30) | Background activity launch restricted, package visibility | Blocker as overlay, launcher-intent queries, no all-packages permission |
| 12 (API 31) | Foreground service start from background restricted | Start the service from a visible user action, handle failure gracefully |
| 13 (API 33) | Notification runtime permission, restricted settings for sideloaded apps | Ask at a sensible moment, guide testers through App Info |
| 14 (API 34) | Foreground service types mandatory, exact alarms off by default | Declare type and permission, avoid exact alarms unless justified |
| 15 (API 35) | Edge-to-edge enforced for targeting 35, some foreground service types have time limits | Insets handled, choose a service type without a timeout, check boot-start restrictions |
| 16 (API 36) (verified Oct 2026) | Edge-to-edge cannot be opted out, predictive back on by default, orientation and resizability restrictions on large screens ignored, 16 KB page-size mode, Advanced Protection Mode introduced | Required target. Adopt predictive-back APIs, adaptive layouts, 16 KB-safe libraries |
| 17 (API 37) (verified Oct 2026) | Background audio needs a visible activity or a suitable foreground service (and while-in-use capability for apps targeting 37), local-network permission mandatory for apps targeting 37, static final fields cannot be modified for apps targeting 37, large-screen opt-outs removed for apps targeting 37, Advanced Protection can block accessibility for non-accessibility-tool apps | Test now. Start ambient audio from the foreground service. Check dependencies for reflection on static fields |

## 2. Advanced Protection Mode (verified Oct 2026)
When the user turns it on, apps that are not accessibility tools lose access to the accessibility API and cannot be granted it until the mode is off. Google provides an API to detect the mode. This app must detect it, explain it kindly, and continue with core blocking.

## 3. OEM behaviour matrix
Aggressive battery management is the top real-world failure cause. Build in-app guidance for each brand and test on real devices. Setting names below are indicative and vary by version.

| OEM family | Typical hazards | What to guide users to |
|---|---|---|
| Samsung | Sleeping and deep-sleeping app lists, adaptive battery | Remove the app from sleeping lists, set battery usage to unrestricted |
| Xiaomi, Redmi, Poco | Autostart toggle, battery saver profiles, background pop-up permission for overlays | Enable autostart, set battery saver to no restrictions, allow background pop-ups and display over other apps |
| Oppo, realme, OnePlus | App auto-launch, background activity limits, battery optimisation | Allow auto-launch and background activity, disable optimisation for the app |
| Vivo | High background power consumption allowlist, autostart | Add the app to the allowlist and enable autostart |
| Pixel and stock-like | Standard Doze and app standby buckets | Exempt from battery optimisation where the app justifies it |

For each guide: screenshots per OS version, a deep link to the relevant system screen where one exists, and a "how to check it worked" step.

## 4. Cross-cutting hazards
- Doze and app standby buckets reduce background work. The blocker relies on the foreground service, not on timers alone.
- The system may kill processes at any time. Recover from persisted timestamps.
- Multi-user, work profile and dual-app features create distinct app identities. Handle them without crashing.
- Split-screen and picture-in-picture can keep a blocked app visible. Test and define behaviour.
- Different keyboards, languages and font scales stress layouts.
- Different default launchers can change what "home" means for the exit action.

## 5. Support policy
- Minimum supported version is set in the brief (recommended API 26). Reconsider yearly using real install data.
- Every release is tested on the minimum version, the latest stable version and one in between.
- Every release is tested on at least three OEM families.

## 6. Verification routine for each new Android release
1. Read the behaviour-change pages for all apps and for apps targeting the new level.
2. Install the beta on a test device and run the scenario scripts in `TEST_STRATEGY.md`.
3. Log findings as ADRs or tasks, update this document and `AGENTS.md` if rules change.
