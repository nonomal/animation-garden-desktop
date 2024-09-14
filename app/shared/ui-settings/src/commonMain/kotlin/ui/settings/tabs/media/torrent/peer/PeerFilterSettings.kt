package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import me.him188.ani.app.ui.foundation.IconButton
import me.him188.ani.app.ui.foundation.layout.AnimatedPane1
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton

@Composable
fun PeerFilterSettingsPage(
    state: PeerFilterSettingsState,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator()
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    val isDualPane by derivedStateOf {
        navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded
    }
    val inIpBlockListPane by derivedStateOf { 
        navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
    }
    
    if (!isDualPane) {
        BackHandler(navigator.canNavigateBack()) {
            if (state.searchingBlockedIp) {
                state.stopSearchBlockedIp()
                return@BackHandler
            }
            navigator.navigateBack()
        }
    } else {
        BackHandler(state.searchingBlockedIp) {
            state.stopSearchBlockedIp()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    if (inIpBlockListPane) {
                        if (state.searchingBlockedIp) {
                            val searchQuery by state.searchBlockedIpQuery.collectAsStateWithLifecycle("")
                            TextField(
                                value = searchQuery,
                                onValueChange = { state.setSearchBlockIpQuery(it) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxSize().focusRequester(focusRequester)
                            )
                            SideEffect { focusRequester.requestFocus() }
                        } else {
                           if (!isDualPane) {
                               Text("管理 IP 黑名单")
                           } else {
                               Text("Peer 过滤和屏蔽设置")
                           }
                        }
                    } else {
                        Text("Peer 过滤和屏蔽设置")
                    }
                },
                navigationIcon = { TopAppBarGoBackButton() },
                actions = {
                    if (inIpBlockListPane && !state.searchingBlockedIp) {
                        IconButton({ state.startSearchBlockedIp() }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索黑名单 IP 地址")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // TODO: use util after #910 is merged.
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val paddingHorizontal = 
            if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 16.dp else 24.dp
        val paddingVertical = 
            if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 16.dp else 24.dp
        
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane1(modifier = Modifier.preferredWidth(480.dp)) { 
                    PeerFilterEditPane(
                        state = state,
                        contentPadding = paddingValues,
                        showIpBlockingItem = !isDualPane,
                        onClickIpBlockSettings = { navigator.navigateTo(ThreePaneScaffoldRole.Primary) },
                        modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical)
                    )
                }
            },
            detailPane = {
                val filteredList by state.searchedIpBlockList.collectAsStateWithLifecycle(emptyList())
                AnimatedPane1 { 
                    BlockListEditPane(
                        blockedIpList = filteredList,
                        newBlockedIpValue = state.newBlockedIpValue,
                        contentPadding = paddingValues,
                        showTitle = isDualPane,
                        onAdd = { state.addBlockedIp(it) },
                        onRemove = { state.removeBlockedIp(it) },
                        modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical)
                    )
                }
            }
        )
    }
}