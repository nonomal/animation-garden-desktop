/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.OutlinedTag
import me.him188.ani.app.ui.foundation.layout.isWidthAtLeastMedium
import me.him188.ani.app.ui.foundation.layout.paddingIfNotEmpty
import me.him188.ani.app.ui.subject.collection.components.AiringLabel
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.datasources.api.seasonMonth

const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

// 图片和标题
@Composable
internal fun SubjectDetailsHeader(
    info: SubjectInfo,
    coverImageUrl: String?,
    airingLabelState: AiringLabelState,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable BoxScope.() -> Unit,
    rating: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (currentWindowAdaptiveInfo().isWidthAtLeastMedium) {
        SubjectDetailsHeaderWide(
            coverImageUrl = coverImageUrl,
            title = {
                Text(info.displayName)
            },
            subtitle = {
                Text(info.name)
            },
            seasonTags = {
                OutlinedTag { Text(renderSubjectSeason(info.airDate)) }
                AiringLabel(
                    airingLabelState,
                    Modifier.align(Alignment.CenterVertically),
                    style = LocalTextStyle.current,
                    progressColor = LocalContentColor.current,
                )
            },
            collectionData = collectionData,
            collectionAction = collectionAction,
            selectEpisodeButton = selectEpisodeButton,
            rating = rating,
            modifier = modifier,
        )
    } else {
        SubjectDetailsHeaderCompact(
            coverImageUrl = coverImageUrl,
            title = {
                Text(info.displayName)
            },
            subtitle = {
                Text(info.name)
            },
            seasonTags = {
                OutlinedTag { Text(renderSubjectSeason(info.airDate)) }
                AiringLabel(
                    airingLabelState,
                    Modifier.align(Alignment.CenterVertically),
                    style = LocalTextStyle.current,
                    progressColor = LocalContentColor.current,
                )
            },
            collectionData = collectionData,
            collectionAction = collectionAction,
            selectEpisodeButton = selectEpisodeButton,
            rating = rating,
            modifier = modifier,
        )
    }
}


// 适合手机, 窄
@Composable
fun SubjectDetailsHeaderCompact(
    coverImageUrl: String?,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    seasonTags: @Composable RowScope.() -> Unit,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable BoxScope.() -> Unit,
    rating: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
            val imageWidth = 140.dp

            Box(Modifier.clip(MaterialTheme.shapes.medium)) {
                AsyncImage(
                    coverImageUrl,
                    null,
                    Modifier
                        .width(imageWidth)
                        .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO),
                    contentScale = ContentScale.Crop,
                    placeholder = if (currentAniBuildConfig.isDebug) remember { ColorPainter(Color.Gray) } else null,
                )
            }

            Column(
                Modifier.weight(1f, fill = true)
                    .padding(horizontal = 12.dp),
            ) {
                Column(
                    Modifier.fillMaxWidth(), // required by Rating
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    var showSubtitle by remember { mutableStateOf(false) }
                    SelectionContainer(Modifier.clickable { showSubtitle = !showSubtitle }) {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                            if (showSubtitle) {
                                subtitle()
                            } else {
                                title()
                            }
                        }
                    }

                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        ) {
                            seasonTags()
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        Modifier.requiredHeight(IntrinsicSize.Max).align(Alignment.End),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        rating()
                    }
                }
            }
        }

        Row(
            Modifier.padding(top = 16.dp).align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
            ) {
                collectionData()
            }
            collectionAction()
        }

        Box(Modifier.paddingIfNotEmpty(top = 8.dp), contentAlignment = Alignment.CenterEnd) {
            selectEpisodeButton()
        }
    }
}

@Composable
fun SubjectDetailsHeaderWide(
    coverImageUrl: String?,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    seasonTags: @Composable RowScope.() -> Unit,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable BoxScope.() -> Unit,
    rating: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top,
        ) {
            val imageWidth = 220.dp

            Box(Modifier.clip(MaterialTheme.shapes.medium)) {
                AsyncImage(
                    coverImageUrl,
                    null,
                    Modifier
                        .width(imageWidth)
                        .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO),
                    contentScale = ContentScale.Crop,
                    placeholder = if (currentAniBuildConfig.isDebug) remember { ColorPainter(Color.Gray) } else null,
                )
            }

            Column(
                Modifier
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {

                    var showSubtitle by remember { mutableStateOf(false) }
                    SelectionContainer(Modifier.clickable { showSubtitle = !showSubtitle }) {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                            if (showSubtitle) {
                                subtitle()
                            } else {
                                title()
                            }
                        }
                    }
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                        ) {
                            seasonTags()
                        }
                    }
                    Spacer(Modifier.weight(1f)) // spacedBy applies
                    Row(Modifier) {
                        rating()
                    }
                }
                Row(Modifier.align(Alignment.Start)) {
                    collectionData()
                }
                Row(
                    Modifier.align(Alignment.Start),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    collectionAction()
                }
                Row(
                    Modifier.align(Alignment.Start),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(IntrinsicSize.Min)) {
                        selectEpisodeButton()
                    }
                }
            }
        }
    }
}

@Stable
val SubjectCollection.date get() = renderSubjectSeason(info.airDate)

@Stable
fun renderSubjectSeason(date: PackedDate): String {
    if (date == PackedDate.Invalid) return "TBA"
    if (date.seasonMonth == 0) {
        return date.toString()
    }
    return "${date.year} 年 ${date.seasonMonth} 月"
}
