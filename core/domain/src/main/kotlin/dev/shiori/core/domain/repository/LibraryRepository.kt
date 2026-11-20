package dev.shiori.core.domain.repository

import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ProgressOrigin
import dev.shiori.core.domain.model.ReadingModePreference
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.UpdateLibraryItemInput
import dev.shiori.core.domain.model.WorkFormat
import kotlinx.coroutines.flow.Flow

/**
 * Port for local-first library persistence and portable export/import.
 */
interface LibraryRepository {
    fun observeLibrary(statusFilter: ReadingStatus? = null): Flow<List<LibraryItem>>

    fun observeLibraryItem(entryId: String): Flow<LibraryItem?>

    suspend fun getLibraryItem(entryId: String): LibraryItem?

    suspend fun createEntry(
        title: String,
        originalTitle: String? = null,
        format: WorkFormat,
        language: String = "pt-BR",
        editionLabel: String = "Default",
        totalChapters: Int? = null,
        status: ReadingStatus = ReadingStatus.READING,
        preferredReadingMode: ReadingModePreference = ReadingModePreference.ADAPTIVE,
        initialChapter: Double = 0.0,
        positionMarker: String? = null,
        sourceUrl: String? = null,
    ): LibraryItem

    suspend fun updateEntryDetails(input: UpdateLibraryItemInput)

    suspend fun toggleFavorite(entryId: String, favorite: Boolean)

    suspend fun updateProgress(
        editionId: String,
        chapterNumber: Double,
        positionMarker: String? = null,
        sourceUrl: String? = null,
        origin: ProgressOrigin = ProgressOrigin.MANUAL,
    )

    suspend fun updateStatus(entryId: String, status: ReadingStatus)

    suspend fun softDeleteEntry(entryId: String)

    suspend fun exportToJson(): String

    suspend fun importFromJson(rawJson: String): Int
}
