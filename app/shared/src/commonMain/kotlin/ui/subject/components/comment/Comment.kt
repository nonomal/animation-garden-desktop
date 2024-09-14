package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.layout.paddingIfNotEmpty
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.richtext.UIRichElement

/**
 * 评论项目
 *
 * @param avatar 用户头像
 * @param primaryTitle 主标题，一般是评论者用户名
 * @param secondaryTitle 副标题，一般是评论发送时间
 * @param rhsTitle 靠右的标题，一般是番剧打分
 * @param content 评论内容
 * @param reactionRow 评论回应的各种表情
 * @param actionRow 评论操作，例如包含回复，添加回应，绝交，举报等按钮
 * @param reply 评论回复
 */
@Composable
fun Comment(
    avatar: @Composable BoxScope.() -> Unit,
    primaryTitle: @Composable ColumnScope.() -> Unit,
    secondaryTitle: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    rhsTitle: @Composable RowScope.() -> Unit = { },
    reactionRow: @Composable ColumnScope.() -> Unit = {},
    actionRow: (@Composable ColumnScope.() -> Unit)? = null,
    reply: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.padding(top = 2.dp).clip(CircleShape)) {
            avatar()
        }
        val horizontalPadding = 12.dp
        Column {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                        primaryTitle()
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelMedium.copy(
                            color = LocalContentColor.current.slightlyWeaken(),
                        ),
                    ) {
                        secondaryTitle()
                    }
                }
                rhsTitle()
            }
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer(
                modifier = Modifier.padding(horizontal = horizontalPadding).fillMaxWidth(),
            ) {
                content()
            }

            SelectionContainer(
                modifier = Modifier
                    .paddingIfNotEmpty(top = 8.dp)
                    .padding(horizontal = horizontalPadding).fillMaxWidth(),
            ) {
                reactionRow()
            }

            if (actionRow != null && isInDebugMode()) {
                SelectionContainer(
                    modifier = Modifier.padding(horizontal = horizontalPadding - 8.dp).fillMaxWidth(),
                ) {
                    actionRow()
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }
            if (reply != null) {
                Surface(
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                        .padding(top = if (actionRow == null) 12.dp else 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small,
                ) {
                    reply()
                }
            }
        }
    }
}

/**
 * A state which is read by Comment composable
 */
@Stable
class CommentState(
    sourceVersion: State<Any?>,
    list: State<List<UIComment>>,
    hasMore: State<Boolean>,
    private val onReload: suspend () -> Unit,
    private val onLoadMore: suspend () -> Unit,
    private val onSubmitCommentReaction: suspend (commentId: Int, reactionId: Int) -> Unit,
    backgroundScope: CoroutineScope,
) {
    private val currentSourceVersion: Any? by sourceVersion
    private var lastSourceVersion: Any? = null

    var sourceVersion: Any?
        get() = currentSourceVersion
        set(value) {
            lastSourceVersion = value
        }

    val list: List<UIComment> by list

    /**
     * 至少 [onReload] 了一次
     */
    private var loadedOnce by mutableStateOf(false)
    private var freshLoaded by mutableStateOf(false)
    private val _hasMore by hasMore
    val hasMore: Boolean by derivedStateOf {
        if (!freshLoaded) return@derivedStateOf false
        _hasMore
    }

    val count by derivedStateOf {
        if (!loadedOnce) null else this.list.size
    }

    private val reloadTasker = MonoTasker(backgroundScope)
    val isLoading get() = reloadTasker.isRunning

    private val reactionSubmitTasker = MonoTasker(backgroundScope)

    fun sourceVersionEquals(): Boolean {
        return lastSourceVersion == currentSourceVersion
    }

    /**
     * 在 LaunchedEffect 中 reload，composition 退出就没必要继续加载
     */
    fun reload() {
        reloadTasker.launch {
            withContext(Dispatchers.Main) {
                freshLoaded = false
            }
            onReload()
            withContext(Dispatchers.Main) {
                freshLoaded = true
                loadedOnce = true
            }
        }
    }

    fun loadMore() {
        reloadTasker.launch {
            onLoadMore()
        }
    }

    fun submitReaction(commentId: Int, reactionId: Int) {
        reactionSubmitTasker.launch {
            onSubmitCommentReaction(commentId, reactionId)
        }
    }
}


@Immutable
class UIRichText(val elements: List<UIRichElement>)

@Immutable
class UIComment(
    val id: Int,
    val creator: UserInfo?,
    val content: UIRichText,
    val createdAt: Long, // timestamp millis
    val reactions: List<UICommentReaction>,
    val briefReplies: List<UIComment>,
    val replyCount: Int,
    val rating: Int?,
)

@Immutable
class UICommentReaction(
    val id: Int,
    val count: Int,
    val selected: Boolean
)

