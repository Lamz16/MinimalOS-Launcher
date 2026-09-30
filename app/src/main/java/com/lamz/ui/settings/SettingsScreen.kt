package com.lamz.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamz.data.model.AppDisplayMode
import com.lamz.data.model.DoubleTapAction
import com.lamz.data.model.FontSize
import com.lamz.data.model.IconSize
import com.lamz.data.model.LauncherTheme
import com.lamz.data.model.SwipeAction
import com.lamz.ui.viewmodel.LauncherUiState
import com.lamz.ui.viewmodel.LauncherViewModel

private enum class SettingsSubScreen {
    MAIN,
    CATEGORIES,
    HIDDEN_APPS
}

@Composable
fun SettingsScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    onSetAsDefaultLauncher: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    LaunchedEffect(Unit) {
        viewModel.checkDefaultLauncher(context)
    }

    when (subScreen) {
        SettingsSubScreen.CATEGORIES -> {
            CategoriesManagementScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN }
            )
        }
        SettingsSubScreen.HIDDEN_APPS -> {
            HiddenAppsScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN }
            )
        }
        SettingsSubScreen.MAIN -> {
            MainSettingsContent(
                uiState = uiState,
                viewModel = viewModel,
                onBack = onBack,
                onSetAsDefaultLauncher = onSetAsDefaultLauncher,
                onOpenWallpaperPicker = onOpenWallpaperPicker,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onNavigateCategories = { subScreen = SettingsSubScreen.CATEGORIES },
                onNavigateHiddenApps = { subScreen = SettingsSubScreen.HIDDEN_APPS },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainSettingsContent(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    onSetAsDefaultLauncher: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onNavigateCategories: () -> Unit,
    onNavigateHiddenApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- DEFAULT LAUNCHER STATUS CARD ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isDefaultLauncher) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (uiState.isDefaultLauncher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.isDefaultLauncher) "MinimalOS is Default Launcher" else "MinimalOS is not Default",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (uiState.isDefaultLauncher)
                        "Pressing HOME opens MinimalOS automatically."
                    else
                        "Set MinimalOS as default home to replace your OEM stock launcher.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!uiState.isDefaultLauncher) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSetAsDefaultLauncher,
                        modifier = Modifier.testTag("set_default_launcher_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Set as Default Launcher")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- APPEARANCE SECTION ---
        SettingsSectionHeader(title = "Appearance", icon = Icons.Rounded.Palette)

        Text(
            text = "Theme",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LauncherTheme.entries.forEach { theme ->
                val label = when (theme) {
                    LauncherTheme.AMOLED -> "AMOLED (Pure Black)"
                    LauncherTheme.MINIMAL_DARK -> "Minimal Dark"
                    LauncherTheme.MINIMAL_LIGHT -> "Minimal Light"
                    LauncherTheme.MONOCHROME -> "Monochrome"
                    LauncherTheme.DEVELOPER -> "Developer"
                }
                FilterChip(
                    selected = uiState.preferences.theme == theme,
                    onClick = { viewModel.setTheme(theme) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "App Display Mode",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppDisplayMode.entries.forEach { mode ->
                val label = when (mode) {
                    AppDisplayMode.TEXT_ONLY -> "Text Only"
                    AppDisplayMode.ICON_AND_TEXT -> "Icon + Text"
                    AppDisplayMode.ICON_ONLY -> "Icon Only"
                }
                FilterChip(
                    selected = uiState.preferences.displayMode == mode,
                    onClick = { viewModel.setDisplayMode(mode) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Font Size",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FontSize.entries.forEach { size ->
                FilterChip(
                    selected = uiState.preferences.fontSize == size,
                    onClick = { viewModel.setFontSize(size) },
                    label = { Text(size.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.preferences.displayMode != AppDisplayMode.TEXT_ONLY) {
            Text(
                text = "Icon Size",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconSize.entries.forEach { size ->
                    FilterChip(
                        selected = uiState.preferences.iconSize == size,
                        onClick = { viewModel.setIconSize(size) },
                        label = { Text(size.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- HOME SCREEN SECTION ---
        SettingsSectionHeader(title = "Home Screen", icon = Icons.Rounded.Home)

        SettingsSwitchItem(
            title = "Show Clock",
            subtitle = "Display digital clock at top of Home",
            checked = uiState.preferences.showClock,
            onCheckedChange = { viewModel.setShowClock(it) }
        )

        SettingsNavRow(
            title = "Wallpaper",
            subtitle = "Open the device wallpaper picker",
            icon = Icons.Rounded.Palette,
            tag = "settings_wallpaper_picker",
            onClick = onOpenWallpaperPicker
        )

        SettingsSwitchItem(
            title = "Show System Wallpaper",
            subtitle = "Use the selected wallpaper as a subtle Home background",
            checked = uiState.preferences.showSystemWallpaper,
            onCheckedChange = { viewModel.setShowSystemWallpaper(it) }
        )

        SettingsSwitchItem(
            title = "Show Date",
            subtitle = "Display day and date below the clock",
            checked = uiState.preferences.showDate,
            onCheckedChange = { viewModel.setShowDate(it) }
        )

        SettingsSwitchItem(
            title = "24-Hour Format",
            subtitle = "Use 24-hour time (e.g. 15:42) instead of 12-hour AM/PM",
            checked = uiState.preferences.is24HourFormat,
            onCheckedChange = { viewModel.setIs24HourFormat(it) }
        )

        SettingsSwitchItem(
            title = "Show Favorite Apps",
            subtitle = "Display pinned favorites on the home screen",
            checked = uiState.preferences.showFavoritesOnHome,
            onCheckedChange = { viewModel.setShowFavoritesOnHome(it) }
        )

        SettingsSwitchItem(
            title = "Show Categories",
            subtitle = "Display custom categories and their apps on Home",
            checked = uiState.preferences.showCategoriesOnHome,
            onCheckedChange = { viewModel.setShowCategoriesOnHome(it) }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- GESTURES SECTION ---
        SettingsSectionHeader(title = "Gestures", icon = Icons.Rounded.TouchApp)

        Text(
            text = "Swipe Down",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(SwipeAction.SEARCH, SwipeAction.NONE).forEach { action ->
                FilterChip(
                    selected = uiState.preferences.swipeDownAction == action,
                    onClick = { viewModel.setSwipeDownAction(action) },
                    label = {
                        Text(
                            text = if (action == SwipeAction.SEARCH) "Open Search" else "None",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe Up",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(SwipeAction.APP_DRAWER, SwipeAction.NONE).forEach { action ->
                FilterChip(
                    selected = uiState.preferences.swipeUpAction == action,
                    onClick = { viewModel.setSwipeUpAction(action) },
                    label = {
                        Text(
                            text = if (action == SwipeAction.APP_DRAWER) "Open App Drawer" else "None",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Double Tap Home Screen",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(DoubleTapAction.SETTINGS, DoubleTapAction.SEARCH, DoubleTapAction.NONE).forEach { action ->
                val label = when (action) {
                    DoubleTapAction.SETTINGS -> "Open Settings"
                    DoubleTapAction.SEARCH -> "Open Search"
                    DoubleTapAction.NONE -> "None"
                    else -> action.name
                }
                FilterChip(
                    selected = uiState.preferences.doubleTapAction == action,
                    onClick = { viewModel.setDoubleTapAction(action) },
                    label = { Text(text = label, maxLines = 1, softWrap = false) }
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- APP MANAGEMENT SECTION ---
        SettingsSectionHeader(title = "App Organization", icon = Icons.Rounded.Folder)

        SettingsNavRow(
            title = "Categories",
            subtitle = "${uiState.categories.size} custom categories created",
            icon = Icons.Rounded.Folder,
            tag = "settings_categories_nav",
            onClick = onNavigateCategories
        )

        SettingsNavRow(
            title = "Hidden Apps",
            subtitle = "${uiState.hiddenApps.size} apps hidden from view",
            icon = Icons.Rounded.VisibilityOff,
            tag = "settings_hidden_apps_nav",
            onClick = onNavigateHiddenApps
        )

        SettingsNavRow(
            title = "Notification Dots",
            subtitle = "Grant notification access to show dots on apps",
            icon = Icons.Rounded.Notifications,
            tag = "settings_notification_dots",
            onClick = onOpenNotificationSettings
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- ABOUT SECTION ---
        SettingsSectionHeader(title = "About", icon = Icons.Rounded.Info)
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "MinimalOS Launcher v1.0",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A distraction-free, ultra-fast, privacy-first Android launcher built with Kotlin and Jetpack Compose. Zero background analytics, zero advertising, zero cloud dependencies.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
