package dev.shiori.core.domain.model

/**
 * Filter criteria applied to the local library view.
 */
data class LibraryFilter(
    val query: String = "",
    val status: ReadingStatus? = null,
    val format: WorkFormat? = null,
    val favoritesOnly: Boolean = false,
)

/**
 * Payload for updating an existing library item across Work, Edition, LibraryEntry, and Progress.
 */
data class UpdateLibraryItemInput(
    val entryId: String,
    val title: String,
    val originalTitle: String? = null,
    val format: WorkFormat,
    val synopsis: String? = null,
    val tags: List<String> = emptyList(),
    val language: String = "pt-BR",
    val editionLabel: String = "Default",
    val totalChapters: Int? = null,
    val status: ReadingStatus,
    val favorite: Boolean,
    val notes: String = "",
    val preferredReadingMode: ReadingModePreference,
    val chapterNumber: Double,
    val positionMarker: String? = null,
    val sourceUrl: String? = null,
) {
    init {
        require(entryId.isNotBlank()) { "entryId must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(totalChapters == null || totalChapters >= 0) { "totalChapters must be >= 0" }
        require(chapterNumber >= 0.0) { "chapterNumber must be >= 0" }
    }
}

/**
 * Pure domain filter for [LibraryItem] collections.
 */
object LibraryFilterEngine {
    fun apply(items: List<LibraryItem>, filter: LibraryFilter): List<LibraryItem> {
        val normalizedQuery = filter.query.trim().lowercase()
        return items.filter { item ->
            val matchesStatus = filter.status == null || item.entry.status == filter.status
            val matchesFormat = filter.format == null || item.work.format == filter.format
            val matchesFavorite = !filter.favoritesOnly || item.entry.favorite
            val matchesQuery = normalizedQuery.isEmpty() ||
                item.work.title.lowercase().contains(normalizedQuery) ||
                (item.work.originalTitle?.lowercase()?.contains(normalizedQuery) == true) ||
                item.work.tags.any { it.lowercase().contains(normalizedQuery) } ||
                (item.progress?.positionMarker?.lowercase()?.contains(normalizedQuery) == true)

            matchesStatus && matchesFormat && matchesFavorite && matchesQuery
        }
    }
}
