---
name: compose-ui-ux
description: Use when building or reviewing any screen, component, theme, animation, copy, or accessibility behaviour in the app.
---

# Compose UI and UX engineering

## Before building
Read `docs/UI_UX_SPEC.md` and `.agents/rules/03-ui-ux.md`. Identify the screen's purpose, primary action, and every state it must show.

## Build procedure
1. List states: loading, content, empty, offline, error, permission problem, and any feature-specific ones.
2. Use shared design-system components and theme tokens. If a needed component is missing, add it to the design system, not the screen.
3. Keep composables stateless. State lives in the ViewModel as one immutable object, effects are separate.
4. Add previews for default, loading, empty, error, dark, and large font.
5. Provide accessibility semantics: names, roles, states, focus order, grouped content, and clear headings.
6. Check contrast, touch targets, 200% font scale, reduced motion, dark theme, and wide windows.
7. Handle edge-to-edge insets and predictive back.
8. Write all copy in resources with plurals and placeholders. Tone: short, warm, plain, never shaming.
9. Add Compose tests for semantics and key interactions, and screenshot tests for the design system.

## Review checklist
- One clear primary action.
- No hard-coded colors, sizes, strings.
- No business logic in composables.
- Motion is purposeful and respects the system setting.
- Error text says what happened and what to do next.
- The blocker screen has a clear way back and never traps the user.

## Pitfalls
Using error colors for normal blocks, loading spinners that block the whole screen, text clipped at large font sizes, icons without descriptions, charts relying on color alone, forgetting tablets and foldables.
