package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.ui.doesNotExist
import me.him188.ani.app.ui.exists
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.effects.TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE
import me.him188.ani.app.ui.foundation.effects.TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.settings.danmaku.createTestDanmakuRegexFilterState
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceInfoProvider
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceResults
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.video.sidesheet.rememberTestEpisodeSelectorState
import me.him188.ani.app.videoplayer.ui.ControllerVisibility
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.guesture.GestureFamily
import me.him188.ani.app.videoplayer.ui.guesture.NoOpLevelController
import me.him188.ani.app.videoplayer.ui.guesture.VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.TAG_PROGRESS_SLIDER_PREVIEW_POPUP
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.danmaku.ui.DanmakuHostState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class EpisodeVideoCursorTest {

    private val controllerState = VideoControllerState(ControllerVisibility.Invisible)
    private var currentPositionMillis by mutableLongStateOf(0L)
    private val progressSliderState: MediaProgressSliderState = MediaProgressSliderState(
        { currentPositionMillis },
        { 100_000 },
        stateOf(persistentListOf()),
        onPreview = {},
        onPreviewFinished = { currentPositionMillis = it },
    )

    private val SemanticsNodeInteractionsProvider.topBar
        get() = onNodeWithTag(TAG_EPISODE_VIDEO_TOP_BAR, useUnmergedTree = true)
    private val SemanticsNodeInteractionsProvider.previewPopup
        get() = onNodeWithTag(TAG_PROGRESS_SLIDER_PREVIEW_POPUP, useUnmergedTree = true)

    private val SemanticsNodeInteractionsProvider.cursorVisible
        get() = onNodeWithTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE, useUnmergedTree = true)

    private val SemanticsNodeInteractionsProvider.cursorInvisible
        get() = onNodeWithTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE, useUnmergedTree = true)

    @Composable
    private fun Player(gestureFamily: GestureFamily = GestureFamily.MOUSE) {
        ProvideCompositionLocalsForPreview(colorScheme = aniDarkColorTheme()) {
            val scope = rememberCoroutineScope()
            val playerState = remember {
                DummyPlayerState(scope.coroutineContext)
            }
            Row {
                EpisodeVideoImpl(
                    playerState = playerState,
                    expanded = true,
                    hasNextEpisode = true,
                    onClickNextEpisode = {},
                    videoControllerState = controllerState,
                    title = { Text("Title") },
                    danmakuHostState = remember { DanmakuHostState() },
                    danmakuEnabled = false,
                    onToggleDanmaku = {},
                    videoLoadingState = { VideoLoadingState.Succeed(isBt = true) },
                    onClickFullScreen = {},
                    onExitFullscreen = {},
                    danmakuEditor = {},
                    configProvider = { VideoScaffoldConfig.Default },
                    onClickScreenshot = {},
                    detachedProgressSlider = {
                        PlayerControllerDefaults.MediaProgressSlider(
                            progressSliderState,
                            cacheProgressState = playerState.cacheProgress,
                            enabled = false,
                        )
                    },
                    progressSliderState = progressSliderState,
                    mediaSelectorPresentation = rememberTestMediaSelectorPresentation(),
                    mediaSourceResultsPresentation = rememberTestMediaSourceResults(),
                    episodeSelectorState = rememberTestEpisodeSelectorState(),
                    mediaSourceInfoProvider = rememberTestMediaSourceInfoProvider(),
                    audioController = NoOpLevelController,
                    brightnessController = NoOpLevelController,
                    leftBottomTips = {},
                    modifier = Modifier.weight(1f),
                    danmakuRegexFilterState = createTestDanmakuRegexFilterState(),
                    gestureFamily = gestureFamily,
                )

                Column(Modifier.fillMaxHeight().requiredWidth(100.dp)) {
                    Text("Dummy")
                }
            }
        }
    }

    /**
     * 初始 controller visible, 会显示指针
     */
    @Test
    fun `initial controller visible`() = runAniComposeUiTest {
        controllerState.toggleFullVisible(true)
        setContent {
            Player()
        }
        runOnIdle {
            waitUntil { cursorVisible.exists() }
        }
    }

    /**
     * 初始 controller invisible, 但因为鼠标没有 hover 到视频, 也会显示指针
     */
    @Test
    fun `initial controller invisible`() = runAniComposeUiTest {
        controllerState.toggleFullVisible(false)
        setContent {
            Player()
        }
        runOnIdle {
            waitUntil { cursorVisible.exists() } // 因为没有 hover
        }
    }

    /**
     * 初始 controller invisible, 但因为鼠标没有 hover 到视频, 也会显示指针.
     * 当鼠标滑入视频 (并且 controller 也显示几秒隐藏后), 会显示指针
     */
    @Test
    fun `initial controller invisible and hover`() = runAniComposeUiTest {
        controllerState.toggleFullVisible(false)
        setContent {
            Player()
        }
        runOnIdle {
            waitUntil { cursorVisible.exists() } // 因为没有 hover
        }
        runOnIdle {
            onRoot().performMouseInput {
                moveTo(center)
            }
        } // 这里不会因为滑动鼠标而显示 controller 进而显示 cursor, 因为会自动 advance 时间跳过状态
        runOnIdle {
            waitUntil { cursorInvisible.exists() }
        }
    }

    /**
     * 滑出视频区域后显示指针
     */
    @Test
    fun `show cursor when outside of video`() = runAniComposeUiTest {
        controllerState.toggleFullVisible(false)
        setContent {
            Player()
        }
        runOnIdle {
            waitUntil { cursorVisible.exists() } // 因为没有 hover
        }
        runOnIdle {
            onRoot().performMouseInput {
                moveTo(center)
            }
        }
        runOnIdle {
            waitUntil { cursorInvisible.exists() } // hover 了
        }
        runOnIdle {
            onRoot().performMouseInput {
                moveTo(centerRight) // 移出视频区域
            }
        }
        runOnIdle {
            assertEquals(ControllerVisibility.Invisible, controllerState.visibility)
            waitUntil { cursorVisible.exists() }
        }
    }

    /**
     * 在 controller visible 时鼠标滑入播放器, 等待几秒后隐藏 controller, 同时隐藏 cursor
     */
    @Test
    fun `hide cursor after some seconds`() = runAniComposeUiTest {
        controllerState.toggleFullVisible(true)
        mainClock.autoAdvance = false
        setContent {
            Player(gestureFamily = GestureFamily.MOUSE)
        }
        val root = onAllNodes(isRoot()).onFirst()
        runOnIdle {
            assertEquals(true, controllerState.visibility.topBar)
            waitUntil { cursorVisible.exists() } // 因为没有 hover
            root.performMouseInput {
                moveTo(centerRight) // 初始在视频外面
            }
        }
        runOnIdle {
            root.performMouseInput {
                moveTo(center)
            }
            // 目前的 controller mouseHoverForController 依赖 Move 事件, 但 compose 似乎有点问题
            // 所以额外广播一个事件
            root.performTouchInput {
                swipe(center, center - Offset(1f, 1f))
            }
        }
        runOnIdle {
            assertEquals(true, controllerState.visibility.topBar)
            waitUntil { cursorVisible.exists() }
        }
        runOnIdle {
            mainClock.advanceTimeBy((VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION + 1.seconds).inWholeMilliseconds)
            mainClock.autoAdvance = true
        }
        runOnIdle {
            waitUntil { topBar.doesNotExist() }
            waitUntil { cursorInvisible.doesNotExist() }
            assertEquals(false, controllerState.visibility.topBar)
        }
    }
}
