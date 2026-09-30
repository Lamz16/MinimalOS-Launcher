package com.lamz.ui.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamz.ui.viewmodel.LauncherUiState
import com.lamz.ui.viewmodel.LauncherViewModel
import com.lamz.util.LauncherUtils

@Composable
fun AppDrawerSheet(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler(enabled = true) {
        viewModel.setAppDrawerOpen(false)
    }

    val displayedApps = remember(uiState.visibleApps, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.visibleApps
        } else {
            val q = uiState.searchQuery.trim().lowercase()
            uiState.visibleApps.filter {
                it.displayLabel.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }
    }

    // Group apps by first letter
    val groupedApps = remember(displayedApps) {
        displayedApps.groupBy {
            val first = it.displayLabel.firstOrNull()?.uppercaseChar() ?: '#'
            if (first.isLetter()) first else '#'
        }.toSortedMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .testTag("app_drawer_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar at Top
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Search apps...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.setSearchQuery("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        if (uiState.categories.isNotEmpty() || uiState.favoriteApps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedCategoryFilter == null,
                    onClick = { viewModel.selectCategoryFilter(null) },
                    label = { Text("All (${uiState.allApps.count { !it.isHidden }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("filter_all_chip")
                )

                uiState.categories.forEach { category ->
                    Spacer(modifier = Modifier.width(6.dp))
                    val count = uiState.categoryAppsMap[category.id]?.size ?: 0
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == category.id,
                        onClick = {
                            viewModel.selectCategoryFilter(
                                if (uiState.selectedCategoryFilter == category.id) null else category.id
                            )
                        },
                        label = { Text("${category.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("filter_category_${category.id}_chip")
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // App List
        if (displayedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No apps found for \"${uiState.searchQuery}\"" else "No apps available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("app_drawer_list")
            ) {
                groupedApps.forEach { (headerChar, appsInGroup) ->
                    item(key = "header_$headerChar") {
                        Text(
                            text = headerChar.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    items(
                        items = appsInGroup,
                        key = { it.packageName }
                    ) { app ->
                        AppItemView(
                            app = app,
                            displayMode = uiState.preferences.displayMode,
                            iconSize = uiState.preferences.iconSize,
                            onClick = {
                                viewModel.setAppDrawerOpen(false)
                                LauncherUtils.launchApp(context, app.packageName)
                            },
                            onLongClick = {
                                viewModel.openContextMenu(app)
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
