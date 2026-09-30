package com.example.ui.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppDisplayMode
import com.example.data.model.IconSize
import com.example.data.model.InstalledApp
import com.example.util.IconCache

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItemView(
    app: InstalledApp,
    displayMode: AppDisplayMode,
    iconSize: IconSize,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cachedIcon by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }

    if (displayMode != AppDisplayMode.TEXT_ONLY) {
        LaunchedEffect(app.packageName) {
            cachedIcon = IconCache.getAppIcon(context, app.packageName)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("app_item_${app.packageName}"),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (displayMode != AppDisplayMode.TEXT_ONLY) {
                val sizeDp = iconSize.dpSize.dp
                if (cachedIcon != null) {
                    Image(
                        bitmap = cachedIcon!!,
                        contentDescription = app.displayLabel,
                        modifier = Modifier
                            .size(sizeDp)
                            .clip(RoundedCornerShape(sizeDp / 4))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(sizeDp)
                            .clip(RoundedCornerShape(sizeDp / 4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.displayLabel.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (displayMode != AppDisplayMode.ICON_ONLY) {
                Text(
                    text = app.displayLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (app.isFavorite) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (app.isFavorite && displayMode == AppDisplayMode.TEXT_ONLY) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
