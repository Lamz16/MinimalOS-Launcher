package com.lamz.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.lamz.data.local.dao.LauncherDao
import com.lamz.data.local.entity.AppCategoryEntity
import com.lamz.data.local.entity.CategoryAppCrossRef
import com.lamz.data.local.entity.CustomAppLabelEntity
import com.lamz.data.local.entity.FavoriteAppEntity
import com.lamz.data.local.entity.HiddenAppEntity
import com.lamz.data.model.InstalledApp
import com.lamz.util.IconCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InstalledAppRepository(
    private val context: Context,
    private val launcherDao: LauncherDao,
    private val externalScope: CoroutineScope
) {
    private val appContext = context.applicationContext

    // Raw installed apps loaded from PackageManager
    private val _rawInstalledApps = MutableStateFlow<List<RawAppInfo>>(emptyList())

    // Merged apps with Room metadata
    private val _appsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
    val appsFlow: StateFlow<List<InstalledApp>> = _appsFlow.asStateFlow()

    val categoriesFlow: Flow<List<AppCategoryEntity>> = launcherDao.getAllCategories()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.data?.schemeSpecificPart?.let { pkg ->
                if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
                    IconCache.remove(pkg)
                }
            }
            externalScope.launch {
                refreshInstalledApps()
            }
        }
    }

    init {
        registerPackageReceiver()
        externalScope.launch {
            refreshInstalledApps()
            observeRoomAndCombine()
        }
    }

    private data class RawAppInfo(
        val packageName: String,
        val activityName: String,
        val label: String,
        val installTime: Long
    )

    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(packageReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            appContext.registerReceiver(packageReceiver, filter)
        }
    }

    fun unregisterReceiver() {
        try {
            appContext.unregisterReceiver(packageReceiver)
        } catch (_: Exception) {}
    }

    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        val pm = appContext.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }

        val ourPackage = appContext.packageName
        val rawList = resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            // Don't show our own launcher inside the launcher
            if (pkg == ourPackage) return@mapNotNull null

            val activityName = resolveInfo.activityInfo.name
            val label = runCatching { resolveInfo.loadLabel(pm).toString() }.getOrDefault(pkg)
            val installTime = runCatching {
                pm.getPackageInfo(pkg, 0).firstInstallTime
            }.getOrDefault(0L)

            RawAppInfo(
                packageName = pkg,
                activityName = activityName,
                label = label,
                installTime = installTime
            )
        }.distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        _rawInstalledApps.value = rawList
    }

    private suspend fun observeRoomAndCombine() {
        combine(
            _rawInstalledApps,
            launcherDao.getAllFavorites(),
            launcherDao.getAllHiddenApps(),
            launcherDao.getAllCustomLabels(),
            launcherDao.getAllCategoryApps()
        ) { rawApps, favorites, hiddenList, customLabels, categoryApps ->
            val favSet = favorites.map { it.packageName }.toSet()
            val hiddenSet = hiddenList.map { it.packageName }.toSet()
            val customLabelMap = customLabels.associate { it.packageName to it.customLabel }
            val categoryMap = mutableMapOf<String, MutableSet<Long>>()
            categoryApps.forEach { crossRef ->
                categoryMap.getOrPut(crossRef.packageName) { mutableSetOf() }.add(crossRef.categoryId)
            }

            rawApps.map { raw ->
                val custom = customLabelMap[raw.packageName]
                InstalledApp(
                    packageName = raw.packageName,
                    activityName = raw.activityName,
                    label = raw.label,
                    customLabel = custom,
                    isFavorite = favSet.contains(raw.packageName),
                    isHidden = hiddenSet.contains(raw.packageName),
                    categoryIds = categoryMap[raw.packageName] ?: emptySet(),
                    installTime = raw.installTime,
                    iconBitmap = null // loaded asynchronously or on demand
                )
            }.sortedBy { it.displayLabel.lowercase() }
        }.collect { combinedApps ->
            _appsFlow.value = combinedApps
        }
    }

    // --- FAVORITES ---
    suspend fun setFavorite(packageName: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (isFavorite) {
            launcherDao.insertFavorite(FavoriteAppEntity(packageName = packageName))
        } else {
            launcherDao.deleteFavorite(packageName)
        }
    }

    // --- HIDDEN APPS ---
    suspend fun setHidden(packageName: String, isHidden: Boolean) = withContext(Dispatchers.IO) {
        if (isHidden) {
            launcherDao.insertHiddenApp(HiddenAppEntity(packageName = packageName))
        } else {
            launcherDao.deleteHiddenApp(packageName)
        }
    }

    // --- CUSTOM LABELS ---
    suspend fun setCustomLabel(packageName: String, customLabel: String?) = withContext(Dispatchers.IO) {
        if (customLabel.isNullOrBlank()) {
            launcherDao.deleteCustomLabel(packageName)
        } else {
            launcherDao.insertCustomLabel(CustomAppLabelEntity(packageName = packageName, customLabel = customLabel.trim()))
        }
    }

    // --- CATEGORIES ---
    suspend fun createCategory(name: String): Long = withContext(Dispatchers.IO) {
        launcherDao.insertCategory(AppCategoryEntity(name = name.trim()))
    }

    suspend fun updateCategory(category: AppCategoryEntity) = withContext(Dispatchers.IO) {
        launcherDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        launcherDao.deleteCategoryAndAssociations(categoryId)
    }

    suspend fun addAppToCategory(categoryId: Long, packageName: String) = withContext(Dispatchers.IO) {
        launcherDao.addAppToCategory(CategoryAppCrossRef(categoryId = categoryId, packageName = packageName))
    }

    suspend fun removeAppFromCategory(categoryId: Long, packageName: String) = withContext(Dispatchers.IO) {
        launcherDao.removeAppFromCategory(categoryId = categoryId, packageName = packageName)
    }
}
