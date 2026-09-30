package com.lamz.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class LauncherNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() = refreshDots()

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refreshDots()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refreshDots()

    override fun onListenerDisconnected() {
        NotificationDots.clear()
        super.onListenerDisconnected()
    }

    private fun refreshDots() {
        val notifications = runCatching { activeNotifications ?: emptyArray<StatusBarNotification>() }
            .getOrDefault(emptyArray())
        NotificationDots.update(notifications)
    }
}
