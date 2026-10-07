pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "FocusFlow"

// App module
include(":app")

// Core modules — single-module-first strategy, split only when justified (per ARCHITECTURE.md)
include(":core:common")
include(":core:designsystem")
include(":core:domain")
include(":core:data")

// Feature modules
include(":feature:home")
include(":feature:timer")
include(":feature:stats")
include(":feature:limits")
include(":feature:profile")
include(":feature:rooms")
