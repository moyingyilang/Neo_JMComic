package com.yourpackage.neojmcomic.utils.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 基础列表组件（整合List相关文件核心功能） */
@Composable
fun <T> UiList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items) { item ->
            itemContent(item)
            Divider(color = UiTheme.colorScheme.surfaceVariant)
        }
    }
}

/** 列表项组件（整合ListItem+ListItemText） */
@Composable
fun UiListItem(
    primaryText: String,
    secondaryText: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = UiTheme.spacing.md,
                vertical = if (secondaryText.isNullOrEmpty()) UiTheme.spacing.sm else UiTheme.spacing.md
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Text(
            text = primaryText,
            style = UiTypography.body1,
            color = UiTheme.colorScheme.onBackground
        )
        secondaryText?.let {
            Text(
                text = it,
                style = UiTypography.body2,
                color = UiTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = UiTheme.spacing.sm)
            )
        }
    }
}
