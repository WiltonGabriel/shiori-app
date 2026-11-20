package dev.shiori.core.domain.model

/**
 * Conceptual work identity independent of any specific website URL or edition.
 */
data class Work(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val format: WorkFormat,
    val synopsis: String? = null,
    val tags: List<String> = emptyList(),
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long = 1L,
    val deletedAtUtc: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Work id must not be blank" }
        require(title.isNotBlank()) { "Work title must not be blank" }
        require(revision >= 1L) { "Work revision must be >= 1" }
    }
}

/**
 * Specific edition, language, or translation of a conceptual [Work].
 */
data class Edition(
    val id: String,
    val workId: String,
    val language: String = "pt-BR",
    val label: String = "Default",
    val totalChapters: Int? = null,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long = 1L,
    val deletedAtUtc: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Edition id must not be blank" }
        require(workId.isNotBlank()) { "Edition workId must not be blank" }
        require(totalChapters == null || totalChapters >= 0) { "totalChapters must be >= 0" }
        require(revision >= 1L) { "Edition revision must be >= 1" }
    }
}

/**
 * User's personal library relationship to a [Work] and [Edition].
 */
data class LibraryEntry(
    val id: String,
    val workId: String,
    val editionId: String,
    val status: ReadingStatus = ReadingStatus.READING,
    val favorite: Boolean = false,
    val lists: List<String> = emptyList(),
    val notes: String = "",
    val preferredReadingMode: ReadingModePreference = ReadingModePreference.ADAPTIVE,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long = 1L,
    val deletedAtUtc: String? = null,
) {
    init {
        require(id.isNotBlank()) { "LibraryEntry id must not be blank" }
        require(workId.isNotBlank()) { "LibraryEntry workId must not be blank" }
        require(editionId.isNotBlank()) { "LibraryEntry editionId must not be blank" }
        require(revision >= 1L) { "LibraryEntry revision must be >= 1" }
    }
}

/**
 * Consolidated reading progress pointer for an [Edition].
 */
data class ReadingProgress(
    val id: String,
    val editionId: String,
    val sourceWorkId: String? = null,
    val chapterExternalId: String? = null,
    val chapterNumber: Double = 0.0,
    val positionMarker: String? = null,
    val sourceUrl: String? = null,
    val origin: ProgressOrigin = ProgressOrigin.MANUAL,
    val lastReadAtUtc: String? = null,
    val updatedAtUtc: String,
    val revision: Long = 1L,
    val deletedAtUtc: String? = null,
) {
    init {
        require(id.isNotBlank()) { "ReadingProgress id must not be blank" }
        require(editionId.isNotBlank()) { "ReadingProgress editionId must not be blank" }
        require(chapterNumber >= 0.0) { "chapterNumber must be >= 0" }
        require(revision >= 1L) { "ReadingProgress revision must be >= 1" }
    }
}

/**
 * Read-model aggregate joining a library entry with its work, edition, and latest progress.
 */
data class LibraryItem(
    val entry: LibraryEntry,
    val work: Work,
    val edition: Edition,
    val progress: ReadingProgress?,
)
