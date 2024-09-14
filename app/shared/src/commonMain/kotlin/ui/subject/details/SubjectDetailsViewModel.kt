package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.app.data.models.preference.EpisodeListProgressTheme
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.RelatedSubjectInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.data.repository.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.repository.CommentRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AuthState
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.progress.EpisodeListState
import me.him188.ani.app.ui.subject.collection.progress.EpisodeListStateFactory
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressStateFactory
import me.him188.ani.app.ui.subject.components.comment.CommentLoader
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.app.ui.subject.rating.RateRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SubjectDetailsViewModel(
    private val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectManager: SubjectManager by inject()
    private val browserNavigator: BrowserNavigator by inject()
    private val bangumiRelatedCharactersRepository: BangumiRelatedCharactersRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val commentRepository: CommentRepository by inject()

    private val subjectInfo: SharedFlow<SubjectInfo> = subjectManager.subjectInfoFlow(subjectId).shareInBackground()
    private val subjectCollectionFlow = subjectManager.subjectCollectionFlow(subjectId).shareInBackground()

    lateinit var navigator: AniNavigator

    val authState = AuthState()

    private val subjectProgressInfoState = subjectCollectionFlow.map { SubjectProgressInfo.calculate(it) }
        .produceState(null)

    val subjectProgressState = kotlin.run {
        SubjectProgressStateFactory(
            subjectManager,
            onPlay = { subjectId, episodeId ->
                navigator.navigateEpisodeDetails(subjectId, episodeId)
            },
        ).run {
            SubjectProgressState(
                stateOf(subjectId),
                subjectProgressInfoState,
                episodeProgressInfoList(subjectId).produceState(emptyList()),
                onPlay,
            )
        }
    }

    val subjectDetailsState = kotlin.run {
        SubjectDetailsState(
            subjectInfoState = subjectInfo.produceState(null),
            selfCollectionTypeState = subjectCollectionFlow.map { it.collectionType }.produceState(null),
            airingLabelState = AiringLabelState(
                subjectCollectionFlow.map { it.airingInfo }.produceState(null),
                subjectProgressInfoState,
            ),
            personsState = bangumiRelatedCharactersRepository.relatedPersonsFlow(subjectId).map {
                RelatedPersonInfo.sortList(it)
            }.onCompletion { if (it != null) emit(emptyList()) }.produceState(null),
            charactersState = bangumiRelatedCharactersRepository.relatedCharactersFlow(subjectId).map {
                RelatedCharacterInfo.sortList(it)
            }.produceState(null),
            relatedSubjectsState = bangumiRelatedCharactersRepository.relatedSubjectsFlow(subjectId).map {
                RelatedSubjectInfo.sortList(it)
            }.produceState(null),
        )
    }

    val episodeListState by lazy {
        EpisodeListStateFactory(
            settingsRepository,
            subjectManager,
            backgroundScope,
        ).run {
            EpisodeListState(
                stateOf(subjectId),
                theme.produceState(EpisodeListProgressTheme.Default),
                episodes(subjectId).produceState(emptyList()),
                ::onSetEpisodeWatched,
                backgroundScope,
            )
        }
    }

    val editableSubjectCollectionTypeState = EditableSubjectCollectionTypeState(
        selfCollectionType = subjectCollectionFlow
            .map { it.collectionType }
            .produceState(UnifiedCollectionType.NOT_COLLECTED),
        hasAnyUnwatched = hasAnyUnwatched@{
            val collections = subjectManager.episodeCollectionsFlow(subjectId)
                .flowOn(Dispatchers.Default).firstOrNull() ?: return@hasAnyUnwatched true
            collections.any { !it.type.isDoneOrDropped() }
        },
        onSetSelfCollectionType = { subjectManager.setSubjectCollectionType(subjectId, it) },
        onSetAllEpisodesWatched = {
            subjectManager.setAllEpisodesWatched(subjectId)
        },
        backgroundScope,
    )

    val editableRatingState = EditableRatingState(
        ratingInfo = subjectInfo.map { it.ratingInfo }.produceState(RatingInfo.Empty),
        selfRatingInfo = subjectCollectionFlow.map { it.selfRatingInfo }
            .produceState(SelfRatingInfo.Empty),
        enableEdit = subjectCollectionFlow
            .map { it.collectionType != UnifiedCollectionType.NOT_COLLECTED }
            .produceState(false),
        isCollected = {
            val collection = subjectCollectionFlow.replayCache.firstOrNull() ?: return@EditableRatingState false
            collection.collectionType != UnifiedCollectionType.NOT_COLLECTED
        },
        onRate = { request ->
            subjectManager.updateRating(
                subjectId,
                request,
            )
        },
        backgroundScope,
    )

    val detailsTabLazyListState = LazyListState()
    val commentTabLazyListState = LazyListState()

    private val subjectCommentLoader = CommentLoader.createForSubject(
        subjectId = flowOf(subjectId),
        coroutineContext = backgroundScope.coroutineContext,
        subjectCommentSource = { commentRepository.getSubjectComments(it) },
    )

    val subjectCommentState: CommentState = CommentState(
        sourceVersion = subjectCommentLoader.sourceVersion.produceState(null),
        list = subjectCommentLoader.list.produceState(emptyList()),
        hasMore = subjectCommentLoader.hasFinished.map { !it }.produceState(true),
        onReload = { subjectCommentLoader.reload() },
        onLoadMore = { subjectCommentLoader.loadMore() },
        onSubmitCommentReaction = { _, _ -> },
        backgroundScope = backgroundScope,
    )

    fun browseSubjectBangumi(context: ContextMP) {
        browserNavigator.openBrowser(context, "https://bgm.tv/subject/${subjectId}")
    }
}

suspend inline fun SubjectManager.updateRating(subjectId: Int, request: RateRequest) {
    return this.updateRating(subjectId, request.score, request.comment, isPrivate = request.isPrivate)
}
