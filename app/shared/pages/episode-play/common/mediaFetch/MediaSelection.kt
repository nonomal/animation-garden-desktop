package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.icons.MediaSourceIcons
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize


private inline val WINDOW_VERTICAL_PADDING get() = 8.dp

// For search: "数据源"
/**
 * 通用的数据源选择器. See preview
 *
 * @param actions shown at the bottom
 */
@Composable
fun MediaSelector(
    state: MediaSelectorState,
    modifier: Modifier = Modifier,
    sourceResults: MediaSelectorSourceResults = emptyMediaSelectorSourceResults(),
    onClickItem: ((Media) -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) = Surface {
    Column(modifier) {
        if (sourceResults.anyLoading) {
            Row(Modifier.fillMaxWidth()) {

                // 刻意一直展示一个一直在动的进度条, 因为实测其实资源都是一起来的, 也就是进度很多时候只有 0 和 1.
                // 如果按进度展示进度条, 就会一直是 0, 进度条整个是白色的, 看不出来, 不容易区分是正在加载还是加载完了.
                LinearProgressIndicator(
                    Modifier.padding(bottom = WINDOW_VERTICAL_PADDING)
                        .fillMaxWidth()
                )
            }
        }

        val lazyListState = rememberLazyListState()
        var isShowDetails by remember { mutableStateOf(false) }
        LazyColumn(
            Modifier.padding(bottom = WINDOW_VERTICAL_PADDING).weight(1f, fill = false),
            lazyListState,
        ) {
            item {

                Row(
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isShowDetails = !isShowDetails },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        remember(
                            sourceResults.anyLoading,
                            sourceResults.enabledSourceCount,
                            sourceResults.totalSourceCount
                        ) {
                            val status = if (sourceResults.anyLoading) "正在查询" else "已查询"
                            "$status ${sourceResults.enabledSourceCount}/${sourceResults.totalSourceCount} 数据源"
                        },
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    var showHelp by remember { mutableStateOf(false) }
                    if (showHelp) {
                        BasicAlertDialog({ showHelp = false }) {
                            MediaSelectorHelp({ showHelp = false })
                        }
                    }
                    IconButton({ showHelp = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Help, "帮助")
                    }
                    val navigator = LocalNavigator.current
                    IconButton({ navigator.navigatePreferences(SettingsTab.MEDIA) }) {
                        Icon(Icons.Outlined.Settings, "设置")
                    }

                    // TODO: 允许展开的话可能要考虑需要把下面 FlowList 变成 Grid 
//                    IconButton({ isShowDetails = !isShowDetails }) {
//                        if (isShowDetails) {
//                            Icon(Icons.Rounded.UnfoldLess, "展示更少")
//                        } else {
//                            Icon(Icons.Rounded.UnfoldMore, "展示更多")
//                        }
//                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val onClick: (MediaSourceResultPresentation) -> Unit = remember(state) {
                        { item ->
                            if (item.isDisabled || item.isFailed) {
                                item.restart()
                            } else {
                                state.preferMediaSource(item.mediaSourceId, removeOnExist = true)
                            }
                        }
                    }
                    MediaSourceResultsRow(
                        isShowDetails,
                        sourceResults.btSources,
                        sourceSelected = { state.selectedMediaSource == it },
                        onClick = onClick,
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(MediaSourceIcons.KindBT, null)
                                ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                                    Box(Modifier.padding(top = 2.dp), contentAlignment = Alignment.Center) {
                                        Text("在线", Modifier.alpha(0f)) // 相同宽度
                                        Text("BT")
                                    }
                                }
                            }
                        },
                        Modifier.animateItemPlacement()
                    )
                    MediaSourceResultsRow(
                        isShowDetails,
                        sourceResults.webSources,
                        sourceSelected = { state.selectedMediaSource == it },
                        onClick = onClick,
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(MediaSourceIcons.KindWeb, null)
                                ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                                    Box(Modifier.padding(top = 2.dp), contentAlignment = Alignment.Center) {
                                        Text("在线")
                                    }
                                }
                            }
//                            Icon(MediaSourceIcons.Web, null)
//                            Text("在线", Modifier.padding(start = 4.dp))
                        },
                        Modifier.animateItemPlacement()
                    )
                }
            }

//            item {
//                HorizontalDivider(Modifier.padding(top = 8.dp))
//            }

            stickyHeader {
                val isStuck by remember(lazyListState) {
                    derivedStateOf {
                        lazyListState.firstVisibleItemIndex == 2
                    }
                }
                Surface(
//                    tonalElevation = if (isStuck) 3.dp else 0.dp,
                    Modifier.animateItemPlacement(),
                ) {
                    Column {
                        Column(
                            Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                remember(state.candidates.size, state.mediaList.size) {
                                    "筛选到 ${state.candidates.size}/${state.mediaList.size} 条资源"
                                },
                                style = MaterialTheme.typography.titleMedium,
                            )

                            MediaSelectorFilters(state)
                        }
                        if (isStuck) {
                            HorizontalDivider(Modifier.fillMaxWidth(), thickness = 2.dp)
                        }
                    }
                }
            }

            items(state.candidates, key = { it.mediaId }) { item ->
                MediaItem(
                    item,
                    state.selected == item,
                    state,
                    onClick = {
                        state.select(item)
                        onClickItem?.invoke(item)
                    },
                    Modifier
                        .animateItemPlacement()
                        .fillMaxWidth().padding(bottom = 8.dp),
                )
            }
            item { } // dummy spacer
        }

        if (actions != null) {
            HorizontalDivider(Modifier.padding(bottom = 8.dp))

            Row(
                Modifier.align(Alignment.End).padding(bottom = 8.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    actions()
                }
            }
        }
    }
}

@Composable
private fun MediaSourceResultsRow(
    expanded: Boolean,
    list: List<MediaSourceResultPresentation>,
    sourceSelected: (String) -> Boolean,
    onClick: (MediaSourceResultPresentation) -> Unit,
    label: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                label()
            }
        }

        if (expanded) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier
            ) {
                for (item in list) {
                    MediaSourceResultCard(
                        sourceSelected(item.mediaSourceId),
                        expanded = true,
                        { onClick(item) },
                        item,
                        Modifier
                            .widthIn(min = 100.dp)
                            .ifThen(item.isDisabled) {
                                alpha(1 - 0.618f)
                            }
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier,
            ) {
                items(list, key = { it.mediaSourceId }) { item ->
                    MediaSourceResultCard(
                        sourceSelected(item.mediaSourceId),
                        expanded = false,
                        { onClick(item) },
                        item,
                        Modifier
                            .ifThen(item.isDisabled) {
                                alpha(1 - 0.618f)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaSourceResultCard(
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    source: MediaSourceResultPresentation,
    modifier: Modifier = Modifier
) {
    if (expanded) {
        OutlinedCard(
            onClick = onClick,
            modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                else CardDefaults.elevatedCardColors().containerColor
            ),
        ) {
            Column(
                Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaSourceIcon(id = source.mediaSourceId, allowText = false)

                    Text(
                        renderMediaSource(source.mediaSourceId),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 80.dp),
                    )
                }

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Row(
                        Modifier.heightIn(min = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            source.isDisabled -> {
                                Icon(Icons.Outlined.HorizontalRule, null)
                                Text("点击临时启用")
                            }

                            source.isLoading -> {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 3.dp)
                                Text(remember(source.totalCount) { "${source.totalCount}" })
                            }

                            source.isFailed -> {
                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
                                    Icon(Icons.Outlined.Close, "查询失败")
                                    Text("点击重试")
                                }
                            }

                            else -> {
                                Icon(Icons.Outlined.Check, "查询成功")
                                Text(remember(source.totalCount) { "${source.totalCount}" })
                            }
                        }
                    }
                }

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    }
                }
            }
        }
    } else {
        InputChip(
            selected,
            onClick,
            label = {
                when {
                    source.isDisabled -> {
                        Icon(Icons.Outlined.HorizontalRule, null)
                    }

                    source.isLoading -> {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 3.dp)
                    }

                    source.isFailed -> {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
                            Icon(Icons.Outlined.Close, "查询失败")
                        }
                    }

                    else -> {
                        Text(remember(source.totalCount) { "${source.totalCount}" })
                    }
                }
            },
            modifier = modifier.heightIn(min = 40.dp),
            leadingIcon = {
                MediaSourceIcon(id = source.mediaSourceId)
            }
        )
//        Row(
//            Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            MediaSourceIcon(id = source.mediaSourceId)
//            when {
//                source.isDisabled -> {
//                    Icon(Icons.Outlined.HorizontalRule, null)
//                }
//
//                source.isLoading -> {
//                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 3.dp)
//                }
//
//                source.isFailed -> {
//                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
//                        Icon(Icons.Outlined.Close, "查询失败")
//                    }
//                }
//
//                else -> {
//                    Text(remember(source.totalCount) { "${source.totalCount}" })
//                }
//            }
//        }
    }
}


private inline val minWidth get() = 60.dp
private inline val maxWidth get() = 120.dp

/**
 * 筛选
 */
@Composable
private fun MediaSelectorFilters(
    state: MediaSelectorState,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
//        MediaSelectorFilterChip(
//            selected = state.selectedMediaSource,
//            allValues = { state.mediaSources },
//            onSelect = { state.preferMediaSource(it) },
//            onDeselect = { state.preferMediaSource(it, removeOnExist = true) },
//            name = { Text("数据源") },
//            Modifier.widthIn(min = minWidth, max = maxWidth),
//            label = { MediaSelectorFilterChipText(renderMediaSource(it)) },
//            leadingIcon = { id ->
//                MediaSourceIcon(id)
//            }
//        )
        MediaSelectorFilterChip(
            selected = state.selectedResolution,
            allValues = { state.resolutions },
            onSelect = { state.preferResolution(it) },
            onDeselect = { state.preferResolution(it, removeOnExist = true) },
            name = { Text("分辨率") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
        )
        MediaSelectorFilterChip(
            selected = state.selectedSubtitleLanguageId,
            allValues = { state.subtitleLanguageIds },
            onSelect = { state.preferSubtitleLanguage(it) },
            onDeselect = { state.preferSubtitleLanguage(it, removeOnExist = true) },
            name = { Text("字幕") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
            label = { MediaSelectorFilterChipText(renderSubtitleLanguage(it)) }
        )
        MediaSelectorFilterChip(
            selected = state.selectedAlliance,
            allValues = { state.alliances },
            onSelect = { state.preferAlliance(it) },
            onDeselect = { state.preferAlliance(it, removeOnExist = true) },
            name = { Text("字幕组") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
        )
    }
}

@Composable
private fun MediaSelectorFilterChipText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * @param selected 选中的值, 为 null 时表示未选中
 * @param name 未被选中时显示
 * @param label 选中时显示
 */
@Composable
private fun <T : Any> MediaSelectorFilterChip(
    selected: T?,
    allValues: () -> List<T>,
    onSelect: (T) -> Unit,
    onDeselect: (T) -> Unit,
    name: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit = { MediaSelectorFilterChipText(it.toString()) },
    leadingIcon: @Composable ((T?) -> Unit)? = null,
) {
    var showDropdown by rememberSaveable {
        mutableStateOf(false)
    }

    val allValuesState by remember(allValues) {
        derivedStateOf(allValues)
    }
    val isSingleValue by remember { derivedStateOf { allValuesState.size == 1 } }
    val selectedState by rememberUpdatedState(selected)

    Box {
        InputChip(
            selected = isSingleValue || selected != null,
            onClick = {
                if (!isSingleValue) {
                    showDropdown = true
                }
            },
            label = {
                if (isSingleValue) {
                    allValuesState.firstOrNull()?.let {
                        label(it)
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            Modifier.alpha(if (selectedState == null) 1f else 0f) // 总是占位
                        ) {
                            name()
                        }
                        selectedState?.let {
                            label(it)
                        }
                    }
                }
            },
            leadingIcon = leadingIcon?.let { { leadingIcon(selectedState) } },
            trailingIcon = if (isSingleValue) null else {
                {
                    if (selected == null) {
                        Icon(Icons.Default.ArrowDropDown, "展开")
                    } else {
                        Icon(
                            Icons.Default.Close, "取消筛选",
                            Modifier.clickable { selectedState?.let { onDeselect(it) } }
                        )
                    }
                }
            },
            modifier = modifier.heightIn(min = 40.dp),
        )

        DropdownMenu(showDropdown, onDismissRequest = { showDropdown = false }) {
            allValuesState.forEach { item ->
                DropdownMenuItem(
                    text = { label(item) },
                    trailingIcon = {
                        if (selectedState == item) {
                            Icon(Icons.Default.Check, "当前选中")
                        }
                    },
                    onClick = {
                        onSelect(item)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * 一个资源的卡片
 */
@Composable
private fun MediaItem(
    media: Media,
    selected: Boolean,
    state: MediaSelectorState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick,
        modifier.width(IntrinsicSize.Min),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else CardDefaults.elevatedCardColors().containerColor
        ),
    ) {
        Column(Modifier.padding(all = 16.dp)) {
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                Text(media.originalTitle)
            }

            // Labels
            FlowRow(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (media.properties.size != FileSize.Zero && media.properties.size != FileSize.Unspecified) {
                    InputChip(
                        false,
                        onClick = {},
                        label = { Text(media.properties.size.toString()) },
                    )
                }
                InputChip(
                    false,
                    onClick = { state.preferResolution(media.properties.resolution) },
                    label = { Text(media.properties.resolution) },
                    enabled = state.selectedResolution != media.properties.resolution,
                )
                media.properties.subtitleLanguageIds.map {
                    InputChip(
                        false,
                        onClick = { state.preferSubtitleLanguage(it) },
                        label = { Text(renderSubtitleLanguage(it)) },
                        enabled = state.selectedSubtitleLanguageId != it,
                    )
                }
            }

            // Bottom row: source, alliance, published time
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Layout note:
                    // On overflow, only the alliance will be ellipsized.

                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(MediaSourceIcons.location(media.location, media.kind), null)

                            Text(
                                remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) },
                                maxLines = 1,
                                softWrap = false,
                            )
                        }

                        Box(Modifier.weight(1f, fill = false), contentAlignment = Alignment.Center) {
                            Text(
                                media.properties.alliance,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        formatDateTime(media.publishedTime, showTime = false),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
