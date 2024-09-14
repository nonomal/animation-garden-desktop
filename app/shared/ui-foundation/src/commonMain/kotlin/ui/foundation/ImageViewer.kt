package me.him188.ani.app.ui.foundation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.annotation.ExperimentalCoilApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.him188.ani.app.imageviewer.zoomable.ZoomableGestureScope
import me.him188.ani.app.imageviewer.zoomable.rememberZoomableState

interface ImageViewerHandler {
    val imageModel: StateFlow<Any?>
    val viewing: State<Boolean>

    fun viewImage(model: Any?)
    fun clear()
}

val LocalImageViewerHandler: ProvidableCompositionLocal<ImageViewerHandler> = compositionLocalOf {
    error("no ImageViewerHandler provided")
}

@Composable
fun rememberImageViewerHandler(): ImageViewerHandler {
    return remember {
        object : ImageViewerHandler {
            override val imageModel: MutableStateFlow<Any?> = MutableStateFlow(null)
            override val viewing: MutableState<Boolean> = mutableStateOf(false)

            override fun viewImage(model: Any?) {
                imageModel.value = model
                viewing.value = model != null
            }

            override fun clear() {
                imageModel.value = null
                viewing.value = false
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageViewer(
    handler: ImageViewerHandler,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val model by handler.imageModel.collectAsStateWithLifecycle()

    var contentSizeX by rememberSaveable(key = model.toString()) { mutableStateOf(0f) }
    var contentSizeY by rememberSaveable(key = model.toString()) { mutableStateOf(0f) }
    val contentSize by derivedStateOf { Size(contentSizeX, contentSizeY) }

    val zoomableState = rememberZoomableState(key = handler.viewing.value)

    LaunchedEffect(contentSize, handler.viewing.value) {
        zoomableState.contentSize = contentSize
    }

    AnimatedVisibility(
        visible = handler.viewing.value,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        me.him188.ani.app.imageviewer.ImageViewer(
            model = me.him188.ani.app.imageviewer.ImageViewer.AnyComposable(
                composable = {
                    AsyncImage(
                        model = model,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = null,
                        onSuccess = {
                            contentSizeX = it.result.image.width.toFloat()
                            contentSizeY = it.result.image.height.toFloat()
                        },
                    )
                },
            ),
            state = zoomableState,
            modifier = Modifier.background(Color.Black),
            detectGesture = ZoomableGestureScope(
                onDoubleTap = { offset -> scope.launch { zoomableState.toggleScale(offset) } },
                onTap = { _ -> onClose() },
            ),
        )
    }
}