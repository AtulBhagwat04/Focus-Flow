package com.focusflow.app

import android.app.Application
import android.os.StrictMode
import com.focusflow.app.system.service.FocusSessionServiceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FocusFlowApp : Application() {

    @Inject
    lateinit var focusSessionServiceManager: FocusSessionServiceManager

    override fun onCreate() {
        super.onCreate()

        focusSessionServiceManager.startObserving()

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
