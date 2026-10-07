# Google Play Data Safety Form Mapping

This reference document maps FocusFlow's data practices directly to questions on the Google Play Console Data Safety questionnaire.

---

## 1. Overview of Data Architecture
FocusFlow is engineered with a **local-first, privacy-by-design architecture**:
- **On-Device Only**: Detailed per-app usage minutes, app launch counts, individual app blocking limits, safe list exemptions, and screen interaction events are processed and stored exclusively in a local SQLite (Room) database.
- **Cloud-Synced (Firebase)**: Only high-level aggregates (total daily focus minutes, streak days, XP, level, public focus room metadata, and friend relationships) are synchronized to the cloud.

---

## 2. Play Console Questionnaire Answers

### Data Collection & Sharing Summary
- **Does your app collect or share any user data?** Yes (App Functionality / Account Sync).
- **Is all user data collected by your app encrypted in transit?** Yes (All network traffic uses TLS/HTTPS).
- **Do you provide a way for users to request that their data be deleted?** Yes (In-app deletion on Profile screen + web deletion request URL).

### Data Types Breakdown

| Data Type | Collected? | Shared? | Purpose | Optional / Required |
|---|---|---|---|---|
| **Personal Info (Name, Email)** | Optional | No | Account management, cross-device sync | Optional (Anonymous guest mode supported) |
| **User IDs (Firebase UID)** | Yes | No | Account management, progress restoration | Required for cloud backup |
| **App Activity (Aggregate Focus Minutes, Streaks)** | Yes | No | Gamification, leaderboard ranking, streak tracking | Optional (Can use purely offline) |
| **App Activity (Raw per-app usage / Screen time)** | **No** | **No** | *Kept 100% on device; never transmitted.* | N/A |
| **Location (Precise / Approximate)** | **No** | **No** | Not used | N/A |
| **Photos and Videos** | **No** | **No** | Not used | N/A |
| **Audio Recordings** | **No** | **No** | Not used (Ambient sounds are played, not recorded) | N/A |
| **Web Browsing / Search History** | **No** | **No** | Not used | N/A |
| **On-screen content / Keystrokes** | **No** | **No** | *Strictly prohibited; zero inspection.* | N/A |
| **Financial / Payment Info** | No | No | Handled entirely by Google Play Billing | N/A |

---

## 3. Account Deletion Mechanism
Under Google Play policy, apps offering account creation must support account and data deletion:
1. **In-App**: The user navigates to Profile > Data Management > "Delete Account & Cloud Data". Upon confirmation, this removes their Firebase user document, session subcollections, friend links, and deletes the authentication credential.
2. **Web URL**: A dedicated web endpoint (`https://focusflow-app.web.app/delete-account`) allows users who have uninstalled the app to submit an account deletion request.
