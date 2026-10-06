# UI/UX Specification

Design goal: calm, trustworthy, motivating. The app asks people to give up distraction, so the interface must never feel punishing, noisy or manipulative.

## 1. Design principles
1. **Calm over loud.** Few colors, generous spacing, quiet motion.
2. **Never shame.** Blocks and missed streaks are met with encouragement, not guilt.
3. **One primary action per screen.** The next step is always obvious.
4. **Explain permissions in human terms.** Say what, why, and what is never collected.
5. **Always a way out.** Every restriction has a visible, controlled exit.
6. **Accessible by default.** Accessibility is part of done, not a later pass.
7. **Honest states.** Loading, empty, offline, error and "permission lost" are designed screens, not afterthoughts.

## 2. Design system
**Foundation:** Material 3 with a custom theme. Optional dynamic color on supported devices, with a branded fallback palette.

**Color roles:** primary (focus/active), secondary (progress), tertiary (rewards), neutral surfaces, error (reserved for real errors, never for "blocked"), success. Blocking uses a calm neutral or primary tone, not red. Both light and dark themes are first-class and follow the system setting with a manual override.

**Typography:** one family, a clear scale (display for timer digits, headline, title, body, label). The timer uses tabular figures so digits do not jitter. All text scales with system font size up to at least 200% without clipping.

**Spacing and shape:** a 4 dp base grid with 8 dp rhythm. Consistent corner radii by component size. Minimum touch target 48 dp.

**Iconography:** one consistent icon set. Icons never carry meaning alone. Pair with a label or accessibility description.

**Motion:** short, purposeful transitions. Respect the system "remove animations" setting. The timer ring and celebrations have reduced-motion variants.

**Tokens only.** Screens use theme tokens. No hard-coded colors, sizes, fonts or strings.

## 3. Information architecture
Bottom navigation (rail on wide windows): **Home, Focus, Stats, Rooms, Profile**. Settings, permission health, limits, Study Mode and premium are reached from Profile or Home cards.

## 4. Screen inventory
| Screen | Purpose | Key states |
|---|---|---|
| Welcome and goals | Explain value, pick goals | first run |
| Permission steps | One permission per screen | granted, denied, skipped, returned from settings |
| Accessibility disclosure | Required consent before enabling | agree, decline |
| Home | Today at a glance, quick start, streak, health banner | no data, active session, permission problem |
| Focus | Timer setup and running session | idle, running, paused, break, completed |
| Blocker overlay | Shown when a blocked app opens | session, schedule, limit, feed, study mode |
| Stats | Screen time and focus charts | loading, no data, offline cache |
| Limits and blocked apps | Choose apps, set rules | empty, search, many apps |
| Study Mode | Allowlist management | empty, search results, quota or offline error |
| Rooms | Browse and join focus rooms | empty, live, offline |
| Room detail | Members, shared timer | joining, full, ended |
| Leaderboard and friends | Weekly ranking | loading, empty, offline cache |
| Profile and rewards | Streak, level, badges, themes | new user |
| Permission Health | What works, what is broken, fixes | all good, partial, critical |
| Settings and privacy | Preferences, data export, delete account | confirm dialogs |

## 5. Key flows
1. **First run:** value, goals, permissions (skippable), pick apps, first session suggestion.
2. **Start a session:** choose duration and tag, optional ambient sound, start. A confirmation explains what will be blocked.
3. **Encountering a block:** overlay appears with what is blocked, why, time left, and one primary action "Back to focus". A quiet secondary "I need this" begins the cooldown path.
4. **Emergency exit:** short cooldown or typed phrase, then a single confirmation. The session records an early exit without shaming copy.
5. **Permission lost:** a non-blocking banner on Home and a clear card in Permission Health with a one-tap fix. The app never nags with dialogs.
6. **Add a Study Mode channel:** search, preview, add. Handle quota exhaustion with a friendly message and the curated list.
7. **Join a room:** browse, join, see presence, start the shared timer.

## 6. The blocker screen
- Calm background, a short supportive message, and the remaining session time.
- Primary button returns the user to their previous or home screen.
- Shows which rule triggered the block.
- Never covers the system navigation controls in a way that traps the user.
- Does not play sounds or vibrate by default.
- Fully usable with TalkBack and at large font sizes.

## 7. Content and tone
- Short, warm, plain language. Second person. No fear, no guilt.
- Avoid medical or addiction vocabulary.
- Error messages say what happened, what the user can do next, and never blame.
- All copy lives in string resources with plurals and placeholders handled for localization. Plan for Hindi and Marathi, and for right-to-left layout.

## 8. Accessibility requirements
- Text contrast at least 4.5:1, and 3:1 for large text and UI components.
- Every interactive element has a name, role and state for TalkBack, and a logical focus order.
- Information never relies on color alone, for example charts use labels or patterns.
- Touch targets at least 48 dp. Support switch access and keyboard navigation.
- Respect font scale, display size and reduced motion.
- Test with TalkBack and Accessibility Scanner before each release.

## 9. Adaptive layout
Use window size classes. Compact phones use bottom navigation. Medium and expanded windows use a rail, and list-detail layouts for Stats and Rooms. Support landscape, foldables, multi-window and system bars with edge-to-edge insets.

## 10. Empty, loading and error design
- Loading uses skeletons for lists and charts, not a blocking spinner.
- Empty states explain the value and offer one action.
- Offline states show cached data with a quiet "last updated" note.
- Errors offer a retry and, when relevant, a link to the fix.

## 11. Gamification UX
Streak freeze instead of hard resets, celebrations that are brief and skippable, rewards that cosmetic only, no countdown pressure or loss-aversion tricks, and an easy way to hide social comparison.

## 12. Handoff checklist (per screen)
States designed, tokens used, strings extracted, TalkBack order checked, large-font screenshot reviewed, dark theme reviewed, tablet layout reviewed, analytics events named, empty and error copy approved.
