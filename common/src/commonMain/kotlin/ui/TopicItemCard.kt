/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.api.model.DATE_FORMAT
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.app.AppTheme

@Immutable
data class Tag(
    @Stable
    val text: AnnotatedString,
    @Stable
    val backgroundColor: Color = Color.Unspecified,
) {
    constructor(text: String, backgroundColor: Color = Color.Unspecified) : this(AnnotatedString(text), backgroundColor)
}

@Composable
fun TopicItemCard(topic: Topic, onClick: () -> Unit) {
    val topicState by rememberUpdatedState(topic)
    Box(Modifier.fillMaxHeight()) {
        val shape = AppTheme.shapes.large
        OutlinedCard(
            Modifier
                .shadow(elevation = 2.dp, shape = shape)
                .clip(shape)
                .clickable(
                    remember { MutableInteractionSource() },
                    rememberRipple(color = AppTheme.colorScheme.surfaceTint),
                ) { onClick() }
//                .border(1.dp, AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
                .wrapContentSize(),
            shape = shape,
        ) {
            Box(Modifier.padding(all = 16.dp)) {
                Row {
                    val details = remember(topic.id) { topic.details }
                    Column {
                        // titles
                        AnimatedTitles(
                            chineseTitle = details?.chineseTitle,
                            otherTitles = details?.otherTitles,
                            episode = details?.episode,
                            rawTitle = { topicState.rawTitle }
                        )

                        val tags = details?.tags
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                            val list = remember(topic.id) {
                                buildList {
                                    add(Tag(topic.size.toString()))

                                    details?.resolution?.id?.let { Tag(it) }?.let { add(it) }

                                    details?.mediaOrigin?.id?.let { Tag(it) }?.let { add(it) }

                                    details?.subtitleLanguages?.forEach {
                                        add(Tag(it.id))
                                    }

                                    tags?.forEach { add(Tag(it)) }
                                }
                            }

                            if (list.isNotEmpty()) {
                                TagsView(list)
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                topic.alliance?.let { alliance ->
                                    Text(
                                        alliance.name,
                                        style = AppTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.W600,
                                        maxLines = 1,
                                    )
                                }
                                Text(
                                    topic.author.name,
                                    Modifier.padding(start = 8.dp),
                                    style = AppTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.W400,
                                    maxLines = 1,
                                )
                            }


                            val dateFormatted by remember { derivedStateOf { topicState.date.format(DATE_FORMAT) } }
                            Text(
                                dateFormatted,
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.typography.bodyMedium.color.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 4.dp),
                                fontWeight = FontWeight.W400,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

    }
}

// Show titles in separate a line on Android to optimize performance
@Stable
val LocalAlwaysShowTitlesInSeparateLine: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf {
    false
}

@Composable
fun ColumnScope.AnimatedTitles(
    chineseTitle: String?,
    otherTitles: List<String>?,
    episode: Episode?,
    rawTitle: @Composable () -> String,
    rowModifier: Modifier = Modifier,
    topEnd: (@Composable () -> Unit)? = null,
) {
    var titleTooLong by remember { mutableStateOf(false) }
    val currentOtherTitles by rememberUpdatedState(otherTitles)
    val currentChineseTitle by rememberUpdatedState(chineseTitle)
    val preferredMainTitle by remember {
        derivedStateOf {
            if (currentChineseTitle != null) {
                currentChineseTitle
            } else if (currentOtherTitles != null) {
                currentOtherTitles?.firstOrNull()
            } else {
                null
            }
        }
    }
    val preferredSecondaryTitle by remember {
        derivedStateOf {
            if (currentChineseTitle != null) {
                currentOtherTitles?.joinToString(" / ")?.takeIf { it.isNotBlank() }
            } else if (currentOtherTitles != null) {
                currentOtherTitles?.asSequence()?.drop(1)?.joinToString(" / ")?.takeIf { it.isNotBlank() }
            } else {
                null
            }
        }
    }
    Row(Modifier.fillMaxWidth()) {
        Row(rowModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Chinese title overflows after only if other title overflowed.
            Row(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    preferredMainTitle ?: rawTitle(),
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )

                episode?.let { episode ->
                    Text(
                        episode.raw,
                        Modifier.padding(start = 8.dp).requiredWidth(IntrinsicSize.Max), // always show episode
                        style = AppTheme.typography.titleMedium,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            // other language title
            if (preferredMainTitle != null) {
                preferredSecondaryTitle?.let { text ->
                    // gradually hide this title if titles are too long
                    if (!LocalAlwaysShowTitlesInSeparateLine.current) {
                        val alpha by animateFloatAsState(if (titleTooLong) 0f else 1f)

                        Subtitle(
                            text,
                            onOverflowChange = {
                                titleTooLong = it
                            },
                            Modifier
                                .padding(start = 12.dp)
                                .alpha(alpha)
                        )
                    }
                }
            }
        }

        Row(Modifier.requiredWidth(IntrinsicSize.Max)) {
            topEnd?.invoke()
        }
    }

    // show other language's title in separate line if titles are too long
    if (preferredMainTitle != null) {
        preferredSecondaryTitle?.let { text ->
            if (LocalAlwaysShowTitlesInSeparateLine.current) {
                Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                    Subtitle(text, onOverflowChange = {})
                }
            } else {
                AnimatedVisibility(titleTooLong) {
                    Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                        Subtitle(text, onOverflowChange = {})
                    }
                }
            }
        }
    }
}

@Composable
private fun Subtitle(text: String, onOverflowChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    val onOverflowChangeState by rememberUpdatedState(onOverflowChange)
    Text(
        text,
        modifier,
        style = AppTheme.typography.titleMedium.run { copy(color = color.copy(alpha = 0.5f)) },
        fontWeight = FontWeight.W400,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = {
            onOverflowChangeState?.invoke(it.hasVisualOverflow)
        }
    )
}

@Composable
private fun TagsView(tags: List<Tag>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags, { it.text.text }) {
            TagButton({ Text(it.text) }, null, it.backgroundColor)
        }
    }
}

@Composable
private fun TagButton(
    text: @Composable () -> Unit,
    onClick: (() -> Unit)?,
    containerColorEffect: Color,
    modifier: Modifier = Modifier
) {
    val onClickState by rememberUpdatedState(onClick)
    val elevation = ButtonDefaults.buttonElevation()
    val interactionSource = remember { MutableInteractionSource() }
    val shadowElevation by elevation.shadowElevation(true, interactionSource)
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    val defaultContainerColor = AppTheme.colorScheme.surfaceColorAtElevation(shadowElevation)
    val containerColor = if (containerColorEffect == Color.Unspecified) {
        defaultContainerColor
    } else {
        containerColorEffect.compositeOver(defaultContainerColor.copy(alpha = 0.5f))
    }
    val shape = AppTheme.shapes.small
    ElevatedButton(
        onClick = { onClickState?.invoke() },
        enabled = onClick != null,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.shadow(1.dp, shape = shape).height(24.dp).wrapContentWidth(),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor,
            disabledContainerColor = containerColor
        ),
        elevation = elevation,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, if (containerColorEffect == Color.Unspecified) Color.Gray else containerColor),
    ) {
        ProvideTextStyle(
            AppTheme.typography.bodySmall.copy(
                color = containerColor.contrastTextColor(),
                lineHeight = 16.sp,
            )
        ) {
            text()
        }
    }
}

@Stable
fun Color.contrastTextColor(): Color =
    if (luminance() > 0.5) Color.Black else Color.White


@Composable
@Preview
private fun PreviewTags() {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        TagButton({ Text("HEVC-10bit") }, null, containerColorEffect = Color.Unspecified)
        //     TagsView(listOf("HEVC-10bit", "AAC"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (s in listOf("HEVC-10bit", "AAC")) {
                TagButton({ Text(s) }, null, containerColorEffect = Color.Unspecified)
            }
        }
    }

}