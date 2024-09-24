/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.web.format

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.source.media.source.web.WebSearchSubjectInfo
import me.him188.ani.utils.xml.Element
import me.him188.ani.utils.xml.QueryParser
import me.him188.ani.utils.xml.parseSelectorOrNull
import org.intellij.lang.annotations.Language
import kotlin.contracts.contract

/**
 * 决定如何匹配条目
 */
sealed class SelectorSubjectFormat<in Config : SelectorFormatConfig>(override val id: SelectorFormatId) :
    SelectorFormat { // 方便改名

    /**
     * `null` means invalid config
     */
    abstract fun select(
        document: Element,
        baseUrl: String,
        config: Config,
    ): List<WebSearchSubjectInfo>?

    companion object {
        val entries by lazy { // 必须 lazy, 否则可能获取到 null
            listOf(
                checkNotNull(SelectorSubjectFormatA),
                checkNotNull(SelectorSubjectFormatIndexed),
            ) // checkNotNull is needed to be fail-fast
        }

        fun findById(id: SelectorFormatId): SelectorSubjectFormat<*>? {
            // reflection is not supported in Kotlin/Native
            return entries.find { it.id == id }
        }
    }
}

/**
 * Select 出一些 `<a>`, text 作为 name, `href` 作为 url
 */
data object SelectorSubjectFormatA : SelectorSubjectFormat<SelectorSubjectFormatA.Config>(SelectorFormatId("a")) {
    @Immutable
    @Serializable
    data class Config(
        @Language("css")
        val selectLists: String = "div.video-info-header > a",
        val preferShorterName: Boolean = true,
    ) : SelectorFormatConfig {
        override fun isValid(): Boolean {
            return selectLists.isNotBlank()
        }
    }

    override fun select(
        document: Element,
        baseUrl: String,
        config: Config,
    ): List<WebSearchSubjectInfo>? {
        val selectLists = QueryParser.parseSelectorOrNull(config.selectLists) ?: return null
        val elements = document.select(selectLists)
        return elements.mapTo(ArrayList(elements.size)) { a ->
            val name = a.attr("title").takeIf { it.isNotBlank() } ?: a.text()
            val href = a.attr("href")
            val id = href.substringBeforeLast(".html").substringAfterLast("/")
            WebSearchSubjectInfo(
                internalId = id,
                name = name,
                fullUrl = SelectorHelpers.computeAbsoluteUrl(baseUrl, href),
                partialUrl = href,
                origin = a,
            )
        }.apply {
            if (config.preferShorterName) {
                sortBy { info ->
                    info.name.length
                }
            }
        }
    }
}


/**
 * 一个语句 select 出所有的名字, 然后一个语句 select 所有的按钮 `<a>`, 按顺序对应
 */
data object SelectorSubjectFormatIndexed :
    SelectorSubjectFormat<SelectorSubjectFormatIndexed.Config>(SelectorFormatId("indexed")) {
    @Immutable
    @Serializable
    data class Config(
        @Language("css")
        val selectNames: String = ".search-box .thumb-content > .thumb-txt",
        @Language("css")
        val selectLinks: String = ".search-box .thumb-menu > a",
        val preferShorterName: Boolean = true,
    ) : SelectorFormatConfig {
        override fun isValid(): Boolean {
            return selectNames.isNotBlank()
        }
    }

    override fun select(
        document: Element,
        baseUrl: String,
        config: Config,
    ): List<WebSearchSubjectInfo>? {
        val selectNames = QueryParser.parseSelectorOrNull(config.selectNames) ?: return null
        val selectLinks = QueryParser.parseSelectorOrNull(config.selectLinks) ?: return null


        val names: List<String> = document.select(selectNames).mapNotNull { a ->
            a.text().takeIf { it.isNotBlank() }
        }

        val links = document.select(selectLinks).mapNotNull { a ->
            val href = a.attr("href")
            href.takeIf { it.isNotBlank() }
        }

        return names.fastZipNotNullToMutable(links) { name, href ->
            val id = href.substringBeforeLast(".html").substringAfterLast("/")
            WebSearchSubjectInfo(
                internalId = id,
                name = name,
                fullUrl = SelectorHelpers.computeAbsoluteUrl(baseUrl, href),
                partialUrl = href,
                origin = null,
            )
        }.apply {
            if (config.preferShorterName) {
                sortBy { info ->
                    info.name.length
                }
            }
        }
    }
}

private inline fun <T, R, V : Any> List<T>.fastZipNotNullToMutable(
    other: List<R>,
    transform: (a: T, b: R) -> V?
): MutableList<V> {
    contract { callsInPlace(transform) }
    val minSize = minOf(size, other.size)
    val target = ArrayList<V>(minSize)
    for (i in 0 until minSize) {
        val res = transform(get(i), other[i])
        if (res != null) {
            target += res
        }
    }
    return target
}
