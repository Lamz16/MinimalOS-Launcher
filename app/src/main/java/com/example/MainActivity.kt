package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.drawer.AppContextMenuDialog
import com.example.ui.drawer.AppDrawerSheet
import com.example.ui.home.HomeScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MinimalOSTheme
import com.example.ui.viewmodel.LauncherViewModel

enum class CurrentScreen {
    HOME,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.provideFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

            MinimalOSTheme(
                theme = uiState.preferences.theme,
                fontSize = uiState.preferences.fontSize
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        CurrentScreen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenSettings = { currentScreen = CurrentScreen.SETTINGS }
                            )

                            // Drawer overlay
                            if (uiState.isAppDrawerOpen) {
                                AppDrawerSheet(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }

                            // Search overlay
                            if (uiState.isSearchOpen) {
                                SearchScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                        }

                        CurrentScreen.SETTINGS -> {
                            SettingsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = { currentScreen = CurrentScreen.HOME }
                            )
                        }
                    }

                    // App Context Menu Dialog / Bottom Sheet
                    uiState.activeContextMenuApp?.let { app ->
                        AppContextMenuDialog(
                            app = app,
                            categories = uiState.categories,
                            onDismiss = { viewModel.closeContextMenu() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleHide = { viewModel.toggleHide(it) },
                            onRename = { targetApp, newName -> viewModel.renameApp(targetApp, newName) },
                            onToggleCategory = { targetApp, catId -> viewModel.toggleAppCategory(targetApp, catId) },
                            onCreateCategory = { name -> viewModel.createCategory(name) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultLauncher(this)
    }
}
