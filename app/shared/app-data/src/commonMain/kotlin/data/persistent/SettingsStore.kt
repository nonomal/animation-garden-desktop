/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.data.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.serialization.builtins.ListSerializer
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.data.repository.EpisodeHistories
import me.him188.ani.app.data.repository.MediaSourceSaves
import me.him188.ani.app.data.repository.MikanIndexes
import me.him188.ani.app.platform.ReplaceFileCorruptionHandler
import me.him188.ani.app.platform.asDataStoreSerializer
import me.him188.ani.app.platform.create
import me.him188.ani.utils.io.SystemPath

// 一个对象, 可都写到 common 里, 不用每个 store 都 expect/actual
abstract class PlatformDataStoreManager {
    val mikanIndexStore: DataStore<MikanIndexes>
        get() = DataStoreFactory.create(
            serializer = MikanIndexes.serializer().asDataStoreSerializer({ MikanIndexes.Empty }),
            produceFile = { resolveDataStoreFile("mikanIndexes") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                MikanIndexes.Empty
            },
        )

    val mediaSourceSaveStore by lazy {
        DataStoreFactory.create(
            serializer = MediaSourceSaves.serializer()
                .asDataStoreSerializer({ MediaSourceSaves.Default }),
            produceFile = { resolveDataStoreFile("mediaSourceSaves") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                MediaSourceSaves.Default
            },
        )
    }

    val episodeHistoryStore by lazy {
        DataStoreFactory.create(
            serializer = EpisodeHistories.serializer()
                .asDataStoreSerializer({ EpisodeHistories.Empty }),
            produceFile = { resolveDataStoreFile("episodeHistories") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                EpisodeHistories.Empty
            },
        )
    }

    // creata a datastore<List<DanmakuFilter>>
    val danmakuFilterStore by lazy {
        DataStoreFactory.create(
            serializer = ListSerializer(DanmakuRegexFilter.serializer())
                .asDataStoreSerializer({ emptyList() }),
            produceFile = { resolveDataStoreFile("danmakuFilter") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                emptyList()
            },
        )
    }

    abstract val tokenStore: DataStore<Preferences>
    abstract val preferencesStore: DataStore<Preferences>
    abstract val preferredAllianceStore: DataStore<Preferences>

    abstract fun resolveDataStoreFile(name: String): SystemPath
}

