package com.lamz.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.lamz.MainActivity
import com.lamz.R

/** Publishes launcher-icon shortcuts owned by MinimalOS (Android 7.1+). */
object LauncherShortcuts {
    const val ACTION_OPEN_SEARCH = "com.lamz.action.OPEN_SEARCH"
    const val ACTION_OPEN_DRAWER = "com.lamz.action.OPEN_DRAWER"

    fun publish(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        runCatching {
            shortcutManager.dynamicShortcuts = listOf(
                shortcut(context, "search", "Search apps", ACTION_OPEN_SEARCH),
                shortcut(context, "drawer", "All apps", ACTION_OPEN_DRAWER)
            )
        }
    }

    private fun shortcut(context: Context, id: String, label: String, action: String): ShortcutInfo =
        ShortcutInfo.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(
                Intent(context, MainActivity::class.java)
                    .setAction(action)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            .build()
}
