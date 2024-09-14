package me.him188.ani.app.ui.subject.cache

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.tabs.media.autoCacheDescription

@Composable
fun SettingsScope.AutoCacheGroup(
    onClickGlobalCacheSettings: () -> Unit,
    onClickGlobalCacheManage: () -> Unit,
) {
    Group(
        title = { Text("自动缓存") },
        description = {
            Text("自动缓存未观看的剧集")
        },
    ) {
        var useGlobalSettings by remember { mutableStateOf(true) }
        SwitchItem(
            title = { Text("使用全局设置") },
            description = { Text("关闭后可为该番剧单独设置 (暂不支持单独设置)") },
            checked = useGlobalSettings,
            onCheckedChange = { useGlobalSettings = !useGlobalSettings },
            enabled = false,
        )

        AnimatedVisibility(!useGlobalSettings) {
            var sliderValue by remember { mutableFloatStateOf(0f) }
            SliderItem(
                title = { Text("最大自动缓存话数") },
                description = {
                    Row {
                        Text(remember(sliderValue) { autoCacheDescription(sliderValue) })
                        if (sliderValue == 10f) {
                            Text("可能会占用大量空间", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    enabled = !useGlobalSettings,
                )
            }
            HorizontalDividerItem()
        }

        AnimatedVisibility(useGlobalSettings) {
            RowButtonItem(
                onClick = onClickGlobalCacheSettings,
                icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            ) { Text("查看全局设置") }
        }

        RowButtonItem(
            onClick = onClickGlobalCacheManage,
            icon = { Icon(Icons.AutoMirrored.Rounded.ViewList, null) },
        ) { Text("管理全部缓存") }
    }
}
