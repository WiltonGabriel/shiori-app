package dev.shiori.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LibraryModelsTest {

    @Test
    fun `work requires non-blank title and valid revision`() {
        val work = Work(
            id = "work-1",
            title = "Solo Leveling",
            format = WorkFormat.MANHWA,
            createdAtUtc = "2026-11-19T23:00:00Z",
            updatedAtUtc = "2026-11-19T23:00:00Z",
        )
        assertEquals(WorkFormat.MANHWA, work.format)
        assertEquals(1L, work.revision)

        assertThrows(IllegalArgumentException::class.java) {
            Work(
                id = "work-2",
                title = "   ",
                format = WorkFormat.MANGA,
                createdAtUtc = "2026-11-19T23:00:00Z",
                updatedAtUtc = "2026-11-19T23:00:00Z",
            )
        }
    }

    @Test
    fun `reading progress rejects negative chapter number`() {
        assertThrows(IllegalArgumentException::class.java) {
            ReadingProgress(
                id = "prog-1",
                editionId = "ed-1",
                chapterNumber = -1.0,
                updatedAtUtc = "2026-11-19T23:00:00Z",
            )
        }
    }
}
