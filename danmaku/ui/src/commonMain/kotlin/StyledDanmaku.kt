package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.platform.Uuid
import me.him188.ani.utils.platform.format2f
import kotlin.math.floor

/**
 * DanmakuState holds all params which [Canvas] needs to draw a danmaku text.
 */
@Immutable
data class StyledDanmaku(
    val presentation: DanmakuPresentation,
    internal val measurer: TextMeasurer,
    internal val baseStyle: TextStyle,
    internal val style: DanmakuStyle,
    internal val enableColor: Boolean,
    internal val isDebug: Boolean
) : SizeSpecifiedDanmaku {
    private val danmakuText = presentation.danmaku.run {
        val seconds = playTimeMillis.toFloat().div(1000)
        if (isDebug) "$text (${floor((seconds / 60)).toInt()}:${String.format2f(seconds % 60)})" else text
    }
    
    private val solidTextLayout = measurer.measure(
        text = danmakuText,
        style = baseStyle.merge(
            style.styleForText(
                color = if (enableColor) {
                    Color(0xFF_00_00_00L or presentation.danmaku.color.toUInt().toLong())
                } else Color.White,
            ).copy(textDecoration = if (presentation.isSelf) TextDecoration.Underline else null),
        ),
        overflow = TextOverflow.Clip,
        maxLines = 1,
        softWrap = false,
    )
    
    private val borderTextLayout = measurer.measure(
        text = danmakuText,
        style = baseStyle.merge(style.styleForBorder())
            .copy(textDecoration = if (presentation.isSelf) TextDecoration.Underline else null),
        overflow = TextOverflow.Clip,
        maxLines = 1,
        softWrap = false,
    )
    
    internal var imageBitmap: ImageBitmap? = null
    
    override val danmakuWidth: Int = solidTextLayout.size.width
    override val danmakuHeight: Int = solidTextLayout.size.height
    
    internal fun DrawScope.draw(screenPosX: Float, screenPosY: Float) {
        val cachedImage = imageBitmap ?: createDanmakuImageBitmap(solidTextLayout, borderTextLayout)
            .also { imageBitmap = it }
        
        drawImage(cachedImage, Offset(screenPosX, screenPosY))
    }
}

/**
 * Create image snapshot of danmaku text.
 */
internal expect fun createDanmakuImageBitmap(
    solidTextLayout: TextLayoutResult,
    borderTextLayout: TextLayoutResult,
): ImageBitmap

internal fun dummyDanmaku(
    measurer: TextMeasurer,
    baseStyle: TextStyle,
    style: DanmakuStyle,
    dummyText: String = "dummy 占位 攟 の \uD83D\uDE04"
): StyledDanmaku {
    return StyledDanmaku(
        presentation = DanmakuPresentation(
            Danmaku(
                Uuid.randomString(),
                "dummy",
                0L, "1",
                DanmakuLocation.NORMAL, dummyText, 0,
            ),
            isSelf = false
        ),
        measurer = measurer,
        baseStyle = baseStyle,
        style = style,
        enableColor = false,
        isDebug = false
    )
}