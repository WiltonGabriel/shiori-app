package dev.shiori.feature.library

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shiori.core.domain.model.ReadingModePreference
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.WorkFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkDetailsViewModel = hiltViewModel(),
) {
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkDetailsEvent.Message -> snackbarHostState.showSnackbar(event.text)
                WorkDetailsEvent.Deleted -> onBack()
            }
        }
    }

    val currentItem = item
    if (currentItem == null) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Detalhes") },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text("Voltar") }
                    },
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Carregando obra...")
            }
        }
        return
    }

    var title by remember(currentItem.entry.id, currentItem.work.updatedAtUtc) {
        mutableStateOf(currentItem.work.title)
    }
    var originalTitle by remember(currentItem.entry.id, currentItem.work.updatedAtUtc) {
        mutableStateOf(currentItem.work.originalTitle.orEmpty())
    }
    var format by remember(currentItem.entry.id, currentItem.work.updatedAtUtc) {
        mutableStateOf(currentItem.work.format)
    }
    var status by remember(currentItem.entry.id, currentItem.entry.updatedAtUtc) {
        mutableStateOf(currentItem.entry.status)
    }
    var favorite by remember(currentItem.entry.id, currentItem.entry.updatedAtUtc) {
        mutableStateOf(currentItem.entry.favorite)
    }
    var chapterText by remember(currentItem.entry.id, currentItem.progress?.updatedAtUtc) {
        mutableStateOf(formatChapterNumber(currentItem.progress?.chapterNumber ?: 0.0))
    }
    var totalText by remember(currentItem.entry.id, currentItem.edition.updatedAtUtc) {
        mutableStateOf(currentItem.edition.totalChapters?.toString().orEmpty())
    }
    var marker by remember(currentItem.entry.id, currentItem.progress?.updatedAtUtc) {
        mutableStateOf(currentItem.progress?.positionMarker.orEmpty())
    }
    var sourceUrl by remember(currentItem.entry.id, currentItem.progress?.updatedAtUtc) {
        mutableStateOf(currentItem.progress?.sourceUrl.orEmpty())
    }
    var readingMode by remember(currentItem.entry.id, currentItem.entry.updatedAtUtc) {
        mutableStateOf(currentItem.entry.preferredReadingMode)
    }
    var tagsRaw by remember(currentItem.entry.id, currentItem.work.updatedAtUtc) {
        mutableStateOf(currentItem.work.tags.joinToString(", "))
    }
    var synopsis by remember(currentItem.entry.id, currentItem.work.updatedAtUtc) {
        mutableStateOf(currentItem.work.synopsis.orEmpty())
    }
    var notes by remember(currentItem.entry.id, currentItem.entry.updatedAtUtc) {
        mutableStateOf(currentItem.entry.notes)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da obra") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Voltar") }
                },
                actions = {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = originalTitle,
                onValueChange = { originalTitle = it },
                label = { Text("Título original (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Formato", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(WorkFormat.entries) { candidate ->
                    FilterChip(
                        selected = format == candidate,
                        onClick = { format = candidate },
                        label = { Text(candidate.displayLabel()) },
                    )
                }
            }

            Text("Status", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ReadingStatus.entries) { candidate ->
                    FilterChip(
                        selected = status == candidate,
                        onClick = { status = candidate },
                        label = { Text(candidate.displayLabel()) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Favorito", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = favorite, onCheckedChange = { favorite = it })
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
                    label = { Text("Total") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = marker,
                onValueChange = { marker = it },
                label = { Text("Marcador de onde parou") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                label = { Text("URL da fonte") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (isSafeHttpUrl(sourceUrl)) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl.trim()))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Abrir fonte no navegador")
                }
            }

            Text("Modo de leitura preferido", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ReadingModePreference.entries) { mode ->
                    FilterChip(
                        selected = readingMode == mode,
                        onClick = { readingMode = mode },
                        label = { Text(mode.displayLabel()) },
                    )
                }
            }

            OutlinedTextField(
                value = tagsRaw,
                onValueChange = { tagsRaw = it },
                label = { Text("Tags (separadas por vírgula)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = synopsis,
                onValueChange = { synopsis = it },
                label = { Text("Sinopse") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas pessoais") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val chapter = chapterText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val total = totalText.toIntOrNull()
                    viewModel.saveChanges(
                        title = title,
                        originalTitle = originalTitle,
                        format = format,
                        status = status,
                        favorite = favorite,
                        chapterNumber = chapter,
                        totalChapters = total,
                        positionMarker = marker,
                        sourceUrl = sourceUrl,
                        preferredReadingMode = readingMode,
                        tagsRaw = tagsRaw,
                        synopsis = synopsis,
                        notes = notes,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Salvar alterações")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remover da biblioteca?") },
            text = { Text("A obra será removida da sua biblioteca local.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteEntry()
                    },
                ) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}
