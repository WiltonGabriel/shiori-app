package dev.shiori.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ProgressOrigin
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat
import dev.shiori.core.domain.repository.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val selectedStatus: ReadingStatus? = null,
    val isAddDialogOpen: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val selectedStatus = MutableStateFlow<ReadingStatus?>(null)
    private val isAddDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(
        selectedStatus.flatMapLatest { filter -> repository.observeLibrary(filter) },
        selectedStatus,
        isAddDialogOpen,
    ) { items, filter, dialogOpen ->
        LibraryUiState(
            items = items,
            selectedStatus = filter,
            isAddDialogOpen = dialogOpen,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LibraryUiState(),
    )

    fun selectStatusFilter(status: ReadingStatus?) {
        selectedStatus.value = status
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
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createEntry(
                title = title,
                format = format,
                totalChapters = totalChapters,
                initialChapter = initialChapter,
            )
            isAddDialogOpen.value = false
        }
    }

    fun incrementChapter(item: LibraryItem) {
        val current = item.progress?.chapterNumber ?: 0.0
        val next = current + 1.0
        viewModelScope.launch {
            repository.updateProgress(
                editionId = item.edition.id,
                chapterNumber = next,
                origin = ProgressOrigin.MANUAL,
            )
        }
    }
}
