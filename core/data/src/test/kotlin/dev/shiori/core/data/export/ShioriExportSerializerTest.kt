package dev.shiori.core.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ShioriExportSerializerTest {

    @Test
    fun `round-trip encode and decode preserves full library payload`() {
        val original = ShioriExportDocument(
            exportedAt = "2026-11-19T23:50:00Z",
            data = ShioriExportPayload(
                works = listOf(
                    WorkDto(
                        id = "work-1",
                        title = "Omniscient Reader",
                        originalTitle = "전지적 독자 시점",
                        format = "MANHWA",
                        tags = listOf("Ação", "Fantasia"),
                        createdAt = "2026-11-19T23:00:00Z",
                        updatedAt = "2026-11-19T23:50:00Z",
                    )
                ),
                editions = listOf(
                    EditionDto(
                        id = "ed-1",
                        workId = "work-1",
                        language = "pt-BR",
                        label = "Scan Padrão",
                        totalChapters = 551,
                        createdAt = "2026-11-19T23:00:00Z",
                        updatedAt = "2026-11-19T23:50:00Z",
                    )
                ),
                libraryEntries = listOf(
                    LibraryEntryDto(
                        id = "entry-1",
                        workId = "work-1",
                        editionId = "ed-1",
                        status = "READING",
                        favorite = true,
                        notes = "Ótimo ritmo",
                        preferredReadingMode = "ADAPTIVE",
                        createdAt = "2026-11-19T23:00:00Z",
                        updatedAt = "2026-11-19T23:50:00Z",
                    )
                ),
                readingProgress = listOf(
                    ReadingProgressDto(
                        id = "prog-1",
                        editionId = "ed-1",
                        chapterNumber = 120.0,
                        positionMarker = "Meio do cap. 120",
                        origin = "MANUAL",
                        lastReadAt = "2026-11-19T23:45:00Z",
                        updatedAt = "2026-11-19T23:50:00Z",
                    )
                ),
            ),
        )

        val encoded = ShioriExportSerializer.encode(original)
        val decoded = ShioriExportSerializer.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `decode rejects unsupported export format`() {
        val invalidJson = """{"format":"unknown","exportedAt":"2026-11-19T00:00:00Z","data":{}}"""
        assertThrows(IllegalArgumentException::class.java) {
            ShioriExportSerializer.decode(invalidJson)
        }
    }
}
