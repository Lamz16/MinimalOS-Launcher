package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppDisplayMode
import com.example.data.model.DoubleTapAction
import com.example.data.model.FontSize
import com.example.data.model.IconSize
import com.example.data.model.LauncherPreferences
import com.example.data.model.LauncherTheme
import com.example.data.model.SwipeAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minimalos_prefs")

class LauncherPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("launcher_theme")
        val DISPLAY_MODE = stringPreferencesKey("app_display_mode")
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SWIPE_DOWN = stringPreferencesKey("swipe_down_action")
        val SWIPE_UP = stringPreferencesKey("swipe_up_action")
        val DOUBLE_TAP = stringPreferencesKey("double_tap_action")
        val SHOW_FAVORITES_HOME = booleanPreferencesKey("show_favorites_home")
        val SHOW_CATEGORIES_HOME = booleanPreferencesKey("show_categories_home")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.dataStore.data.map { prefs ->
        val themeName = prefs[PreferencesKeys.THEME] ?: LauncherTheme.AMOLED.name
        val theme = runCatching { LauncherTheme.valueOf(themeName) }.getOrDefault(LauncherTheme.AMOLED)

        val displayModeName = prefs[PreferencesKeys.DISPLAY_MODE] ?: AppDisplayMode.TEXT_ONLY.name
        val displayMode = runCatching { AppDisplayMode.valueOf(displayModeName) }.getOrDefault(AppDisplayMode.TEXT_ONLY)

        val iconSizeName = prefs[PreferencesKeys.ICON_SIZE] ?: IconSize.MEDIUM.name
        val iconSize = runCatching { IconSize.valueOf(iconSizeName) }.getOrDefault(IconSize.MEDIUM)

        val fontSizeName = prefs[PreferencesKeys.FONT_SIZE] ?: FontSize.MEDIUM.name
        val fontSize = runCatching { FontSize.valueOf(fontSizeName) }.getOrDefault(FontSize.MEDIUM)

        val showClock = prefs[PreferencesKeys.SHOW_CLOCK] ?: true
        val showDate = prefs[PreferencesKeys.SHOW_DATE] ?: true
        val is24Hour = prefs[PreferencesKeys.IS_24_HOUR] ?: true

        val swipeDownName = prefs[PreferencesKeys.SWIPE_DOWN] ?: SwipeAction.SEARCH.name
        val swipeDown = runCatching { SwipeAction.valueOf(swipeDownName) }.getOrDefault(SwipeAction.SEARCH)

        val swipeUpName = prefs[PreferencesKeys.SWIPE_UP] ?: SwipeAction.APP_DRAWER.name
        val swipeUp = runCatching { SwipeAction.valueOf(swipeUpName) }.getOrDefault(SwipeAction.APP_DRAWER)

        val doubleTapName = prefs[PreferencesKeys.DOUBLE_TAP] ?: DoubleTapAction.SETTINGS.name
        val doubleTap = runCatching { DoubleTapAction.valueOf(doubleTapName) }.getOrDefault(DoubleTapAction.SETTINGS)

        val showFavoritesHome = prefs[PreferencesKeys.SHOW_FAVORITES_HOME] ?: true
        val showCategoriesHome = prefs[PreferencesKeys.SHOW_CATEGORIES_HOME] ?: true

        LauncherPreferences(
            theme = theme,
            displayMode = displayMode,
            iconSize = iconSize,
            fontSize = fontSize,
            showClock = showClock,
            showDate = showDate,
            is24HourFormat = is24Hour,
            swipeDownAction = swipeDown,
            swipeUpAction = swipeUp,
            doubleTapAction = doubleTap,
            showFavoritesOnHome = showFavoritesHome,
            showCategoriesOnHome = showCategoriesHome
        )
    }

    suspend fun setTheme(theme: LauncherTheme) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    suspend fun setDisplayMode(mode: AppDisplayMode) {
        context.dataStore.edit { it[PreferencesKeys.DISPLAY_MODE] = mode.name }
    }

    suspend fun setIconSize(size: IconSize) {
        context.dataStore.edit { it[PreferencesKeys.ICON_SIZE] = size.name }
    }

    suspend fun setFontSize(size: FontSize) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE] = size.name }
    }

    suspend fun setShowClock(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CLOCK] = show }
    }

    suspend fun setShowDate(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_DATE] = show }
    }

    suspend fun setIs24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_24_HOUR] = is24Hour }
    }

    suspend fun setSwipeDownAction(action: SwipeAction) {
        context.dataStore.edit { it[PreferencesKeys.SWIPE_DOWN] = action.name }
    }

    suspend fun setSwipeUpAction(action: SwipeAction) {
        context.dataStore.edit { it[PreferencesKeys.SWIPE_UP] = action.name }
    }

    suspend fun setDoubleTapAction(action: DoubleTapAction) {
        context.dataStore.edit { it[PreferencesKeys.DOUBLE_TAP] = action.name }
    }

    suspend fun setShowFavoritesOnHome(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_FAVORITES_HOME] = show }
    }

    suspend fun setShowCategoriesOnHome(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CATEGORIES_HOME] = show }
    }
}
