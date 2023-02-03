package social.plasma.ui.mappers

import android.text.format.DateUtils
import kotlinx.coroutines.flow.firstOrNull
import social.plasma.PubKey
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.ui.components.notes.NoteUiModel
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCardMapper @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
) {
    private val imageUrlRegex = Regex("https?:/(/[^/]+)+\\.(?:jpg|gif|png|jpeg|svg|webp)")
    private val tagPlaceholderRegex = Regex("#\\[[0-9]+]")

    suspend fun toNoteUiModel(noteWithUser: NoteWithUser): NoteUiModel {
        val note = noteWithUser.noteEntity
        val author = noteWithUser.userMetadataEntity
        val authorPubKey = PubKey(note.pubkey)

        return NoteUiModel(
            id = note.id,
            name = author?.name ?: authorPubKey.shortBech32,
            content = splitIntoContentBlocks(note).filterNotNull(),
            avatarUrl = author?.picture
                ?: "https://api.dicebear.com/5.x/bottts/jpg?seed=${authorPubKey.hex}",
            timePosted = Instant.ofEpochMilli(note.createdAt).relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = if (note.reactionCount > 0) "${note.reactionCount}" else "",
            userPubkey = authorPubKey,
            nip5 = author?.nip05,
            displayName = author?.displayName?.takeIf { it.isNotBlank() } ?: author?.name
            ?: authorPubKey.shortBech32,
            cardLabel = note.buildBannerLabel(),
        )
    }

    private suspend fun NoteView.buildBannerLabel(): String? {
        val referencedNames = getReferencedNames(this)

        return if (referencedNames.isNotEmpty()) {
            referencedNames.generateBannerLabel()
        } else null
    }

    private suspend fun getReferencedNames(note: NoteView): MutableSet<String> {
        val referencedNames = mutableSetOf<String>()

        note.tags.forEachIndexed { index, it ->
            if (it.firstOrNull() == "p") {
                val pubkey = PubKey(it[1])

                val userName = (userMetadataDao.getById(pubkey.hex).firstOrNull()?.name
                    ?: pubkey.shortBech32)

                if (pubkey.hex != note.pubkey) {
                    referencedNames.add(userName)
                }
            }
        }
        return referencedNames
    }

    private suspend fun splitIntoContentBlocks(
        note: NoteView,
    ): List<NoteUiModel.ContentBlock?> {
        val tagIndexMap = note.tags.toIndexedMap()

        val imageUrls = note.parseImageUrls()

        val imageContent = when {
            imageUrls.size == 1 -> NoteUiModel.ContentBlock.Image(imageUrls.first())
            imageUrls.size > 1 -> NoteUiModel.ContentBlock.Carousel(imageUrls.toList())
            else -> null
        }

        return listOf(
            NoteUiModel.ContentBlock.Text(
                replacePlaceholders(
                    note.content,
                    tagIndexMap
                )
            )
        ) + imageContent
    }


    private suspend fun List<List<String>>.toIndexedMap(): Map<Int, String> =
        mapIndexed { index, tag ->
            when (tag.firstOrNull()) {
                "p" -> {
                    val pubkey = PubKey(tag[1])

                    val userName = (userMetadataDao.getById(pubkey.hex).firstOrNull()?.name
                        ?: pubkey.shortBech32)

                    return@mapIndexed index to "@${userName}"
                }

                "e" -> {
                    return@mapIndexed index to "@${tag[1]}"
                }

                else -> null
            }
        }.filterNotNull().toMap()


    private fun NoteView.parseImageUrls(): Set<String> =
        imageUrlRegex.findAll(content).map { it.value }.toSet()

    private fun Instant.relativeTime(): String {
        return DateUtils.getRelativeTimeSpanString(
            this.toEpochMilli(),
            Instant.now().toEpochMilli(),
            DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_ALL
        ).toString()
    }

    private fun replacePlaceholders(input: String, replacements: Map<Int, String>): String {
        var result = input
        val matches = tagPlaceholderRegex.findAll(input)
        for (match in matches) {
            val placeholder = match.value

            val key = placeholder.substring(2, placeholder.length - 1).toInt()

            if (replacements.containsKey(key)) {
                result = result.replace(placeholder, replacements[key]!!)
            }
        }
        return result
    }

    private fun Iterable<String>.generateBannerLabel(): String {
        return "Replying to ${this.joinToString()}"
    }
}