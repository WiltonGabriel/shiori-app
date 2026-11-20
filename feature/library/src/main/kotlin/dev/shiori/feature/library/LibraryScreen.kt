package dev.shiori.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Shiori") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddDialog) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            StatusFilterRow(
                selected = state.selectedStatus,
                onSelect = viewModel::selectStatusFilter,
            )

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhuma obra na biblioteca local. Toque em + para adicionar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.entry.id }) { item ->
                        LibraryItemCard(
                            item = item,
                            onIncrementChapter = { viewModel.incrementChapter(item) },
                        )
                    }
                }
            }
        }

        if (state.isAddDialogOpen) {
            AddWorkDialog(
                onDismiss = viewModel::closeAddDialog,
                onConfirm = viewModel::addWork,
            )
        }
    }
}

@Composable
private fun StatusFilterRow(
    selected: ReadingStatus?,
    onSelect: (ReadingStatus?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Todos") },
            )
        }
        items(ReadingStatus.entries) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(status.displayLabel()) },
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    item: LibraryItem,
    onIncrementChapter: () -> Unit,
) {
    val currentChapter = item.progress?.chapterNumber ?: 0.0
    val formattedChapter = if (currentChapter % 1.0 == 0.0) {
        currentChapter.toInt().toString()
    } else {
        currentChapter.toString()
    }
    val totalLabel = item.edition.totalChapters?.let { " / $it" } ?: ""

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.work.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${item.work.format.displayLabel()} • ${item.entry.status.displayLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Cap. $formattedChapter$totalLabel",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedButton(onClick = onIncrementChapter) {
                Text("+1")
            }
        }
    }
}

@Composable
private fun AddWorkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, WorkFormat, Double, Int?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(WorkFormat.MANGA) }
    var chapterText by remember { mutableStateOf("0") }
    var totalText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar obra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WorkFormat.entries) { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.displayLabel()) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chapterText,
                        onValueChange = { chapterText = it },
                        label = { Text("Capítulo atual") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = { Text("Total (opcional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val chapter = chapterText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val total = totalText.toIntOrNull()
                    onConfirm(title, selectedFormat, chapter, total)
                },
                enabled = title.isNotBlank(),
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

private fun WorkFormat.displayLabel(): String = when (this) {
    WorkFormat.MANGA -> "Mangá"
    WorkFormat.MANHWA -> "Manhwa"
    WorkFormat.MANHUA -> "Manhua"
    WorkFormat.WEBTOON -> "Webtoon"
    WorkFormat.COMIC -> "HQ / Comic"
    WorkFormat.NOVEL -> "Novel"
    WorkFormat.LIGHT_NOVEL -> "Light Novel"
    WorkFormat.BOOK -> "Livro"
}

private fun ReadingStatus.displayLabel(): String = when (this) {
    ReadingStatus.READING -> "Lendo"
    ReadingStatus.PLANNED -> "Planejado"
    ReadingStatus.COMPLETED -> "Concluído"
    ReadingStatus.PAUSED -> "Pausado"
    ReadingStatus.DROPPED -> "Abandonado"
}
