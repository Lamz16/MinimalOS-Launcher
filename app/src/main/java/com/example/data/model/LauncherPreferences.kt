package com.example.data.model

enum class LauncherTheme {
    AMOLED,
    MINIMAL_DARK,
    MINIMAL_LIGHT,
    MONOCHROME,
    DEVELOPER
}

enum class AppDisplayMode {
    TEXT_ONLY,
    ICON_AND_TEXT,
    ICON_ONLY
}

enum class IconSize(val dpSize: Int) {
    SMALL(28),
    MEDIUM(38),
    LARGE(48)
}

enum class FontSize(val scaleFactor: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.2f)
}

enum class SwipeAction {
    SEARCH,
    APP_DRAWER,
    NOTIFICATIONS,
    NONE
}

enum class DoubleTapAction {
    NONE,
    SETTINGS,
    SEARCH,
    APP_DRAWER
}

data class LauncherPreferences(
    val theme: LauncherTheme = LauncherTheme.AMOLED,
    val displayMode: AppDisplayMode = AppDisplayMode.TEXT_ONLY,
    val iconSize: IconSize = IconSize.MEDIUM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val is24HourFormat: Boolean = true,
    val swipeDownAction: SwipeAction = SwipeAction.SEARCH,
    val swipeUpAction: SwipeAction = SwipeAction.APP_DRAWER,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.SETTINGS,
    val showFavoritesOnHome: Boolean = true,
    val showCategoriesOnHome: Boolean = true
)
