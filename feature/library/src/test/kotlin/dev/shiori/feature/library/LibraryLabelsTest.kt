package dev.shiori.feature.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryLabelsTest {

    @Test
    fun `formatChapterNumber drops trailing zero for whole chapters`() {
        assertEquals("12", formatChapterNumber(12.0))
        assertEquals("12.5", formatChapterNumber(12.5))
    }

    @Test
    fun `isSafeHttpUrl accepts http and https and rejects unsafe schemes`() {
        assertTrue(isSafeHttpUrl("https://example.org/manga/1"))
        assertTrue(isSafeHttpUrl("http://localhost:8080/read"))
        assertFalse(isSafeHttpUrl("javascript:alert(1)"))
        assertFalse(isSafeHttpUrl("file:///etc/passwd"))
        assertFalse(isSafeHttpUrl("not a url"))
        assertFalse(isSafeHttpUrl(null))
    }
}
