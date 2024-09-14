package me.him188.ani.app.ui.subject.collection.progress

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.indication.HorizontalIndicator
import me.him188.ani.app.ui.foundation.indication.IndicatedBox
import me.him188.ani.app.ui.subject.episode.list.cacheStatusIndicationColor


/**
 * 显示条目的当前观看进度或推荐观看下一集.
 *
 * 在我的追番中的每个卡片的右下角.
 */
@Composable
fun SubjectProgressButton(
    state: SubjectProgressState,
    modifier: Modifier = Modifier,
) {
    IndicatedBox(
        indicator = {
            state.episodeIdToPlay?.let { episode ->
                HorizontalIndicator(
                    6.dp,
                    CircleShape,
                    cacheStatusIndicationColor(
                        state.episodeCacheStatus(episode),
                        state.isLatestEpisodeWatched,
                    ),
                    Modifier.offset(y = (-2).dp),
                )
            }
        },
    ) {
        val requiredWidth = Modifier.requiredWidth(IntrinsicSize.Max)
        Crossfade(state.buttonIsPrimary) { isPrimary ->
            if (isPrimary) {
                Button(onClick = state::onClickButton, modifier) {
                    Text(state.buttonText, requiredWidth, softWrap = false)
                }
            } else {
                FilledTonalButton(onClick = state::onClickButton, modifier) {
                    Text(state.buttonText, requiredWidth, softWrap = false)
                }
            }
        }
    }
}
