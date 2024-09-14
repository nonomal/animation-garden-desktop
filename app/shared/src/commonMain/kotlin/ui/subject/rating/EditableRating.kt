package me.him188.ani.app.ui.subject.rating

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.tools.MonoTasker


@Stable
class EditableRatingState(
    ratingInfo: State<RatingInfo>,
    selfRatingInfo: State<SelfRatingInfo>,
    enableEdit: State<Boolean>,
    /**
     * 是否收藏了待评分的条目. 必须要收藏才能评分.
     */
    private val isCollected: () -> Boolean,
    private val onRate: suspend (RateRequest) -> Unit,
    backgroundScope: CoroutineScope,
) {
    val ratingInfo by ratingInfo
    val selfRatingInfo by selfRatingInfo
    private val enableEdit by enableEdit

    val clickEnabled by derivedStateOf { this.enableEdit && !isUpdatingRating }

    var showRatingRequiresCollectionDialog by mutableStateOf(false)

    var showRatingDialog by mutableStateOf(false)
        private set

    fun requestEdit() {
        if (isCollected()) {
            showRatingDialog = true
        } else {
            showRatingRequiresCollectionDialog = true
        }
    }

    fun cancelEdit() {
        showRatingDialog = false
        showRatingRequiresCollectionDialog = false
    }

    private val tasker = MonoTasker(backgroundScope)
    val isUpdatingRating get() = tasker.isRunning
    fun updateRating(rateRequest: RateRequest) {
        tasker.launch {
            onRate(rateRequest)
            showRatingDialog = false
        }
    }

    fun dismissRatingRequiresCollectionDialog() {
        showRatingRequiresCollectionDialog = false
    }
}

/**
 * 显示 [Rating] 和 [RatingEditorDialog] 的组合
 */
@Composable
fun EditableRating(
    state: EditableRatingState,
    modifier: Modifier = Modifier,
) {
    if (state.showRatingRequiresCollectionDialog) {
        AlertDialog(
            { state.dismissRatingRequiresCollectionDialog() },
            text = { Text("请先收藏再评分") },
            confirmButton = {
                TextButton({ state.dismissRatingRequiresCollectionDialog() }) {
                    Text("关闭")
                }
            },
        )
    }

    if (state.showRatingDialog) {
        val selfRatingInfo = state.selfRatingInfo
        RatingEditorDialog(
            remember(selfRatingInfo) {
                RatingEditorState(
                    initialScore = selfRatingInfo.score,
                    initialComment = selfRatingInfo.comment ?: "",
                    initialIsPrivate = selfRatingInfo.isPrivate,
                )
            },
            onDismissRequest = {
                state.cancelEdit()
            },
            onRate = { state.updateRating(it) },
            isLoading = state.isUpdatingRating,
        )
    }
    Rating(
        rating = state.ratingInfo,
        selfRatingScore = state.selfRatingInfo.score,
        onClick = { state.requestEdit() },
        clickEnabled = state.clickEnabled,
        modifier = modifier,
    )
}
