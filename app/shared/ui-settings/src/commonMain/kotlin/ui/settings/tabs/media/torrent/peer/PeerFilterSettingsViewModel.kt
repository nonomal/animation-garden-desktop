/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.settings.mediasource.rss.SaveableStorage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class PeerFilterSettingsViewModel : AbstractViewModel(), KoinComponent {
    val settingsRepository: SettingsRepository by inject()
    
    private val peerFilterConfig = settingsRepository.torrentPeerConfig.flow
    private val updateTasker = MonoTasker(backgroundScope)
    private val localConfig: MutableState<TorrentPeerConfig?> = mutableStateOf(null)
    
    val state = PeerFilterSettingsState(
        storage = SaveableStorage(
            localConfig,
            onSave = { update(it) },
            isSavingState = derivedStateOf { updateTasker.isRunning }
        )
    )
    
    init {
        launchInBackground { 
            peerFilterConfig.distinctUntilChanged().collectLatest { config ->
                withContext(Dispatchers.Main) {
                    localConfig.value = config
                }
            }
        }
    }
    
    private fun update(new: TorrentPeerConfig) {
        updateTasker.launch {
            localConfig.value = new
            delay(500)
            settingsRepository.torrentPeerConfig.update { new }
        }
    }
}