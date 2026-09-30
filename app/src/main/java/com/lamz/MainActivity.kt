package com.lamz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamz.ui.drawer.AppContextMenuDialog
import com.lamz.ui.drawer.AppDrawerSheet
import com.lamz.ui.home.HomeScreen
import com.lamz.ui.search.SearchScreen
import com.lamz.ui.settings.SettingsScreen
import com.lamz.ui.theme.MinimalOSTheme
import com.lamz.ui.viewmodel.LauncherViewModel
import com.lamz.util.LauncherUtils
import com.lamz.util.LauncherShortcuts
import kotlinx.coroutines.flow.MutableStateFlow

enum class CurrentScreen {
    HOME,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.provideFactory(this)
    }

    private val requestHomeRole = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // RESULT_OK is not consistent across OEM role controllers; ask the
        // PackageManager again instead of trusting the result code.
        viewModel.checkDefaultLauncher(this)
    }

    private enum class LauncherDestination { HOME, SEARCH, DRAWER }
    private data class LauncherCommand(val destination: LauncherDestination, val id: Int)

    private val launcherCommand = MutableStateFlow(LauncherCommand(LauncherDestination.HOME, 0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LauncherShortcuts.publish(this)
        handleLauncherIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val command by launcherCommand.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

            // With singleTask, pressing Home can deliver a new intent to the
            // existing Activity. A launcher should always return to its home UI.
            LaunchedEffect(command.id) {
                currentScreen = CurrentScreen.HOME
                viewModel.setAppDrawerOpen(false)
                viewModel.setSearchOpen(false)
                viewModel.closeContextMenu()
                when (command.destination) {
                    LauncherDestination.SEARCH -> viewModel.setSearchOpen(true)
                    LauncherDestination.DRAWER -> viewModel.setAppDrawerOpen(true)
                    LauncherDestination.HOME -> Unit
                }
            }

            MinimalOSTheme(
                theme = uiState.preferences.theme,
                fontSize = uiState.preferences.fontSize
            ) {
                ConfigureSystemBars()
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
                                onBack = { currentScreen = CurrentScreen.HOME },
                                onSetAsDefaultLauncher = ::requestDefaultHomeRole,
                                onOpenWallpaperPicker = { LauncherUtils.openWallpaperPicker(this@MainActivity) },
                                onOpenNotificationSettings = {
                                    LauncherUtils.openNotificationListenerSettings(this@MainActivity)
                                }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLauncherIntent(intent)
        viewModel.checkDefaultLauncher(this)
    }

    private fun handleLauncherIntent(intent: Intent?) {
        val destination = when (intent?.action) {
            LauncherShortcuts.ACTION_OPEN_SEARCH -> LauncherDestination.SEARCH
            LauncherShortcuts.ACTION_OPEN_DRAWER -> LauncherDestination.DRAWER
            else -> LauncherDestination.HOME
        }
        launcherCommand.value = LauncherCommand(destination, launcherCommand.value.id + 1)
    }

    private fun requestDefaultHomeRole() {
        val roleRequest = LauncherUtils.createDefaultHomeRoleRequest(this)
        if (roleRequest != null) {
            requestHomeRole.launch(roleRequest)
        } else {
            // Android 9 and below (or OEMs without the role API) expose the
            // chooser from the Home settings page.
            LauncherUtils.openDefaultHomeSettings(this)
        }
    }
}

@Composable
private fun ComponentActivity.ConfigureSystemBars() {
    val view = LocalView.current
    val colors = MaterialTheme.colorScheme

    SideEffect {
        // Keep the notification area readable even when Home uses a wallpaper.
        window.statusBarColor = colors.background.toArgb()
        // Deliberately use the inverse text color at the bottom for a clear,
        // consistent separation between the launcher and system navigation.
        window.navigationBarColor = colors.onBackground.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = colors.background.luminance() > 0.5f
            isAppearanceLightNavigationBars = colors.onBackground.luminance() > 0.5f
        }
    }
}
