package dev.shiori.feature.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val payload = viewModel.buildExportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(payload.toByteArray(Charsets.UTF_8))
                    } ?: error("Não foi possível abrir o destino de exportação.")
                }
            }.onSuccess {
                viewModel.notifyMessage("Biblioteca exportada em JSON.")
            }.onFailure { error ->
                viewModel.notifyMessage("Erro ao exportar: ${error.message ?: "falha desconhecida"}")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    } ?: error("Não foi possível ler o arquivo selecionado.")
                }
            }.onSuccess { rawJson ->
                viewModel.importJson(rawJson)
            }.onFailure { error ->
                viewModel.notifyMessage("Erro ao ler arquivo: ${error.message ?: "falha desconhecida"}")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Shiori") },
                actions = {
                    TextButton(onClick = { exportLauncher.launch("shiori-library.json") }) {
                        Text("Exportar")
                    }
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                        Text("Importar")
                    }
                },
            )
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
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Buscar por título, tag ou marcador") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            StatusFilterRow(
                selected = state.filter.status,
                favoritesOnly = state.filter.favoritesOnly,
                onSelectStatus = viewModel::selectStatusFilter,
                onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
            )

            FormatFilterRow(
                selected = state.filter.format,
                onSelectFormat = viewModel::selectFormatFilter,
            )

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhuma obra encontrada. Toque em + para adicionar.",
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
                            onOpenDetails = { onOpenDetails(item.entry.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) },
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
    favoritesOnly: Boolean,
    onSelectStatus: (ReadingStatus?) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = favoritesOnly,
                onClick = onToggleFavoritesOnly,
                label = { Text("Favoritos") },
            )
        }
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelectStatus(null) },
                label = { Text("Todos") },
            )
        }
        items(ReadingStatus.entries) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelectStatus(status) },
                label = { Text(status.displayLabel()) },
            )
        }
    }
}

@Composable
private fun FormatFilterRow(
    selected: WorkFormat?,
    onSelectFormat: (WorkFormat?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelectFormat(null) },
                label = { Text("Todos os formatos") },
            )
        }
        items(WorkFormat.entries) { format ->
            FilterChip(
                selected = selected == format,
                onClick = { onSelectFormat(if (selected == format) null else format) },
                label = { Text(format.displayLabel()) },
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    item: LibraryItem,
    onOpenDetails: () -> Unit,
    onToggleFavorite: () -> Unit,
    onIncrementChapter: () -> Unit,
) {
    val currentChapter = item.progress?.chapterNumber ?: 0.0
    val formattedChapter = formatChapterNumber(currentChapter)
    val totalLabel = item.edition.totalChapters?.let { " / $it" } ?: ""
    val marker = item.progress?.positionMarker

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
    ) {
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
                    text = "Cap. $formattedChapter$totalLabel" + (marker?.let { " — $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onToggleFavorite) {
                    Text(if (item.entry.favorite) "★" else "☆")
                }
                OutlinedButton(onClick = onIncrementChapter) {
                    Text("+1")
                }
            }
        }
    }
}

@Composable
private fun AddWorkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, WorkFormat, Double, Int?, String?, String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(WorkFormat.MANGA) }
    var chapterText by remember { mutableStateOf("0") }
    var totalText by remember { mutableStateOf("") }
    var markerText by remember { mutableStateOf("") }
    var sourceUrlText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar obra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        label = { Text("Cap. atual") },
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
                OutlinedTextField(
                    value = markerText,
                    onValueChange = { markerText = it },
                    label = { Text("Onde parou (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = sourceUrlText,
                    onValueChange = { sourceUrlText = it },
                    label = { Text("URL da fonte (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val chapter = chapterText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val total = totalText.toIntOrNull()
                    onConfirm(
                        title,
                        selectedFormat,
                        chapter,
                        total,
                        markerText.ifBlank { null },
                        sourceUrlText.ifBlank { null },
                    )
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
