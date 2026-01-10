package com.freight24.trucker

import android.app.Application
import com.freight24.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Freight24Application : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }
}
