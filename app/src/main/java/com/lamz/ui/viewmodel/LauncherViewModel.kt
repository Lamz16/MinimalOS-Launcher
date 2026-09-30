package com.lamz.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lamz.data.local.LauncherDatabase
import com.lamz.data.local.LauncherPreferencesRepository
import com.lamz.data.local.entity.AppCategoryEntity
import com.lamz.data.model.AppDisplayMode
import com.lamz.data.model.DoubleTapAction
import com.lamz.data.model.FontSize
import com.lamz.data.model.IconSize
import com.lamz.data.model.InstalledApp
import com.lamz.data.model.LauncherTheme
import com.lamz.data.model.SwipeAction
import com.lamz.data.repository.InstalledAppRepository
import com.lamz.notification.NotificationDots
import com.lamz.util.LauncherUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val appRepository: InstalledAppRepository,
    private val preferencesRepository: LauncherPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    private val _activeContextMenuApp = MutableStateFlow<InstalledApp?>(null)
    private val _isAppDrawerOpen = MutableStateFlow(false)
    private val _isSearchOpen = MutableStateFlow(false)
    private val _isDefaultLauncher = MutableStateFlow(false)

    private data class LocalUiState(
        val searchQuery: String = "",
        val categoryFilter: Long? = null,
        val contextMenuApp: InstalledApp? = null,
        val isDrawerOpen: Boolean = false,
        val isSearchOpen: Boolean = false
    )

    private val localStateFlow = combine(
        _searchQuery,
        _selectedCategoryFilter,
        _activeContextMenuApp,
        _isAppDrawerOpen,
        _isSearchOpen
    ) { query, filter, menuApp, drawerOpen, searchOpen ->
        LocalUiState(query, filter, menuApp, drawerOpen, searchOpen)
    }

    private val appsWithNotificationState = combine(
        appRepository.appsFlow,
        NotificationDots.packages
    ) { apps, packagesWithNotifications ->
        apps.map { app -> app.copy(hasNotification = app.packageName in packagesWithNotifications) }
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        appsWithNotificationState,
        appRepository.categoriesFlow,
        preferencesRepository.preferencesFlow,
        localStateFlow,
        _isDefaultLauncher
    ) { rawApps, categories, preferences, local, isDefault ->
        val visible = rawApps.filter { !it.isHidden }
        val favorites = visible.filter { it.isFavorite }
        val hidden = rawApps.filter { it.isHidden }

        val categoryMap = categories.associate { category ->
            category.id to visible.filter { app -> app.categoryIds.contains(category.id) }
        }

        val filteredVisible = if (local.categoryFilter != null) {
            visible.filter { it.categoryIds.contains(local.categoryFilter) }
        } else {
            visible
        }

        val searchResults = if (local.searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = local.searchQuery.trim().lowercase()
            visible.filter {
                it.displayLabel.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        LauncherUiState(
            allApps = rawApps,
            visibleApps = filteredVisible,
            favoriteApps = favorites,
            hiddenApps = hidden,
            categories = categories,
            categoryAppsMap = categoryMap,
            preferences = preferences,
            searchQuery = local.searchQuery,
            searchResults = searchResults,
            selectedCategoryFilter = local.categoryFilter,
            activeContextMenuApp = local.contextMenuApp,
            isAppDrawerOpen = local.isDrawerOpen,
            isSearchOpen = local.isSearchOpen,
            isDefaultLauncher = isDefault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun openContextMenu(app: InstalledApp) {
        _activeContextMenuApp.value = app
    }

    fun closeContextMenu() {
        _activeContextMenuApp.value = null
    }

    fun setAppDrawerOpen(open: Boolean) {
        _isAppDrawerOpen.value = open
        if (!open) {
            _searchQuery.value = ""
        }
    }

    fun setSearchOpen(open: Boolean) {
        _isSearchOpen.value = open
        if (!open) {
            _searchQuery.value = ""
        }
    }

    fun checkDefaultLauncher(context: Context) {
        _isDefaultLauncher.value = LauncherUtils.isDefaultLauncher(context)
    }

    fun refreshApps() {
        viewModelScope.launch {
            appRepository.refreshInstalledApps()
        }
    }

    // --- FAVORITES & HIDDEN ---
    fun toggleFavorite(app: InstalledApp) {
        viewModelScope.launch {
            appRepository.setFavorite(app.packageName, !app.isFavorite)
        }
    }

    fun toggleHide(app: InstalledApp) {
        viewModelScope.launch {
            appRepository.setHidden(app.packageName, !app.isHidden)
            closeContextMenu()
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            appRepository.setHidden(packageName, false)
        }
    }

    fun renameApp(app: InstalledApp, newLabel: String) {
        viewModelScope.launch {
            appRepository.setCustomLabel(app.packageName, newLabel)
            closeContextMenu()
        }
    }

    // --- CATEGORIES ---
    fun createCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            appRepository.createCategory(name)
        }
    }

    fun updateCategory(category: AppCategoryEntity) {
        viewModelScope.launch {
            appRepository.updateCategory(category)
        }
    }

    fun moveCategory(categories: List<AppCategoryEntity>, fromIndex: Int, toIndex: Int) {
        if (fromIndex !in categories.indices || toIndex !in categories.indices) return

        val reordered = categories.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }.mapIndexed { index, category -> category.copy(orderIndex = index) }

        viewModelScope.launch {
            appRepository.updateCategoryOrder(reordered)
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            if (_selectedCategoryFilter.value == categoryId) {
                _selectedCategoryFilter.value = null
            }
            appRepository.deleteCategory(categoryId)
        }
    }

    fun toggleAppCategory(app: InstalledApp, categoryId: Long) {
        // The context menu holds an app snapshot. Update that snapshot first so
        // repeated checkbox taps cannot briefly revert to stale Room data.
        val sourceApp = _activeContextMenuApp.value
            ?.takeIf { it.packageName == app.packageName }
            ?: app
        val updatedCategoryIds = if (categoryId in sourceApp.categoryIds) {
            sourceApp.categoryIds - categoryId
        } else {
            sourceApp.categoryIds + categoryId
        }
        _activeContextMenuApp.value = sourceApp.copy(categoryIds = updatedCategoryIds)

        viewModelScope.launch {
            if (categoryId in sourceApp.categoryIds) {
                appRepository.removeAppFromCategory(categoryId, sourceApp.packageName)
            } else {
                appRepository.addAppToCategory(categoryId, sourceApp.packageName)
            }
        }
    }

    // --- PREFERENCES SETTERS ---
    fun setTheme(theme: LauncherTheme) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun setDisplayMode(mode: AppDisplayMode) {
        viewModelScope.launch { preferencesRepository.setDisplayMode(mode) }
    }

    fun setIconSize(size: IconSize) {
        viewModelScope.launch { preferencesRepository.setIconSize(size) }
    }

    fun setFontSize(size: FontSize) {
        viewModelScope.launch { preferencesRepository.setFontSize(size) }
    }

    fun setShowClock(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowClock(show) }
    }

    fun setShowDate(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowDate(show) }
    }

    fun setShowSystemWallpaper(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowSystemWallpaper(show) }
    }

    fun setIs24HourFormat(is24: Boolean) {
        viewModelScope.launch { preferencesRepository.setIs24HourFormat(is24) }
    }

    fun setSwipeDownAction(action: SwipeAction) {
        viewModelScope.launch { preferencesRepository.setSwipeDownAction(action) }
    }

    fun setSwipeUpAction(action: SwipeAction) {
        viewModelScope.launch { preferencesRepository.setSwipeUpAction(action) }
    }

    fun setDoubleTapAction(action: DoubleTapAction) {
        viewModelScope.launch { preferencesRepository.setDoubleTapAction(action) }
    }

    fun setShowFavoritesOnHome(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowFavoritesOnHome(show) }
    }

    fun setShowCategoriesOnHome(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowCategoriesOnHome(show) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val database = LauncherDatabase.getInstance(appContext)
                    val dao = database.launcherDao()
                    val prefRepo = LauncherPreferencesRepository(appContext)
                    val appRepo = InstalledAppRepository(
                        context = appContext,
                        launcherDao = dao,
                        externalScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
                    )
                    return LauncherViewModel(appRepo, prefRepo) as T
                }
            }
    }
}
