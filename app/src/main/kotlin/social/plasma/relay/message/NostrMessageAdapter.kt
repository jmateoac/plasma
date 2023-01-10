package social.plasma.relay.message

import com.squareup.moshi.*
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import social.plasma.models.Event
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.RelayMessage.NoticeRelayMessage
import java.time.Instant

class NostrMessageAdapter {

    // RequestMessage
    @FromJson
    fun requestMessageFromJson(
        reader: JsonReader,
        filtersDelegate: JsonAdapter<Filters>
    ): RequestMessage {
        reader.beginArray()
        reader.nextString()
        val subscriptionId = reader.nextString()
        val filters = filtersDelegate.fromJson(reader)!!
        reader.endArray()
        return RequestMessage(subscriptionId, filters)
    }

    @ToJson
    fun requestMessageToJson(request: RequestMessage) =
        listOf("REQ", request.subscriptionId, request.filters)


    // RelayMessage
    @FromJson
    fun relayMessageFromJson(reader: JsonReader, eventDelegate: JsonAdapter<Event>): RelayMessage {
        reader.beginArray()
        val message = when (reader.nextString()) {
            "EVENT" -> EventRelayMessage(
                subscriptionId = reader.nextString(),
                event = eventDelegate.fromJson(reader)!!
            )
            "NOTICE" -> NoticeRelayMessage(reader.nextString())
            else -> throw java.lang.IllegalArgumentException()
        }
        reader.endArray()
        return message
    }

    @ToJson
    fun relayMessageToJson(
        writer: JsonWriter,
        message: RelayMessage,
        eventDelegate: JsonAdapter<Event>
    ) {
        when (message) {
            is NoticeRelayMessage -> {
                writer.beginArray()
                    .value("NOTICE")
                    .value(message.message)
                    .endArray()
            }
            is EventRelayMessage -> {
                writer.beginArray()
                    .value("EVENT")
                    .value(message.subscriptionId)
                eventDelegate.toJson(writer, message.event)
                writer.endArray()
            }
        }
    }


    // === primitives

    // Hex ByteString
    @FromJson
    fun byteStringFromJson(s: String): ByteString = s.decodeHex()

    @ToJson
    fun byteStringToJson(b: ByteString): String = b.hex()

    // Instant
    @FromJson
    fun instantFromJson(seconds: Long): Instant = Instant.ofEpochSecond(seconds)

    @ToJson
    fun instantToJson(i: Instant): Long = i.epochSecond

}