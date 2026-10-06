# Rule: UI and UX

Source of truth: `docs/UI_UX_SPEC.md`.

- Use the design system tokens and shared components. Do not invent one-off styles.
- Every screen implements all its states: loading, content, empty, offline, error, and permission-problem where relevant.
- One clear primary action per screen.
- Never shame or pressure the user. Blocking and missed streaks use supportive wording. Do not use the error color for a normal block.
- Copy is short, warm and plain. No medical or addiction language.
- Accessibility is part of done:
  - contrast at least 4.5:1 for text and 3:1 for large text and components
  - touch targets at least 48 dp
  - every control has a name, role and state for TalkBack, and focus order is logical
  - information never depends on color alone
  - layouts work at 200% font scale
  - respect the reduced-motion setting
- Light and dark themes both required. Check edge-to-edge insets and predictive back.
- Layouts adapt to window size classes. Wide windows use a navigation rail and list-detail layouts where specified.
- The blocker screen must always offer a clear way back and must not trap the user.
- Permission screens explain what, why, and what is never collected, and always allow skipping optional permissions.
- Do not add dark patterns, fake urgency, or guilt-based notifications. Keep notification volume low and user-controllable.
- Attach screenshots (default, dark, large font) to UI changes for review.
