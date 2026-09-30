package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoubleTapAction
import com.example.data.model.SwipeAction
import com.example.ui.drawer.AppItemView
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.LauncherUtils

@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(uiState.preferences) {
                detectTapGestures(
                    onLongPress = {
                        onOpenSettings()
                    },
                    onDoubleTap = {
                        when (uiState.preferences.doubleTapAction) {
                            DoubleTapAction.SETTINGS -> onOpenSettings()
                            DoubleTapAction.SEARCH -> viewModel.setSearchOpen(true)
                            DoubleTapAction.APP_DRAWER -> viewModel.setAppDrawerOpen(true)
                            DoubleTapAction.NONE -> {}
                        }
                    }
                )
            }
            .pointerInput(uiState.preferences) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDragOffset = 0f },
                    onDragEnd = {
                        if (verticalDragOffset < -80f) {
                            // Swiped Up
                            if (uiState.preferences.swipeUpAction == SwipeAction.APP_DRAWER) {
                                viewModel.setAppDrawerOpen(true)
                            }
                        } else if (verticalDragOffset > 80f) {
                            // Swiped Down
                            if (uiState.preferences.swipeDownAction == SwipeAction.SEARCH) {
                                viewModel.setSearchOpen(true)
                            }
                        }
                        verticalDragOffset = 0f
                    },
                    onDragCancel = { verticalDragOffset = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        verticalDragOffset += dragAmount
                    }
                )
            }
            .testTag("home_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar with Subtle Settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Minimal Clock & Date
            HomeClock(
                showClock = uiState.preferences.showClock,
                showDate = uiState.preferences.showDate,
                is24Hour = uiState.preferences.is24HourFormat,
                onClockClick = { viewModel.setAppDrawerOpen(true) },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Pinned Apps and Categories List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("home_apps_list")
            ) {
                // 1. Favorite Apps Section
                if (uiState.preferences.showFavoritesOnHome && uiState.favoriteApps.isNotEmpty()) {
                    item(key = "header_favorites") {
                        HomeSectionHeader(title = "Favorites")
                    }

                    items(
                        items = uiState.favoriteApps,
                        key = { "fav_${it.packageName}" }
                    ) { app ->
                        AppItemView(
                            app = app,
                            displayMode = uiState.preferences.displayMode,
                            iconSize = uiState.preferences.iconSize,
                            onClick = { LauncherUtils.launchApp(context, app.packageName) },
                            onLongClick = { viewModel.openContextMenu(app) }
                        )
                    }

                    item(key = "spacer_favorites") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 2. Categories with Apps
                if (uiState.preferences.showCategoriesOnHome) {
                    uiState.categories.forEach { category ->
                        val appsInCategory = uiState.categoryAppsMap[category.id] ?: emptyList()
                        if (appsInCategory.isNotEmpty()) {
                            item(key = "header_cat_${category.id}") {
                                HomeSectionHeader(title = category.name)
                            }

                            items(
                                items = appsInCategory,
                                key = { "cat_${category.id}_${it.packageName}" }
                            ) { app ->
                                AppItemView(
                                    app = app,
                                    displayMode = uiState.preferences.displayMode,
                                    iconSize = uiState.preferences.iconSize,
                                    onClick = { LauncherUtils.launchApp(context, app.packageName) },
                                    onLongClick = { viewModel.openContextMenu(app) }
                                )
                            }

                            item(key = "spacer_cat_${category.id}") {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // If no favorites or categories on home yet, show hint
                if (uiState.favoriteApps.isEmpty() && uiState.categories.isEmpty()) {
                    item(key = "home_empty_hint") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Swipe up for all apps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Long press any app to add it to Home",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            // Bottom Search / Drawer Trigger Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { viewModel.setAppDrawerOpen(true) }
                        .testTag("home_bottom_drawer_pill"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}
