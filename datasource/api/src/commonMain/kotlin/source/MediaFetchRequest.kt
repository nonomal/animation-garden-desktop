package me.him188.ani.datasources.api.source

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaCacheMetadata

/**
 * 一个数据源查询请求. 该请求包含尽可能多的信息以便 [MediaSource] 可以查到尽可能多的结果.
 *
 * @see MediaSource.fetch
 */
@Serializable
class MediaFetchRequest(
    // 提示, 查看 [MediaFetcher]
    /**
     * 条目服务 (Bangumi) 提供的条目 ID. 若数据源支持, 可以用此信息做精确匹配.
     * 可能为 `null`, 表示未知.
     */
    val subjectId: String,
    /**
     * 条目服务 (Bangumi) 提供的剧集 ID. 若数据源支持, 可以用此信息做精确匹配.
     * 可能为 `null`, 表示未知.
     */
    val episodeId: String,
    /**
     * 条目的主简体中文名称.
     * 建议使用 [subjectNames] 用所有已知名称去匹配.
     */
    val subjectNameCN: String? = null,
    /**
     * 已知的该条目的所有名称. 包含季度信息.
     *
     * 所有名称包括简体中文译名, 各种别名, 简称, 以及日文原名.
     *
     * E.g. "关于我转生变成史莱姆这档事 第三季"
     */
    val subjectNames: Set<String>,
    /**
     * 在系列中的集数, 例如第二季的第一集为 26.
     *
     * E.g. "49", "01".
     *
     * @see EpisodeSort
     */
    val episodeSort: EpisodeSort,
    /**
     * 条目服务 (Bangumi) 提供的剧集名称, 例如 "恶魔与阴谋", 不会包含 "第 x 集".
     * 不一定为简体中文, 可能为日文. 也可能为空字符串.
     */
    val episodeName: String,
    /**
     * 在当前季度中的集数, 例如第二季的第一集为 01
     *
     * E.g. "49", "01".
     *
     * @see EpisodeSort
     */
    val episodeEp: EpisodeSort? = episodeSort,
) {
    companion object
}

fun MediaFetchRequest.toStringMultiline() = buildString {
    append("subjectId").append(": ").append(subjectId).appendLine()
    append("episodeId").append(": ").append(episodeId).appendLine()
    append("subjectNameCn").append(": ").append(subjectNameCN).appendLine()
    append("subjectNames:").appendLine()
    subjectNames.forEach { append("- ").appendLine(it) }
    append("episodeSort").append(": ").append(episodeSort).appendLine()
    append("episodeName").append(": ").append(episodeName).appendLine()
    append("episodeEp").append(": ").append(episodeEp).appendLine()
}

/**
 * 尝试匹配
 */
infix fun MediaFetchRequest.matches(cache: MediaCacheMetadata): MatchKind? {
    if (episodeId != "" && cache.episodeId != "") {
        // Both query and cache have episodeId, perform exact match.
        if (cache.episodeId == episodeId) {
            return MatchKind.EXACT
        }

        // Don't go for fuzzy match otherwise we'll always get false positives.
        return null
    }

    // Exact match is not possible, do a fuzzy match.

    // Success if the episode name exactly matches
    if (episodeName.isNotEmpty() && cache.episodeName == episodeName) return MatchKind.FUZZY

    if (subjectNames.any { cache.subjectNames.contains(it) }) {
        // Any subject name matches

        return if (episodeSort == cache.episodeSort || episodeEp == cache.episodeSort) {
            // Episode sort matches
            MatchKind.FUZZY
        } else {
            null
        }
    }

    return null
}
