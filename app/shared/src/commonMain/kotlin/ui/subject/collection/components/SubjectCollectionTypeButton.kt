package me.him188.ani.app.ui.subject.collection.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

object SubjectCollectionTypeButtonDefaults {
    @Composable
    fun collectedButtonColors() = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun collectedBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.612f))
}

/**
 * 展示当前收藏类型的按钮, 点击时可以弹出菜单 [EditCollectionTypeDropDown] 选择要修改的收藏类型.
 *
 * 已经收藏时为一个 [OutlinedButton], 未收藏时为 [Button].
 *
 * 这是最基础的按钮. 更多时候, 你可能需要使用 [EditableSubjectCollectionTypeButton].
 *
 * @param type 当前收藏类型
 * @param onEdit 当修改类型时调用
 */
@Composable
fun SubjectCollectionTypeButton(
    type: UnifiedCollectionType,
    onEdit: (newType: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    collectedColors: ButtonColors = SubjectCollectionTypeButtonDefaults.collectedButtonColors(),
    collectedBorder: BorderStroke = SubjectCollectionTypeButtonDefaults.collectedBorder(),
) {
    val action = remember(type) {
        SubjectCollectionActionsForCollect.find { it.type == type }
    }
    Box(modifier) {
        var showDropdown by rememberSaveable { mutableStateOf(false) }
        val onClick = remember {
            {
                showDropdown = true
            }
        }
        if (type != UnifiedCollectionType.NOT_COLLECTED) {
            OutlinedButton(
                onClick = onClick,
                colors = collectedColors,
                border = collectedBorder,
                enabled = enabled,
            ) {
                if (action != null) {
                    action.icon()
                    Row(Modifier.padding(start = 8.dp)) {
                        Text(renderCollectionTypeAsCurrent(type))
                    }
                } else {
                    Text("载入") // 随便什么都行, 占空间
                }
            }
        } else {
            Button(
                onClick = onClick,
                enabled = enabled,
            ) {
                if (action != null) {
                    action.icon()
                    Row(Modifier.padding(start = 8.dp)) {
                        action.title()
                    }
                } else {
                    Text("载入") // 随便什么都行, 占空间
                }
            }

        }
        EditCollectionTypeDropDown(
            currentType = type,
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            onClick = {
                showDropdown = false
                onEdit(it.type)
            },
        )
    }
}

@Stable
private fun renderCollectionTypeAsCurrent(type: UnifiedCollectionType): String {
    return when (type) {
        UnifiedCollectionType.WISH -> "已想看"
        UnifiedCollectionType.DOING -> "已在看"
        UnifiedCollectionType.DONE -> "已看过"
        UnifiedCollectionType.ON_HOLD -> "已搁置"
        UnifiedCollectionType.DROPPED -> "已抛弃"
        UnifiedCollectionType.NOT_COLLECTED -> "未追番"
    }
}
