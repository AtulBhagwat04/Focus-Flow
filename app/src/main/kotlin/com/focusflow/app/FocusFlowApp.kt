package com.focusflow.app

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * - @HiltAndroidApp triggers Hilt code generation for the entire app.
 * - StrictMode is enabled in debug builds to catch threading / resource violations early.
 *   Per ARCHITECTURE.md §6: "StrictMode is enabled in debug builds."
 */
@HiltAndroidApp
class FocusFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
