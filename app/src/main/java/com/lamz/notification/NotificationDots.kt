package com.lamz.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps only package names; notification content is never retained by the launcher. */
object NotificationDots {
    private val _packages = MutableStateFlow<Set<String>>(emptySet())
    val packages: StateFlow<Set<String>> = _packages.asStateFlow()

    fun update(notifications: Array<StatusBarNotification>) {
        _packages.value = notifications.asSequence()
            .filterNot { (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0 }
            .map { it.packageName }
            .toSet()
    }

    fun clear() {
        _packages.value = emptySet()
    }
}
