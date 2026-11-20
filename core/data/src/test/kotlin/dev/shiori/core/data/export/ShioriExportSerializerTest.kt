package dev.shiori.core.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ShioriExportSerializerTest {

    @Test
    fun `round-trip encode and decode preserves library payload`() {
        val original = ShioriExportDocument(
            exportedAt = "2026-11-19T23:50:00Z",
            data = ShioriExportPayload(
                works = listOf(
                    WorkDto(
                        id = "work-1",
                        title = "Omniscient Reader",
                        format = "MANHWA",
                        createdAt = "2026-11-19T23:00:00Z",
                        updatedAt = "2026-11-19T23:50:00Z",
                    )
                )
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
