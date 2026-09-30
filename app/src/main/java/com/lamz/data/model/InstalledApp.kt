package com.lamz.data.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Immutable model representing an installed launcher app.
 */
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val categoryIds: Set<Long> = emptySet(),
    val hasNotification: Boolean = false,
    val installTime: Long = 0L,
    val iconBitmap: ImageBitmap? = null
) {
    val displayLabel: String
        get() = if (!customLabel.isNullOrBlank()) customLabel else label
}
