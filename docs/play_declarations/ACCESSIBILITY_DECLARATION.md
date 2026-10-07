# Google Play Accessibility API Declaration

## 1. App Details & Declared Purpose
- **App Name**: FocusFlow
- **Category**: Productivity / Digital Wellbeing
- **Declared API**: Android AccessibilityService (`FocusAccessibilityService`)
- **Package Scope**: Strictly limited to `com.google.android.youtube` and `com.instagram.android`.
- **Accessibility Tool Designation**: **NOT DESIGNATED**. The `isAccessibilityTool="true"` attribute is strictly omitted.

---

## 2. Policy Compliance & Core Justification
Under Google Play Accessibility API Policy, apps not eligible for the accessibility-tool designation may use the API only if they provide prominent disclosure, obtain explicit user consent, and use the API strictly to provide user-facing functionality requested by the user.

### Why FocusFlow requires this API:
1. **Short-Form Feed Blocking (Shorts & Reels)**:
   Users seeking deep study and focus struggle with algorithmic infinite-scroll short-form video feeds. FocusFlow uses window content detection to identify when a YouTube Short or Instagram Reel container is opened and executes a standard system Back navigation (`GLOBAL_ACTION_BACK`) to return the user to their study material without closing the parent app completely.
2. **YouTube Study Mode**:
   During active focus sessions, users allow only educational channels (e.g. Khan Academy, MIT OpenCourseWare). The service inspects the channel name of playing videos against the user's local allowlist.

### Explicit Privacy Guarantees:
- **Zero Content Logging / Storage**: No keystrokes, personal messages, video content, or screen images are logged, recorded, or transmitted to any server.
- **Exit Action Only**: The service never automates touches, clicks, form inputs, or advertisements; it only triggers standard system back or home navigation.
- **Remote Kill Switch**: Includes an emergency remote kill switch to disable detection if third-party app view structures change.

---

## 3. Reviewer Video Demonstration Script
*Video duration: ~45 seconds, demonstrating the full disclosure and user journey.*

1. **Step 1: In-App Feature Discovery**
   - User navigates to the "App Blocking & Study Mode" screen and selects "Advanced & Feeds".
   - User taps the "Setup" button next to "Accessibility required for feed detection".
2. **Step 2: Prominent Pre-Consent Disclosure**
   - The app displays the dedicated `AccessibilityDisclosureDialog`.
   - The dialog clearly states:
     - "Why FocusFlow uses Accessibility" (Detecting Shorts/Reels feeds and YouTube Study Mode educational channels).
     - "Privacy Guarantee" (Zero keystrokes or screen data recorded; no data transmitted).
   - User explicitly taps "Open Settings".
3. **Step 3: System Settings Activation**
   - System navigates to Android Settings > Accessibility.
   - User taps "FocusFlow", enables the switch, and confirms system prompt.
4. **Step 4: Functional Verification**
   - User opens YouTube and taps on the "Shorts" tab.
   - The service detects the Shorts container and triggers a gentle back action, returning user to their search/subscriptions.
5. **Step 5: User Control & Revocation**
   - User returns to FocusFlow and toggles off Shorts blocking.
   - User can disable the service in Android Settings at any time with graceful app degradation.
