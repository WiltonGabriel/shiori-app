package dev.shiori.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ReadingModePreference
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.UpdateLibraryItemInput
import dev.shiori.core.domain.model.WorkFormat
import dev.shiori.core.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
) : ViewModel() {

    private val entryId: String = checkNotNull(savedStateHandle["entryId"])

    val item: StateFlow<LibraryItem?> = repository.observeLibraryItem(entryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null,
        )

    private val _events = MutableSharedFlow<WorkDetailsEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<WorkDetailsEvent> = _events.asSharedFlow()

    fun saveChanges(
        title: String,
        originalTitle: String,
        format: WorkFormat,
        status: ReadingStatus,
        favorite: Boolean,
        chapterNumber: Double,
        totalChapters: Int?,
        positionMarker: String,
        sourceUrl: String,
        preferredReadingMode: ReadingModePreference,
        tagsRaw: String,
        synopsis: String,
        notes: String,
    ) {
        if (title.isBlank()) {
            _events.tryEmit(WorkDetailsEvent.Message("O título é obrigatório."))
            return
        }
        val tags = tagsRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        viewModelScope.launch {
            repository.updateEntryDetails(
                UpdateLibraryItemInput(
                    entryId = entryId,
                    title = title,
                    originalTitle = originalTitle.ifBlank { null },
                    format = format,
                    synopsis = synopsis.ifBlank { null },
                    tags = tags,
                    totalChapters = totalChapters,
                    status = status,
                    favorite = favorite,
                    notes = notes,
                    preferredReadingMode = preferredReadingMode,
                    chapterNumber = chapterNumber.coerceAtLeast(0.0),
                    positionMarker = positionMarker.ifBlank { null },
                    sourceUrl = sourceUrl.ifBlank { null },
                )
            )
            _events.emit(WorkDetailsEvent.Message("Alterações salvas."))
        }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            repository.softDeleteEntry(entryId)
            _events.emit(WorkDetailsEvent.Deleted)
        }
    }
}

sealed interface WorkDetailsEvent {
    data class Message(val text: String) : WorkDetailsEvent
    data object Deleted : WorkDetailsEvent
}
