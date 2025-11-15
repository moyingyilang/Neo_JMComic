package com.yourpackage.neojmcomic.utils.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 圆形进度条（加载状态，整合CircularProgress核心功能） */
@Composable
fun UiCircularProgress(
    modifier: Modifier = Modifier,
    size: Int = 40,
    strokeWidth: Int = 4,
    indeterminate: Boolean = true,
    progress: Float = 0f
) {
    CircularProgressIndicator(
        progress = if (indeterminate) { { 0.5f } } else { { progress } },
        modifier = modifier.size(size.dp),
        strokeWidth = strokeWidth.dp,
        color = UiTheme.colorScheme.primary,
        trackColor = UiTheme.colorScheme.surfaceVariant
    )
}
