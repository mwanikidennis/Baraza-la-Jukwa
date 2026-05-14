# Architecture

This document provides a detailed overview of the Jukwa platform's technical architecture.

## Android Application

The Android application is a critical component of the Jukwa platform, providing a rich, native experience for citizen engagement. It is built with a modern, offline-first architecture.

### Core Technologies

- **Kotlin Version:** 2.0.0
- **UI:** Jetpack Compose (with Material 3)
- **Build System:** Gradle (using Android Gradle Plugin 8.4.0)
- **Dependency Injection:** Hilt
- **Database:** Room (for offline storage)
- **Mapping:** MapLibre SDK

### Recent Foundational Upgrade

The Android project has recently undergone a significant foundational upgrade to modernize its dependencies and ensure long-term stability. The key changes include:

- **Kotlin 2.0.0:** The project has been migrated to Kotlin 2.0.0, which required a corresponding update to the Jetpack Compose Compiler.
- **Dependency Harmonization:** A number of core dependencies were updated to resolve conflicts and ensure compatibility with Kotlin 2.0.0. This included `Ktor`, `MapLibre`, `kotlinx.coroutines`, and `kotlinx.serialization`.
- **Build Tooling:** The project's build tooling has been updated to use the latest stable versions of the Android Gradle Plugin and related tools.
- **Test Suite Refactoring:** The unit tests have been updated to reflect the changes in the application's data classes and to ensure that they are compatible with the new, stricter Kotlin type system.
