package dev.shiori.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shiori.core.domain.model.LibraryFilter
import dev.shiori.core.domain.model.LibraryFilterEngine
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ProgressOrigin
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat
import dev.shiori.core.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val filter: LibraryFilter = LibraryFilter(),
    val isAddDialogOpen: Boolean = false,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val filterState = MutableStateFlow(LibraryFilter())
    private val isAddDialogOpen = MutableStateFlow(false)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeLibrary(statusFilter = null),
        filterState,
        isAddDialogOpen,
    ) { allItems, filter, dialogOpen ->
        LibraryUiState(
            items = LibraryFilterEngine.apply(allItems, filter),
            filter = filter,
            isAddDialogOpen = dialogOpen,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LibraryUiState(),
    )

    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(query = query)
    }

    fun selectStatusFilter(status: ReadingStatus?) {
        filterState.value = filterState.value.copy(status = status)
    }

    fun selectFormatFilter(format: WorkFormat?) {
        filterState.value = filterState.value.copy(format = format)
    }

    fun toggleFavoritesOnly() {
        filterState.value = filterState.value.copy(favoritesOnly = !filterState.value.favoritesOnly)
    }

    fun openAddDialog() {
        isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        isAddDialogOpen.value = false
    }

    fun addWork(
        title: String,
        format: WorkFormat,
        initialChapter: Double,
        totalChapters: Int?,
        positionMarker: String?,
        sourceUrl: String?,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createEntry(
                title = title,
                format = format,
                totalChapters = totalChapters,
                initialChapter = initialChapter,
                positionMarker = positionMarker,
                sourceUrl = sourceUrl,
            )
            isAddDialogOpen.value = false
        }
    }

    fun incrementChapter(item: LibraryItem) {
        val current = item.progress?.chapterNumber ?: 0.0
        viewModelScope.launch {
            repository.updateProgress(
                editionId = item.edition.id,
                chapterNumber = current + 1.0,
                origin = ProgressOrigin.MANUAL,
            )
        }
    }

    fun toggleFavorite(item: LibraryItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.entry.id, !item.entry.favorite)
        }
    }

    suspend fun buildExportJson(): String = repository.exportToJson()

    fun importJson(rawJson: String) {
        viewModelScope.launch {
            runCatching {
                repository.importFromJson(rawJson)
            }.onSuccess { importedCount ->
                _messages.emit("Importação concluída: $importedCount obra(s).")
            }.onFailure { error ->
                _messages.emit("Falha ao importar JSON: ${error.message ?: "formato inválido"}")
            }
        }
    }

    fun notifyMessage(text: String) {
        _messages.tryEmit(text)
    }
}
