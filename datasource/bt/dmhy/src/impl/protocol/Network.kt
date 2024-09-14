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

package me.him188.ani.datasources.dmhy.impl.protocol

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import me.him188.ani.datasources.api.source.bodyAsDocument
import me.him188.ani.datasources.dmhy.DmhyTopic
import me.him188.ani.datasources.dmhy.impl.cache.Cache
import me.him188.ani.datasources.dmhy.impl.cache.CacheImpl

class Network(
    private val client: HttpClient,
) {
    private object Paths {
        const val host: String = "www.dmhy.org"
        val userPathSegments: List<String> =
            listOf("topics", "list", "user_id")  // https://www.dmhy.org/topics/list/user_id/637871
        val alliancePathSegments: List<String> =
            listOf("topics", "list", "team_id") // https://www.dmhy.org/topics/list/team_id/801
    }

    data class ListResponse(
        val context: Cache,
        val list: List<DmhyTopic>,
        val currentPage: Int,
        val hasPreviousPage: Boolean,
        val hasNextPage: Boolean,
    )

    // https://www.dmhy.org/topics/list?keyword=lyc&sort_id=2&team_id=823&order=date-asc
    // page starts from 1
    suspend fun list(
        page: Int? = null,
        keyword: String? = null,
        sortId: String? = null,
        teamId: String? = null,
        orderId: String? = null,
    ): ListResponse {
        require(page == null || page >= 1) { "page must be >= 1" }
        val resp = client.get {
            url {
                protocol = URLProtocol.HTTP
                host = Paths.host
                appendPathSegments("topics", "list")
                if (page != null && page != 1) {
                    appendPathSegments("page", page.toString())
                }
            }
            parameter("keyword", keyword)
            parameter("sort_id", sortId)
            parameter("team_id", teamId)
            parameter("order", orderId)
        }
        val document = resp.bodyAsDocument()
        val context = CacheImpl()
        return ListResponse(
            context = context,
            list = ListParser.parseList(context, document).orEmpty(),
            currentPage = 0,
            hasPreviousPage = false,
            hasNextPage = false,
        )
    }
}
