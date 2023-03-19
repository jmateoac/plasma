package social.plasma.feeds.presenters.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.PagingData
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.retained.rememberRetained
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.RepostNote
import social.plasma.domain.interactors.SendNoteReaction
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.models.NoteWithUser
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

class FeedPresenter @AssistedInject constructor(
    private val notePagingFlowMapper: NotePagingFlowMapper,
    private val sendNoteReaction: SendNoteReaction,
    private val repostNote: RepostNote,
    private val syncMetadata: SyncMetadata,
    private val openGraphParser: OpenGraphParser,
    @Assisted private val pagingFlow: Flow<PagingData<NoteWithUser>>,
    @Assisted private val navigator: Navigator,
) : Presenter<FeedUiState> {

    private val getOpenGraphMetadata: suspend (String) -> OpenGraphMetadata? =
        {
            try {
                openGraphParser.parse(URL(it))
            } catch (e: MalformedURLException) {
                Timber.w(e)
                null
            }
        }

    @Composable
    override fun present(): FeedUiState {
        val feedPagingFlow = rememberRetained { notePagingFlowMapper.map(pagingFlow) }
        val coroutineScope = rememberCoroutineScope()

        return FeedUiState(
            pagingFlow = feedPagingFlow,
            getOpenGraphMetadata = getOpenGraphMetadata
        ) { event ->
            when (event) {
                is FeedUiEvent.OnNoteClick -> navigator.goTo(ThreadScreen(event.noteId))
                is FeedUiEvent.OnReplyClick -> navigator.goTo(ComposingScreen(parentNote = event.noteId))
                is FeedUiEvent.OnNoteRepost -> {
                    coroutineScope.launch {
                        repostNote.executeSync(RepostNote.Params(noteId = event.noteId))
                    }
                }

                is FeedUiEvent.OnNoteReaction -> {
                    coroutineScope.launch {
                        sendNoteReaction.executeSync(SendNoteReaction.Params(noteId = event.noteId))
                    }
                }

                is FeedUiEvent.OnProfileClick -> {
                    navigator.goTo(ProfileScreen(pubKeyHex = event.pubKey.hex))
                }

                is FeedUiEvent.OnNoteDisplayed -> {
                    coroutineScope.launch {
                        syncMetadata.executeSync(SyncMetadata.Params(event.pubKey))
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, pagingFlow: Flow<PagingData<NoteWithUser>>): FeedPresenter
    }
}