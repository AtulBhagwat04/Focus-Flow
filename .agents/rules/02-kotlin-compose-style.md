# Rule: Kotlin and Compose style

## Kotlin
- Idiomatic Kotlin, immutable by default (`val`, read-only collections, data classes for models).
- No non-null assertions (`!!`). Handle nullability explicitly.
- Sealed types for states, results and events. Exhaustive `when` without a catch-all where the type is sealed.
- Public domain APIs have short KDoc stating contract and failure behaviour.
- Name by intent. Avoid abbreviations except widely known ones.
- Keep files under about 300 lines and functions under about 40 lines. Extract when exceeded.
- No commented-out code. No unexplained TODOs: include an issue reference or remove.
- Static analysis (Detekt) and formatting (ktlint) must pass.

## Compose
- Composables are stateless where possible. Hoist state to the ViewModel or caller.
- No business logic, I/O or time logic inside composables.
- Collect state in a lifecycle-aware way.
- Pass only the data a composable needs. Prefer stable, immutable parameters.
- Use the standard side-effect APIs correctly. Do not launch work directly in composition.
- Every screen composable has previews for: default, loading, empty, error, dark theme, large font.
- Modifiers are the first optional parameter and are applied once at the root.
- Use keys in lazy lists. Avoid heavy work during recomposition.
- Navigation uses type-safe routes. Screens receive callbacks, not navigation controllers.

## Resources
- No hard-coded user-facing strings, colors, dimensions or fonts. Use string resources, theme tokens and dimension tokens.
- Plurals and placeholders are handled in resources for localisation.
