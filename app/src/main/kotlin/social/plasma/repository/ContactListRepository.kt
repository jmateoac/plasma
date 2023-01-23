package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import social.plasma.nostr.models.Contact
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.SubscribeMessage
import javax.inject.Inject

interface ContactListRepository {
    fun observeContactLists(pubkey: String): Flow<Set<Contact>>
}

class RealContactListRepository @Inject constructor(
    private val relay: Relay,
    private val eventRefiner: EventRefiner,
) : ContactListRepository {

    override fun observeContactLists(pubkey: String): Flow<Set<Contact>> {
        return relay.subscribe(SubscribeMessage(filters = Filters.contactList(pubkey)))
            .map { eventRefiner.toContacts(it) }
            .filterNotNull()
            .map {
                it.contacts()
            }
    }
}
