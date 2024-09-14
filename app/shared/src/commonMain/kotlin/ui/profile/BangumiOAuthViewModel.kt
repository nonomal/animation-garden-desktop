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

package me.him188.ani.app.ui.profile

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.source.AniAuthClient
import me.him188.ani.app.data.source.session.ExternalOAuthRequest
import me.him188.ani.app.data.source.session.OAuthResult
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.SessionStatus
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

@Stable
class BangumiOAuthViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val client: AniAuthClient by inject()

    /**
     * 需要进行授权
     */
    val needAuth by sessionManager.state
        .map { it !is SessionStatus.Verified }
        .produceState(true)

    private var requestIdFlow = MutableStateFlow(Uuid.randomString())

    /**
     * 当前是第几次尝试
     */
    val requestId by requestIdFlow.produceState()

    fun makeOAuthUrl(
        requestId: String,
    ): String {
        val base = currentAniBuildConfig.aniAuthServerUrl.removeSuffix("/")
        return "${base}/v1/login/bangumi/oauth?requestId=${requestId.encodeURLParameter()}"
    }

    val oauthUrl by derivedStateOf {
        makeOAuthUrl(requestId)
    }

    /**
     * 展示登录失败的错误
     */
    val authError: MutableStateFlow<ErrorMessage?> = sessionManager.processingRequest.map { request ->
        (request?.state?.value as? ExternalOAuthRequest.State.Failed)?.throwable?.let {
            ErrorMessage.simple("登录失败, 请重试", it)
        }
    }.localCachedStateFlow(null)

    suspend fun doCheckResult() {
        withContext(backgroundScope.coroutineContext) {
            while (true) {
                val resp = client.getResult(requestIdFlow.value)
                logger.info { "Check OAuth result: $resp" }
                resp.fold(
                    onSuccess = { result ->
                        if (result == null) {
                            return@fold
                        }
                        val request = sessionManager.processingRequest.value
                        logger.info {
                            "Check OAuth result success, request is $request, " +
                                    "token expires in ${result.expiresIn.seconds}"
                        }
                        request?.onCallback(
                            Result.success(
                                OAuthResult(
                                    accessToken = result.accessToken,
                                    refreshToken = result.refreshToken,
                                    expiresIn = result.expiresIn.seconds,
                                ),
                            ),
                        )
                        return@withContext
                    },
                    onKnownFailure = {
                        logger.info { "Check OAuth result failed: $it" }
                    },
                )

                delay(1000)
            }
        }
    }

    @UiThread
    fun dismissError() {
        logger.debug { "dismissError" }
        authError.value = null
        refresh()
    }

    @UiThread
    fun refresh() {
        logger.debug { "refresh" }
        requestIdFlow.value = Uuid.randomString()
    }

    fun onCancel(reason: String?) {
        sessionManager.processingRequest.value?.cancel(reason)
    }
}
