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

package me.him188.ani.datasources.api.paging

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * A [SizedSource] that adds pagination support.
 * @see SizedSource
 */
interface PagedSource<out T> : SizedSource<T> {
    /**
     * 全部搜索结果, 以 [Flow] 形式提供, 惰性请求.
     */
    override val results: Flow<T>

    override val finished: StateFlow<Boolean>

    val currentPage: StateFlow<Int>

    override val totalSize: StateFlow<Int?>

    /**
     * 主动查询下一页. 当已经没有下一页时返回 `null`. 注意, 若有使用 [results], 主动操作 [nextPageOrNull] 将导致 [results] 会跳过该页.
     *
     * ### Coroutine Cancellation
     *
     * This function supports coroutine cancellation, and will **always** check for cancellation.
     *
     * When this coroutine is cancelled, [currentPage] is guaranteed to be left intact, i.e. not incremented.
     */
    suspend fun nextPageOrNull(): List<T>?

    fun skipToPage(page: Int)

    /**
     * Update the page counter to the previous page if there is one.
     * Do nothing if there isn't.
     */
    fun backToPrevious()
}

inline fun <T, R> PagedSource<T>.map(crossinline transform: suspend (T) -> R): PagedSource<R> {
    return convert(
        flowOperator = { flow ->
            flow.map { transform(it) }
        },
        listOperator = { list ->
            list.map { transform(it) }
        },
    )
}

inline fun <T, R> PagedSource<T>.mapNotNull(crossinline transform: suspend (T) -> R?): PagedSource<R & Any> {
    return convert(
        flowOperator = { flow ->
            flow.mapNotNull { transform(it) }
        },
        listOperator = { list ->
            list.mapNotNull { transform(it) }
        },
    )
}

inline fun <T, R> PagedSource<T>.flatMapConcat(crossinline transform: suspend (T) -> List<R>): PagedSource<R> {
    return convert(
        flowOperator = { flow ->
            flow.flatMapConcat { transform(it).asFlow() }
        },
        listOperator = { list ->
            list.flatMap { transform(it) }
        },
    )
}

@PublishedApi
internal inline fun <T, R> PagedSource<T>.convert(
    crossinline flowOperator: (Flow<T>) -> Flow<R>,
    crossinline listOperator: suspend (List<T>) -> List<R>,
): PagedSource<R> {
    val self = this
    return object : PagedSource<R> {
        override val results: Flow<R> by lazy {
            flowOperator(self.results)
        }
        override val finished: StateFlow<Boolean> get() = self.finished
        override val currentPage: StateFlow<Int> get() = self.currentPage
        override val totalSize: StateFlow<Int?> get() = self.totalSize

        override suspend fun nextPageOrNull(): List<R>? {
            val nextPageOrNull = self.nextPageOrNull()
            return try {
                nextPageOrNull?.let {
                    listOperator(it)
                }
            } catch (e: CancellationException) {
                self.backToPrevious() // reset page index
                throw e
            }
        }

        override fun skipToPage(page: Int) {
            self.skipToPage(page)
        }

        override fun backToPrevious() {
            self.backToPrevious()
        }
    }
}
