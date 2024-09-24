/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.OverrideNavigation
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.interaction.WindowDragArea
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.tabs.AboutTab
import me.him188.ani.app.ui.settings.tabs.DebugTab
import me.him188.ani.app.ui.settings.tabs.app.AppSettingsTab
import me.him188.ani.app.ui.settings.tabs.media.AutoCacheGroup
import me.him188.ani.app.ui.settings.tabs.media.CacheDirectoryGroup
import me.him188.ani.app.ui.settings.tabs.media.MediaSelectionGroup
import me.him188.ani.app.ui.settings.tabs.media.TorrentEngineGroup
import me.him188.ani.app.ui.settings.tabs.media.VideoResolverGroup
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceGroup
import me.him188.ani.app.ui.settings.tabs.network.DanmakuGroup
import me.him188.ani.app.ui.settings.tabs.network.GlobalProxyGroup
import me.him188.ani.app.ui.settings.tabs.network.OtherTestGroup
import me.him188.ani.utils.platform.isMobile

/**
 * @see renderPreferenceTab 查看名称
 */
typealias SettingsTab = me.him188.ani.app.navigation.SettingsTab

@Composable
fun SettingsPage(
    vm: SettingsViewModel,
    showBack: Boolean,
    modifier: Modifier = Modifier,
    initialTab: SettingsTab = SettingsTab.Default,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val appBarColors = AniThemeDefaults.topAppBarColors()
    Scaffold(
        modifier,
        topBar = {
            WindowDragArea {
                TopAppBar(
                    title = { Text("设置") },
                    navigationIcon = {
                        if (showBack) {
                            TopAppBarGoBackButton()
                        }
                    },
                    colors = appBarColors,
                    windowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                )
            }
        },
        contentWindowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { topBarPaddings ->
        val pageCount by remember {
            derivedStateOf {
                SettingsTab.entries.run { if (vm.isInDebugMode) size else (size - 1) }
            }
        }
        val pagerState = rememberPagerState(
            initialPage = initialTab.ordinal,
            pageCount = { pageCount },
        )

        val scope = rememberCoroutineScope()

        // Pager with TabRow
        Column(Modifier.padding(topBarPaddings).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    )
                },
                containerColor = appBarColors.containerColor,
                contentColor = appBarColors.titleContentColor,
                modifier = Modifier.fillMaxWidth(),
                divider = {},
            ) {
                val tabs by remember {
                    derivedStateOf {
                        SettingsTab.entries
                            .filter { if (vm.isInDebugMode) true else it != SettingsTab.DEBUG }
                    }
                }
                tabs.forEachIndexed { index, tabId ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(text = renderPreferenceTab(tabId))
                        },
                    )
                }
            }
            HorizontalDivider()

            OverrideNavigation(
                remember(scope, pagerState) {
                    { old ->
                        object : AniNavigator by old {
                            override fun navigateSettings(tab: SettingsTab) {
                                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            }
                        }
                    }
                },
            ) {
                HorizontalPager(
                    state = pagerState,
                    Modifier.fillMaxSize(),
                    userScrollEnabled = LocalPlatform.current.isMobile(),
                ) { index ->
                    val type = SettingsTab.entries[index]
                    Column(Modifier.fillMaxSize().padding(contentPadding)) {
                        when (type) {
                            SettingsTab.MEDIA -> {
                                SettingsTab(Modifier.fillMaxSize()) {
                                    VideoResolverGroup(vm.videoResolverSettingsState)
                                    AutoCacheGroup(vm.mediaCacheSettingsState)

                                    TorrentEngineGroup(vm.torrentSettingsState)
                                    CacheDirectoryGroup(vm.cacheDirectoryGroupState)
                                    MediaSelectionGroup(vm.mediaSelectionGroupState)
                                }
                            }

                            SettingsTab.NETWORK -> {
                                SettingsTab(Modifier.fillMaxSize()) {
                                    GlobalProxyGroup(vm.proxySettingsState)
                                    MediaSourceGroup(vm.mediaSourceGroupState, vm.editMediaSourceState)
                                    OtherTestGroup(vm.otherTesters)
                                    DanmakuGroup(vm.danmakuSettingsState, vm.danmakuServerTesters)
                                }
                            }

                            SettingsTab.ABOUT -> {
                                val toaster = LocalToaster.current
                                AboutTab(
                                    modifier = Modifier.fillMaxSize(),
                                    onTriggerDebugMode = {
                                        if (vm.debugTriggerState.triggerDebugMode()) {
                                            toaster.toast("已开启调试模式")
                                        }
                                    },
                                )
                            }

                            SettingsTab.APP -> AppSettingsTab(
                                vm.softwareUpdateGroupState,
                                vm.uiSettings,
                                vm.videoScaffoldConfig,
                                vm.danmakuFilterConfigState,
                                vm.danmakuRegexFilterState,
                                vm.isInDebugMode,
                                Modifier.fillMaxSize(),
                            )

                            SettingsTab.DEBUG -> DebugTab(
                                vm.debugSettingsState,
                                Modifier.fillMaxSize(),
                                onDisableDebugMode = { scope.launch { pagerState.animateScrollToPage(0) } },
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun renderPreferenceTab(
    tab: SettingsTab,
): String {
    return when (tab) {
        SettingsTab.APP -> "应用与界面"
        SettingsTab.NETWORK -> "数据源与网络"
        SettingsTab.MEDIA -> "播放与缓存"
        SettingsTab.ABOUT -> "关于"
        SettingsTab.DEBUG -> "调试"
    }
}

@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    content: @Composable SettingsScope.() -> Unit,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(
            currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding,
        ),
    ) {
        val scope = remember(this) {
            object : SettingsScope(), ColumnScope by this {}
        }
        scope.content()
    }
}
