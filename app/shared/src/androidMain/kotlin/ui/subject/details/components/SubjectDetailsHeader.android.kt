package me.him188.ani.app.ui.subject.details.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.app.data.models.subject.SubjectCollectionStats
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.rememberTestEditableRatingState
import me.him188.ani.app.ui.subject.rating.EditableRating
import me.him188.ani.app.ui.subject.rating.TestRatingInfo
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.annotations.TestOnly

internal val TestCollectionStats = SubjectCollectionStats(
    wish = 100,
    doing = 200,
    done = 300,
    onHold = 400,
    dropped = 500,
)

internal val TestSubjectInfo = SubjectInfo.Empty.copy(
    nameCn = "孤独摇滚！",
    name = "ぼっち・ざ・ろっく！",
    airDateString = "2023-10-01",
    summary = """
        作为网络吉他手“吉他英雄”而广受好评的后藤一里，在现实中却是个什么都不会的沟通障碍者。一里有着组建乐队的梦想，但因为不敢向人主动搭话而一直没有成功，直到一天在公园中被伊地知虹夏发现并邀请进入缺少吉他手的“结束乐队”。可是，完全没有和他人合作经历的一里，在人前完全发挥不出原本的实力。为了努力克服沟通障碍，一里与“结束乐队”的成员们一同开始努力……
    """.trimIndent(),
    tags = listOf(
        Tag("芳文社", 7098),
        Tag("音乐", 5000),
        Tag("CloverWorks", 5000),
        Tag("轻百合", 4000),
        Tag("日常", 3758),
    ),
    ratingInfo = TestRatingInfo,
    collection = TestCollectionStats,
)

internal const val TestCoverImage = "https://ui-avatars.com/api/?name=John+Doe"

internal val TestSubjectAiringInfo = SubjectAiringInfo.EmptyCompleted

@Composable
fun rememberTestEditableSubjectCollectionTypeState(type: UnifiedCollectionType = UnifiedCollectionType.WISH): EditableSubjectCollectionTypeState {
    val backgroundScope = rememberBackgroundScope()
    val selfCollectionType = remember {
        mutableStateOf(type)
    }
    return remember {
        EditableSubjectCollectionTypeState(
            selfCollectionType,
            hasAnyUnwatched = { false },
            onSetSelfCollectionType = {
                selfCollectionType.value = it
            },
            onSetAllEpisodesWatched = { },
            backgroundScope.backgroundScope,
        )
    }
}

@Composable
@Preview
@Preview(device = Devices.TABLET)
fun PreviewSubjectDetailsHeaderCompleted() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo,
        progressInfo = SubjectProgressInfo(
            continueWatchingStatus = ContinueWatchingStatus.Done,
            nextEpisodeIdToPlay = null,
        ),
        subjectInfo = TestSubjectInfo,
    )
}

@Composable
@Preview
fun PreviewSubjectDetailsHeaderCompletedLong() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo,
        progressInfo = SubjectProgressInfo(
            continueWatchingStatus = ContinueWatchingStatus.Done,
            nextEpisodeIdToPlay = null,
        ),
        subjectInfo = TestSubjectInfo.copy(
            nameCn = "孤独摇滚".repeat(20),
        ),
    )
}

@Composable
@Preview
@Preview(device = Devices.TABLET)
fun PreviewSubjectDetailsHeaderOnAirWatched() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo.copy(
            kind = SubjectAiringKind.ON_AIR,
            episodeCount = 24,
            latestSort = EpisodeSort(20),
        ),
        progressInfo = SubjectProgressInfo(
            continueWatchingStatus = ContinueWatchingStatus.Watched(0, EpisodeSort(20), PackedDate.Invalid),
            nextEpisodeIdToPlay = null,
        ),
    )
}

@Composable
@Preview
@Preview(device = Devices.TABLET)
fun PreviewSubjectDetailsHeaderOnAirContinue() {
    PreviewSubjectDetailsHeader(
        airingInfo = TestSubjectAiringInfo.copy(
            kind = SubjectAiringKind.ON_AIR,
            episodeCount = 24,
            latestSort = EpisodeSort(20),
        ),
        progressInfo = SubjectProgressInfo(
            continueWatchingStatus = ContinueWatchingStatus.Continue(0, EpisodeSort(20), EpisodeSort(19)),
            nextEpisodeIdToPlay = null,
        ),
    )
}

@OptIn(TestOnly::class)
@Composable
fun PreviewSubjectDetailsHeader(
    airingInfo: SubjectAiringInfo,
    progressInfo: SubjectProgressInfo,
    subjectInfo: SubjectInfo = TestSubjectInfo,
) {
    ProvideCompositionLocalsForPreview {
        SubjectDetailsHeader(
            subjectInfo,
            TestCoverImage,
            airingLabelState = remember {
                AiringLabelState(stateOf(airingInfo), stateOf(progressInfo))
            },
            collectionData = {
                SubjectDetailsDefaults.CollectionData(
                    collectionStats = subjectInfo.collection,
                )
            },
            collectionAction = {
                EditableSubjectCollectionTypeButton(
                    rememberTestEditableSubjectCollectionTypeState(),
                )
            },
            selectEpisodeButton = {
                SubjectDetailsDefaults.SelectEpisodeButtons(rememberTestSubjectProgressState(), {})
            },
            rating = {
                EditableRating(
                    state = rememberTestEditableRatingState(),
                )
            },
        )
    }
}

@TestOnly
internal fun createTestAiringLabelState(
    airingInfo: SubjectAiringInfo = TestSubjectAiringInfo,
    progressInfo: SubjectProgressInfo = TestSubjectProgressInfos.ContinueWatching2,
): AiringLabelState = AiringLabelState(stateOf(airingInfo), stateOf(progressInfo))
