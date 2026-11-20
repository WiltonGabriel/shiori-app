package dev.shiori.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LibraryFilterEngineTest {

    private fun sampleItem(
        id: String,
        title: String,
        originalTitle: String? = null,
        format: WorkFormat = WorkFormat.MANGA,
        status: ReadingStatus = ReadingStatus.READING,
        favorite: Boolean = false,
        tags: List<String> = emptyList(),
        marker: String? = null,
    ): LibraryItem {
        val now = "2026-11-19T23:00:00Z"
        return LibraryItem(
            entry = LibraryEntry(
                id = "entry-$id",
                workId = "work-$id",
                editionId = "edition-$id",
                status = status,
                favorite = favorite,
                createdAtUtc = now,
                updatedAtUtc = now,
            ),
            work = Work(
                id = "work-$id",
                title = title,
                originalTitle = originalTitle,
                format = format,
                tags = tags,
                createdAtUtc = now,
                updatedAtUtc = now,
            ),
            edition = Edition(
                id = "edition-$id",
                workId = "work-$id",
                createdAtUtc = now,
                updatedAtUtc = now,
            ),
            progress = ReadingProgress(
                id = "prog-$id",
                editionId = "edition-$id",
                chapterNumber = 10.0,
                positionMarker = marker,
                updatedAtUtc = now,
            ),
        )
    }

    @Test
    fun `filters by query across title, original title, tags, and marker`() {
        val items = listOf(
            sampleItem("1", "Omniscient Reader", originalTitle = "전지적 독자 시점", format = WorkFormat.MANHWA, tags = listOf("Fantasia")),
            sampleItem("2", "Lord of the Mysteries", format = WorkFormat.NOVEL, marker = "Pág. 142"),
            sampleItem("3", "Vagabond", format = WorkFormat.MANGA, status = ReadingStatus.PAUSED, favorite = true),
        )

        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(query = "독자")).size)
        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(query = "fantasia")).size)
        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(query = "142")).size)
        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(favoritesOnly = true)).size)
        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(format = WorkFormat.NOVEL)).size)
        assertEquals(1, LibraryFilterEngine.apply(items, LibraryFilter(status = ReadingStatus.PAUSED)).size)
    }

    @Test
    fun `update input validates non-blank title and non-negative chapter`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateLibraryItemInput(
                entryId = "entry-1",
                title = "  ",
                format = WorkFormat.MANGA,
                status = ReadingStatus.READING,
                favorite = false,
                preferredReadingMode = ReadingModePreference.ADAPTIVE,
                chapterNumber = 1.0,
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            UpdateLibraryItemInput(
                entryId = "entry-1",
                title = "Valid Title",
                format = WorkFormat.MANGA,
                status = ReadingStatus.READING,
                favorite = false,
                preferredReadingMode = ReadingModePreference.ADAPTIVE,
                chapterNumber = -0.5,
            )
        }
    }
}
