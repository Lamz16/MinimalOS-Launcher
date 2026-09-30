package com.example.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.AppCategoryEntity
import com.example.data.model.InstalledApp
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.RenameAppDialog
import com.example.util.LauncherUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenuDialog(
    app: InstalledApp,
    categories: List<AppCategoryEntity>,
    onDismiss: () -> Unit,
    onToggleFavorite: (InstalledApp) -> Unit,
    onToggleHide: (InstalledApp) -> Unit,
    onRename: (InstalledApp, String) -> Unit,
    onToggleCategory: (InstalledApp, Long) -> Unit,
    onCreateCategory: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRenameDialog by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("app_context_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: App title and package
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = app.displayLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action: Open
            MenuActionItem(
                icon = Icons.Outlined.PlayArrow,
                title = "Open App",
                tag = "menu_action_open",
                onClick = {
                    onDismiss()
                    LauncherUtils.launchApp(context, app.packageName)
                }
            )

            // Action: Toggle Favorite (Add to Home)
            MenuActionItem(
                icon = if (app.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                title = if (app.isFavorite) "Remove from Home" else "Add to Home (Favorite)",
                tag = "menu_action_favorite",
                onClick = {
                    onToggleFavorite(app)
                    onDismiss()
                }
            )

            // Action: Rename App
            MenuActionItem(
                icon = Icons.Outlined.Edit,
                title = "Rename",
                tag = "menu_action_rename",
                onClick = {
                    showRenameDialog = true
                }
            )

            // Action: Assign Category
            MenuActionItem(
                icon = Icons.Outlined.Folder,
                title = "Categories",
                tag = "menu_action_category",
                onClick = {
                    showCategoryPicker = true
                }
            )

            // Action: Hide App
            MenuActionItem(
                icon = Icons.Outlined.VisibilityOff,
                title = if (app.isHidden) "Unhide App" else "Hide App",
                tag = "menu_action_hide",
                onClick = {
                    onToggleHide(app)
                }
            )

            // Action: App Info
            MenuActionItem(
                icon = Icons.Outlined.Info,
                title = "App Info",
                tag = "menu_action_info",
                onClick = {
                    onDismiss()
                    LauncherUtils.openAppInfo(context, app.packageName)
                }
            )

            // Action: Uninstall
            MenuActionItem(
                icon = Icons.Outlined.Delete,
                title = "Uninstall",
                tag = "menu_action_uninstall",
                onClick = {
                    onDismiss()
                    LauncherUtils.uninstallApp(context, app.packageName)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showRenameDialog) {
        RenameAppDialog(
            app = app,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                showRenameDialog = false
                onRename(app, newName)
                onDismiss()
            }
        )
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            app = app,
            categories = categories,
            onToggleCategory = { catId -> onToggleCategory(app, catId) },
            onCreateCategory = onCreateCategory,
            onDismiss = { showCategoryPicker = false }
        )
    }
}

@Composable
private fun MenuActionItem(
    icon: ImageVector,
    title: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
