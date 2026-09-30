package com.lamz.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object LauncherUtils {

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
                true
            } else {
                Toast.makeText(context, "Cannot launch app ($packageName)", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening app: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open app info: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot request uninstall: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Returns the system-owned confirmation intent for becoming the Home app.
     *
     * This intent must be launched by an Activity for result.  Starting it from an
     * arbitrary Context loses the result and makes it impossible for the UI to
     * reliably update its "default launcher" state after the user decides.
     */
    fun createDefaultHomeRoleRequest(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            ?: return null
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME) ||
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            return null
        }
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
    }

    fun openDefaultHomeSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Cannot open home settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openWallpaperPicker(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            Toast.makeText(context, "No wallpaper picker is available", Toast.LENGTH_SHORT).show()
        }
    }

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                    return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                }
            }
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (e: Exception) {
            false
        }
    }
}
