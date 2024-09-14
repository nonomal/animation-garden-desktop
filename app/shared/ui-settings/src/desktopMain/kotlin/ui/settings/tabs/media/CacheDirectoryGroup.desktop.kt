package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import me.him188.ani.app.desktop.projectDirectories
import me.him188.ani.app.desktop.torrentCacheDir
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import java.awt.Desktop
import java.io.File

@Composable
actual fun SettingsScope.CacheDirectoryGroup(state: CacheDirectoryGroupState) {
    Group({ Text("存储设置") }) {
        val mediaCacheSettings by state.mediaCacheSettingsState

        val defaultSaveDir = remember { projectDirectories.torrentCacheDir.absolutePath }
        val currentSaveDir: String by remember {
            derivedStateOf {
                mediaCacheSettings.saveDir ?: defaultSaveDir
            }
        }
        TextFieldItem(
            currentSaveDir,
            title = { Text("BT 视频缓存位置") },
            onValueChangeCompleted = {
                state.mediaCacheSettingsState.update(mediaCacheSettings.copy(saveDir = it))
            },
            textFieldDescription = {
                Text("修改后不会自动迁移数据，也不会自动删除旧数据。\n如需删除旧数据，请在修改之前点击 \"打开 BT 缓存目录\" 并删除该目录下的所有文件。\n\n重启生效")
            },
            extra = { textFieldValue ->
                val directoryPicker = rememberDirectoryPickerLauncher(
                    "选择视频保存目录",
                    currentSaveDir,
                ) {
                    it?.let {
                        textFieldValue.value = it.file.absolutePath
                    }
                }
                OutlinedButton({ directoryPicker.launch() }) {
                    Text("打开目录选择")
                }
            },
        )
        RowButtonItem(
            title = { Text("打开 BT 缓存目录") },
            icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            onClick = {
                Desktop.getDesktop().open(
                    File(mediaCacheSettings.saveDir ?: projectDirectories.torrentCacheDir.absolutePath),
                )
            },
        )
    }
}
