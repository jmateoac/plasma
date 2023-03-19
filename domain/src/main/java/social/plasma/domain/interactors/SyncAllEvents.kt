package social.plasma.domain.interactors

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import social.plasma.domain.Interactor
import social.plasma.models.PubKey
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncAllEvents @Inject constructor(
    private val relay: Relay,
    private val storeEvents: StoreEvents,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncAllEvents.Params>() {

    override suspend fun doWork(params: Params) = withContext(ioDispatcher) {
        val pubkey = params.pubKey

        val subscribeMessage = ClientMessage.SubscribeMessage(
            Filter(authors = setOf(pubkey.hex), since = Instant.EPOCH, limit = 2000),
            Filter(pTags = setOf(pubkey.hex), limit = 2000)
        )

        val subscription = relay.subscribe(subscribeMessage).distinctUntilChanged().map { it.event }

        storeEvents(subscription)
        storeEvents.flow.collect()
    }

    data class Params(
        val pubKey: PubKey,
    )
}