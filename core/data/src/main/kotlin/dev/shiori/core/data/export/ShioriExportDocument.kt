package dev.shiori.core.data.export

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ShioriExportDocument(
    val format: String = "shiori-export",
    val schemaVersion: String = "1.0-draft",
    val exportedAt: String,
    val scope: String = "full-library",
    val data: ShioriExportPayload,
)

@Serializable
data class ShioriExportPayload(
    val works: List<WorkDto> = emptyList(),
    val editions: List<EditionDto> = emptyList(),
    val libraryEntries: List<LibraryEntryDto> = emptyList(),
    val readingProgress: List<ReadingProgressDto> = emptyList(),
)

@Serializable
data class WorkDto(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val format: String,
    val synopsis: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val revision: Long = 1L,
)

@Serializable
data class EditionDto(
    val id: String,
    val workId: String,
    val language: String,
    val label: String,
    val totalChapters: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val revision: Long = 1L,
)

@Serializable
data class LibraryEntryDto(
    val id: String,
    val workId: String,
    val editionId: String,
    val status: String,
    val favorite: Boolean = false,
    val lists: List<String> = emptyList(),
    val notes: String = "",
    val preferredReadingMode: String = "ADAPTIVE",
    val createdAt: String,
    val updatedAt: String,
    val revision: Long = 1L,
)

@Serializable
data class ReadingProgressDto(
    val id: String,
    val editionId: String,
    val sourceWorkId: String? = null,
    val chapterExternalId: String? = null,
    val chapterNumber: Double = 0.0,
    val positionMarker: String? = null,
    val sourceUrl: String? = null,
    val origin: String = "MANUAL",
    val lastReadAt: String? = null,
    val updatedAt: String,
    val revision: Long = 1L,
)

object ShioriExportSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(document: ShioriExportDocument): String = json.encodeToString(document)

    fun decode(rawJson: String): ShioriExportDocument {
        val doc = json.decodeFromString<ShioriExportDocument>(rawJson)
        require(doc.format == "shiori-export") { "Unsupported export format: ${doc.format}" }
        return doc
    }
}
