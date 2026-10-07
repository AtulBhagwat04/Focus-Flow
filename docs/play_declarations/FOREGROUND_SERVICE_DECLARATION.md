# Google Play Foreground Service Declaration

## 1. Declared Types & Configuration
In `app/src/main/AndroidManifest.xml`:
```xml
<service
    android:name=".system.service.FocusSessionService"
    android:exported="false"
    android:foregroundServiceType="specialUse|mediaPlayback">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="focus_session_timer_and_blocking" />
</service>
```

---

## 2. Technical Justification by Type

### A. Type: `specialUse`
- **Subtype Value**: `focus_session_timer_and_blocking`
- **Core Purpose**: Real-time focus session countdown timer and background app blocking state machine.
- **Why a Foreground Service is Required**:
  1. **Immediate User Awareness**: The service displays an ongoing notification showing current session time remaining, elapsed focus minutes, and quick actions ("Pause", "End Session").
  2. **Doze & Battery Optimization Survival**: During deep work sessions (often 25 to 90 minutes), the screen is turned off or the user switches between study apps. Without a foreground service, the Android system would defer background execution, causing the timer and schedule-blocking state machine to desynchronize or terminate unexpectedly.
  3. **Why WorkManager is Insufficient**: WorkManager is designed for deferrable, opportunistic background work with minimum 15-minute periodic intervals. It cannot maintain an active second-by-second countdown or immediate app-switch intervention.

### B. Type: `mediaPlayback`
- **Core Purpose**: Continuous ambient study audio (white noise, rain, forest sounds) during active focus sessions.
- **Why a Foreground Service is Required**:
  - In Android 14+ and Android 17, continuous background audio playback requires an active foreground service with the `mediaPlayback` type.
  - The ambient soundscape must continue playing seamlessly while the user takes notes, reads study PDFs, or when the screen is turned off.
  - Playback is strictly initiated by user action and can be paused or stopped directly from the persistent notification or within the app.
