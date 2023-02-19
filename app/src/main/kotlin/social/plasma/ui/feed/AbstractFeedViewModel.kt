package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.cash.molecule.RecompositionClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.db.notes.NoteWithUser
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.mappers.NotePagingFlowMapper
import java.net.URL

// TODO convert to assisted viewmodel instead of abstract
abstract class AbstractFeedViewModel(
    recompositionClock: RecompositionClock,
    private val userMetaDataRepository: UserMetaDataRepository,
    private val reactionsRepository: ReactionsRepository,
    private val openGraphParser: OpenGraphParser,
    notePagingFlowMapper: NotePagingFlowMapper,
    pagingFlow: Flow<PagingData<NoteWithUser>>,
) : MoleculeViewModel<FeedUiState, FeedUiEvent>(recompositionClock) {
    private val feedPagingFlow = notePagingFlowMapper.map(pagingFlow)
        .cachedIn(viewModelScope)

    @Composable
    override fun models(events: Flow<FeedUiEvent>): FeedUiState {
        return FeedUiState.Loaded(feedPagingFlow = feedPagingFlow)
    }

    open fun onNoteDisposed(id: NoteId, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(pubkey.hex)
            reactionsRepository.stopSyncNoteReactions(id.hex)
        }
    }

    open fun onNoteDisplayed(id: NoteId, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(pubkey.hex)
            reactionsRepository.syncNoteReactions(id.hex)
        }
    }

    fun onNoteReaction(noteId: NoteId) {
        viewModelScope.launch {
            reactionsRepository.sendReaction(noteId.hex)
        }
    }

    suspend fun getOpenGraphMetadata(url: String): OpenGraphMetadata? {
        return runCatching { openGraphParser.parse(URL(url)) }.getOrNull()
    }
}
