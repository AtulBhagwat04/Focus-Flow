# Changelog

All notable changes to FocusFlow will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-10-06

### Milestone 1 — Foundation

#### Added
- **Multi-module Gradle Architecture**:
  - `:app`: Application host with edge-to-edge Compose container and Navigation host.
  - `:core:common`: Core abstractions (`Clock`, `DispatcherProvider`, `Result<T>`).
  - `:core:designsystem`: Material 3 design system, color tokens (primary calm blue, gold rewards, calm neutral blockers), shapes, typography, and `FocusFlowTheme`.
  - `:core:domain`: Pure Kotlin domain module with architectural guards preventing Android and Firebase dependencies.
  - `:feature:home`: Initial home screen feature stub with Hilt `HomeViewModel`, immutable `HomeUiState`, and Composable previews.
- **Toolchain & Version Catalog**:
  - Gradle Version Catalog (`gradle/libs.versions.toml`) pinning Android SDK 36 (minSdk 26, targetSdk 36).
  - Jetpack Compose with Material 3, Navigation Compose, Hilt DI, Coroutines, and WorkManager.
  - Core library desugaring for `java.time` backwards compatibility on API 26+.
- **Static Analysis & Testing**:
  - Detekt configured in root `build.gradle.kts` with `detekt.yml` enforcing domain layer purity and coroutine hygiene.
  - Unit test suite for `:core:common` and `:feature:home` passing.
- **CI & Deployment**:
  - GitHub Actions CI workflow in `.github/workflows/ci.yml`.
  - Development configuration template for Firebase Google Services.
