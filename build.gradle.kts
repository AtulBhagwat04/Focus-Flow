// Root build file — plugin declarations and project-wide quality tools
plugins {
    alias(libs.plugins.android.application)      apply false
    alias(libs.plugins.android.library)          apply false
    alias(libs.plugins.kotlin.android)           apply false
    alias(libs.plugins.kotlin.compose.compiler)  apply false
    alias(libs.plugins.kotlin.serialization)     apply false
    alias(libs.plugins.hilt)                     apply false
    alias(libs.plugins.ksp)                      apply false
    alias(libs.plugins.google.services)          apply false
    alias(libs.plugins.firebase.crashlytics)     apply false
    alias(libs.plugins.firebase.perf)            apply false
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(
        files(
            "app/src/main/kotlin",
            "core/common/src/main/kotlin",
            "core/designsystem/src/main/kotlin",
            "core/domain/src/main/kotlin",
            "core/data/src/main/kotlin",
            "feature/home/src/main/kotlin",
            "feature/timer/src/main/kotlin",
            "feature/stats/src/main/kotlin",
            "feature/limits/src/main/kotlin",
            "feature/profile/src/main/kotlin"
        )
    )
}