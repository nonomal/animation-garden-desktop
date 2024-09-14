package me.him188.ani.datasources.ikaros

import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.HttpMediaSource
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.get
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.datasources.api.source.parameter.MediaSourceParametersBuilder
import me.him188.ani.datasources.api.source.useHttpClient
import java.nio.charset.StandardCharsets
import java.util.Base64

class IkarosMediaSource(
    override val mediaSourceId: String,
    config: MediaSourceConfig
) : HttpMediaSource() {
    companion object {
        const val ID = "ikaros"
        val INFO = MediaSourceInfo(
            displayName = "Ikaros",
            description = "专注于 ACGMN 的内容管理系统 (CMS)",
            websiteUrl = "https://docs.ikaros.run",
            iconUrl = "https://docs.ikaros.run/img/favicon.ico",
        )
    }

    internal val client = IkarosClient(
        config[Parameters.baseUrl],
        useHttpClient(config) {
            defaultRequest {
                val username = config[Parameters.username]
                val password = config[Parameters.password]
                header(
                    HttpHeaders.Authorization,
                    "Basic " + Base64.getEncoder()
                        .encodeToString(
                            "$username:$password".toByteArray(
                                StandardCharsets.UTF_8,
                            ),
                        ),
                )
            }
        },
    )

    object Parameters : MediaSourceParametersBuilder() {
        val baseUrl = string("baseUrl", description = "API base URL")
        val username = string("username", description = "用户名")
        val password = string("password", description = "密码")
    }

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = FactoryId(ID)

        override val parameters: MediaSourceParameters = Parameters.build()
        override val allowMultipleInstances: Boolean get() = true
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource =
            IkarosMediaSource(mediaSourceId, config)
        override val info: MediaSourceInfo get() = INFO
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB
    override val info: MediaSourceInfo get() = INFO

    override suspend fun checkConnection(): ConnectionStatus {
        return if ((HttpStatusCode.OK == client.checkConnection())
        ) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        val subjectId = checkNotNull(query.subjectId)
        val episodeSort = checkNotNull(query.episodeSort)
        val ikarosSubjectDetails = checkNotNull(client.postSubjectSyncBgmTv(subjectId))
        return client.subjectDetails2SizedSource(ikarosSubjectDetails, episodeSort)
    }
}
