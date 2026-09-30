package com.lamz.ui.viewmodel

import com.lamz.data.local.entity.AppCategoryEntity
import com.lamz.data.model.InstalledApp
import com.lamz.data.model.LauncherPreferences

data class LauncherUiState(
    val allApps: List<InstalledApp> = emptyList(),
    val visibleApps: List<InstalledApp> = emptyList(),
    val favoriteApps: List<InstalledApp> = emptyList(),
    val hiddenApps: List<InstalledApp> = emptyList(),
    val categories: List<AppCategoryEntity> = emptyList(),
    val categoryAppsMap: Map<Long, List<InstalledApp>> = emptyMap(),
    val preferences: LauncherPreferences = LauncherPreferences(),
    val searchQuery: String = "",
    val searchResults: List<InstalledApp> = emptyList(),
    val selectedCategoryFilter: Long? = null,
    val activeContextMenuApp: InstalledApp? = null,
    val isAppDrawerOpen: Boolean = false,
    val isSearchOpen: Boolean = false,
    val isDefaultLauncher: Boolean = false
)
