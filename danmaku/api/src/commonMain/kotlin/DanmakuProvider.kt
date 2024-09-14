package me.him188.ani.danmaku.api

import io.ktor.client.HttpClientConfig
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.logging.logger
import kotlin.jvm.JvmStatic
import kotlin.time.Duration

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 *
 * @see DanmakuProviderFactory
 */
interface DanmakuProvider : AutoCloseable {
    val id: String

    /**
     * Matches a danmaku stream by the given filtering parameters.
     *
     * Returns `null` if not found.
     *
     * The returned [DanmakuCollection] should be closed when it is no longer needed.
     */
    suspend fun fetch(
        request: DanmakuSearchRequest,
    ): DanmakuFetchResult
}

class DanmakuSearchRequest(
    val subjectId: Int,
    val subjectPrimaryName: String,
    val subjectNames: List<String>,
    /**
     * Cane be [PackedDate.Invalid]
     */
    val subjectPublishDate: PackedDate,
    val episodeId: Int,
    val episodeSort: EpisodeSort,
    val episodeEp: EpisodeSort?,
    val episodeName: String,

    val filename: String?,
    val fileHash: String?,
    val fileSize: Long?,
    val videoDuration: Duration,
)

class DanmakuFetchResult(
    val matchInfo: DanmakuMatchInfo,
    val list: Sequence<Danmaku>,
) {
    companion object {
        @JvmStatic
        fun noMatch(providerId: String) = DanmakuFetchResult(
            matchInfo = DanmakuMatchInfo(
                providerId = providerId,
                count = 0,
                method = DanmakuMatchMethod.NoMatch,
            ),
            list = emptySequence(),
        )
    }
}

class DanmakuMatchInfo(
    /**
     * @see DanmakuProvider.id
     */
    val providerId: String,
    val count: Int,
    val method: DanmakuMatchMethod,
)

sealed class DanmakuMatchMethod {
    data class Exact(
        val subjectTitle: String,
        val episodeTitle: String,
    ) : DanmakuMatchMethod()

    data class ExactSubjectFuzzyEpisode(
        val subjectTitle: String,
        val episodeTitle: String,
    ) : DanmakuMatchMethod()

    data class Fuzzy(
        val subjectTitle: String,
        val episodeTitle: String,
    ) : DanmakuMatchMethod()

    data class ExactId(
        val subjectId: Int,
        val episodeId: Int,
    ) : DanmakuMatchMethod()

    data object NoMatch : DanmakuMatchMethod()
}

fun interface DanmakuMatcher {
    fun match(list: List<DanmakuEpisode>): DanmakuEpisode?
}

@Serializable
data class DanmakuEpisode(
    val id: String,
    val subjectName: String,
    val episodeName: String,
    /**
     * 可能是系列内的, 也可能是单季的
     */
    val epOrSort: EpisodeSort? = null,
)

object DanmakuMatchers {
    fun first() = DanmakuMatcher { it.firstOrNull() }

    fun mostRelevant(targetSubjectName: String, targetEpisodeName: String): DanmakuMatcher = DanmakuMatcher { list ->
        list.minByOrNull {
            levenshteinDistance(it.subjectName, targetSubjectName) + levenshteinDistance(
                it.episodeName,
                targetEpisodeName,
            )
        }
    }

    // Thanks to ChatGPT :)
    // Helper function to calculate the Levenshtein distance between two strings
    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = minOf(costReplace, costInsert, costDelete)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }
}


abstract class AbstractDanmakuProvider(
    config: DanmakuProviderConfig,
) : DanmakuProvider {
    protected val logger = logger(this::class)

    protected val client = createDefaultHttpClient {
        applyDanmakuProviderConfig(config)
        configureClient()
    }.apply {
        registerLogging(logger)
    }

    protected open fun HttpClientConfig<*>.configureClient() {}

    override fun close() {
        client.close()
    }
}
