package com.yourpackage.neojmcomic.utils.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.align
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 开关组件（整合SwitchBase核心功能） */
@Composable
fun UiSwitch(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = UiTheme.spacing.md, vertical = UiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = UiTypography.body1,
            color = UiTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onCheckedChange?.invoke(it)
            },
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = UiTheme.colorScheme.onPrimary,
                checkedTrackColor = UiTheme.colorScheme.primary
            )
        )
    }
}
