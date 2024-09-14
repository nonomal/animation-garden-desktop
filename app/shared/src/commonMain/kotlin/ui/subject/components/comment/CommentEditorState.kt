package me.him188.ani.app.ui.subject.components.comment

import androidx.annotation.UiThread
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.comment.CommentEditorTextState
import org.jetbrains.compose.resources.DrawableResource
import kotlin.coroutines.CoroutineContext

@Stable
class CommentEditorState(
    showExpandEditCommentButton: Boolean,
    initialEditExpanded: Boolean,
    panelTitle: State<String?>,
    stickers: State<List<EditCommentSticker>>,
    private val richTextRenderer: suspend (String) -> UIRichText,
    private val onSend: suspend (target: CommentContext, content: String) -> Unit,
    backgroundScope: CoroutineScope,
) {
    private val editor = CommentEditorTextState("")

    private val sendTasker = MonoTasker(backgroundScope)

    val panelTitle by panelTitle

    var currentSendTarget: CommentContext? by mutableStateOf(null)
        private set
    val sending: Boolean get() = sendTasker.isRunning
    
    val content get() = editor.textField
    var previewing by mutableStateOf(false)
        private set
    var previewContent: UIRichText? by mutableStateOf(null)
        private set

    var editExpanded: Boolean by mutableStateOf(initialEditExpanded)
    val expandButtonState by derivedStateOf { if (!showExpandEditCommentButton) null else editExpanded }
    
    var showStickerPanel: Boolean by mutableStateOf(false)
        private set
    val stickers by stickers

    /**
     * 连续开关为同一个评论的编辑框将保存编辑内容和编辑框状态
     */
    fun startEdit(newTarget: CommentContext) {
        if (newTarget != currentSendTarget) {
            editor.override(TextFieldValue(""))
        }
        currentSendTarget = newTarget
        previewing = false
        previewContent = null
        editExpanded = false
    }

    fun toggleStickerPanelState(desired: Boolean? = null) {
        showStickerPanel = desired ?: !showStickerPanel
    }

    fun setContent(value: TextFieldValue) {
        editor.override(value)
    }

    /**
     * @see CommentEditorTextState.wrapSelectionWith
     */
    fun wrapSelectionWith(value: String, secondSliceIndex: Int) {
        editor.wrapSelectionWith(value, secondSliceIndex)
    }

    /**
     * @see CommentEditorTextState.insertTextAt
     */
    fun insertTextAt(value: String, cursorOffset: Int = value.length) {
        editor.insertTextAt(value, cursorOffset)
    }

    fun togglePreview() {
        previewing = !previewing
    }

    @UiThread
    suspend fun renderPreview() {
        previewContent = null
        val rendered = richTextRenderer(content.text)
        previewContent = rendered
    }

    suspend fun send(context: CoroutineContext = Dispatchers.Default) {
        val target = currentSendTarget
        val content = editor.textField.text

        editExpanded = false

        sendTasker.launch(context) {
            checkNotNull(target)
            onSend(target, content)
        }
        sendTasker.join()

        editor.override(TextFieldValue(""))
    }
}

@Immutable
data class EditCommentSticker(
    val id: Int,
    val drawableRes: DrawableResource?,
)

/**
 * 评论发送的对象，在 [CommentEditorState.onSend] 需要提供。
 */
@Immutable
sealed interface CommentContext {
    /**
     * 剧集评论
     */
    data class Episode(val subjectId: Int, val episodeId: Int) : CommentContext

    /**
     * 条目吐槽箱
     */
    data class Subject(val subjectId: Int) : CommentContext

    /**
     * 回复某个人的评论
     */
    data class Reply(val commentId: Int) : CommentContext
}