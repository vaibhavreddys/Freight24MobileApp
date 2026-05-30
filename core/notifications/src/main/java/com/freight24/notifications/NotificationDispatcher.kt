package com.freight24.notifications

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationDispatcher
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun show(notification: AppNotification) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

            NotificationChannels.create(context)

            val channelId =
                when (notification) {
                    is AppNotification.Critical -> NotificationChannels.CRITICAL
                    is AppNotification.InterestAlert -> NotificationChannels.CRITICAL
                    else -> NotificationChannels.GENERAL
                }

            val builder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_logo_freight24)
                    .setContentTitle(notification.title)
                    .setContentText(notification.message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

            NotificationManagerCompat.from(context)
                .notify(notification.id, builder.build())
        }
    }
