package dev.shiori.core.domain.model

/**
 * Supported literary and visual reading formats in Shiori.
 */
enum class WorkFormat {
    MANGA,
    MANHWA,
    MANHUA,
    WEBTOON,
    COMIC,
    NOVEL,
    LIGHT_NOVEL,
    BOOK,
}

enum class ReadingStatus {
    READING,
    PLANNED,
    COMPLETED,
    PAUSED,
    DROPPED,
}

enum class ReadingModePreference {
    ADAPTIVE,
    NATIVE_READER,
    WEBVIEW,
    EXTERNAL_BROWSER,
}

enum class ProgressOrigin {
    MANUAL,
    WEBVIEW,
    NATIVE_READER,
    IMPORT,
    SYNC,
}
