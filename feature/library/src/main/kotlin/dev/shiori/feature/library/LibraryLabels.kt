package dev.shiori.feature.library

import dev.shiori.core.domain.model.ReadingModePreference
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat
import java.net.URI

internal fun WorkFormat.displayLabel(): String = when (this) {
    WorkFormat.MANGA -> "Mangá"
    WorkFormat.MANHWA -> "Manhwa"
    WorkFormat.MANHUA -> "Manhua"
    WorkFormat.WEBTOON -> "Webtoon"
    WorkFormat.COMIC -> "HQ / Comic"
    WorkFormat.NOVEL -> "Novel"
    WorkFormat.LIGHT_NOVEL -> "Light Novel"
    WorkFormat.BOOK -> "Livro"
}

internal fun ReadingStatus.displayLabel(): String = when (this) {
    ReadingStatus.READING -> "Lendo"
    ReadingStatus.PLANNED -> "Planejado"
    ReadingStatus.COMPLETED -> "Concluído"
    ReadingStatus.PAUSED -> "Pausado"
    ReadingStatus.DROPPED -> "Abandonado"
}

internal fun ReadingModePreference.displayLabel(): String = when (this) {
    ReadingModePreference.ADAPTIVE -> "Adaptativo"
    ReadingModePreference.NATIVE_READER -> "Leitor Nativo"
    ReadingModePreference.WEBVIEW -> "WebView"
    ReadingModePreference.EXTERNAL_BROWSER -> "Navegador Externo"
}

internal fun formatChapterNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

internal fun isSafeHttpUrl(rawUrl: String?): Boolean {
    if (rawUrl.isNullOrBlank()) return false
    return runCatching {
        val uri = URI(rawUrl.trim())
        val scheme = uri.scheme?.lowercase()
        (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
