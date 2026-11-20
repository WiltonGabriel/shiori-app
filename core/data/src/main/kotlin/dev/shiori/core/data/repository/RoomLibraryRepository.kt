package dev.shiori.core.data.repository

import dev.shiori.core.data.export.EditionDto
import dev.shiori.core.data.export.LibraryEntryDto
import dev.shiori.core.data.export.ReadingProgressDto
import dev.shiori.core.data.export.ShioriExportDocument
import dev.shiori.core.data.export.ShioriExportPayload
import dev.shiori.core.data.export.ShioriExportSerializer
import dev.shiori.core.data.export.WorkDto
import dev.shiori.core.data.local.dao.LibraryDao
import dev.shiori.core.data.local.entity.EditionEntity
import dev.shiori.core.data.local.entity.LibraryEntryEntity
import dev.shiori.core.data.local.entity.ReadingProgressEntity
import dev.shiori.core.data.local.entity.WorkEntity
import dev.shiori.core.domain.model.Edition
import dev.shiori.core.domain.model.LibraryEntry
import dev.shiori.core.domain.model.LibraryItem
import dev.shiori.core.domain.model.ProgressOrigin
import dev.shiori.core.domain.model.ReadingModePreference
import dev.shiori.core.domain.model.ReadingProgress
import dev.shiori.core.domain.model.ReadingStatus
import dev.shiori.core.domain.model.Work
import dev.shiori.core.domain.model.WorkFormat
import dev.shiori.core.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RoomLibraryRepository @Inject constructor(
    private val dao: LibraryDao,
) : LibraryRepository {

    private val listJson = Json { encodeDefaults = true }

    override fun observeLibrary(statusFilter: ReadingStatus?): Flow<List<LibraryItem>> {
        val entriesFlow = if (statusFilter == null) {
            dao.observeActiveEntries()
        } else {
            dao.observeActiveEntriesByStatus(statusFilter.name)
        }

        return combine(
            entriesFlow,
            dao.observeActiveWorks(),
            dao.observeActiveEditions(),
            dao.observeActiveProgress(),
        ) { entries, works, editions, progresses ->
            val workMap = works.associateBy { it.id }
            val editionMap = editions.associateBy { it.id }
            val progressMap = progresses.associateBy { it.editionId }

            entries.mapNotNull { entry ->
                val work = workMap[entry.workId] ?: return@mapNotNull null
                val edition = editionMap[entry.editionId] ?: return@mapNotNull null
                val progress = progressMap[entry.editionId]
                LibraryItem(
                    entry = entry.toDomain(),
                    work = work.toDomain(),
                    edition = edition.toDomain(),
                    progress = progress?.toDomain(),
                )
            }
        }
    }

    override suspend fun getLibraryItem(entryId: String): LibraryItem? {
        val entry = dao.getEntryById(entryId) ?: return null
        val work = dao.getWorkById(entry.workId) ?: return null
        val edition = dao.getEditionById(entry.editionId) ?: return null
        val progress = dao.getProgressByEditionId(entry.editionId)
        return LibraryItem(
            entry = entry.toDomain(),
            work = work.toDomain(),
            edition = edition.toDomain(),
            progress = progress?.toDomain(),
        )
    }

    override suspend fun createEntry(
        title: String,
        originalTitle: String?,
        format: WorkFormat,
        language: String,
        editionLabel: String,
        totalChapters: Int?,
        status: ReadingStatus,
        preferredReadingMode: ReadingModePreference,
        initialChapter: Double,
        positionMarker: String?,
        sourceUrl: String?,
    ): LibraryItem {
        val now = Instant.now().toString()
        val workId = UUID.randomUUID().toString()
        val editionId = UUID.randomUUID().toString()
        val entryId = UUID.randomUUID().toString()
        val progressId = UUID.randomUUID().toString()

        val workEntity = WorkEntity(
            id = workId,
            title = title.trim(),
            originalTitle = originalTitle?.trim()?.takeIf { it.isNotEmpty() },
            format = format.name,
            synopsis = null,
            tagsJson = "[]",
            createdAtUtc = now,
            updatedAtUtc = now,
            revision = 1L,
            deletedAtUtc = null,
        )
        val editionEntity = EditionEntity(
            id = editionId,
            workId = workId,
            language = language,
            label = editionLabel,
            totalChapters = totalChapters,
            createdAtUtc = now,
            updatedAtUtc = now,
            revision = 1L,
            deletedAtUtc = null,
        )
        val entryEntity = LibraryEntryEntity(
            id = entryId,
            workId = workId,
            editionId = editionId,
            status = status.name,
            favorite = false,
            listsJson = "[]",
            notes = "",
            preferredReadingMode = preferredReadingMode.name,
            createdAtUtc = now,
            updatedAtUtc = now,
            revision = 1L,
            deletedAtUtc = null,
        )
        val progressEntity = ReadingProgressEntity(
            id = progressId,
            editionId = editionId,
            sourceWorkId = null,
            chapterExternalId = null,
            chapterNumber = initialChapter.coerceAtLeast(0.0),
            positionMarker = positionMarker?.trim()?.takeIf { it.isNotEmpty() },
            sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotEmpty() },
            origin = ProgressOrigin.MANUAL.name,
            lastReadAtUtc = if (initialChapter > 0.0) now else null,
            updatedAtUtc = now,
            revision = 1L,
            deletedAtUtc = null,
        )

        dao.insertNewLibraryItem(workEntity, editionEntity, entryEntity, progressEntity)

        return LibraryItem(
            entry = entryEntity.toDomain(),
            work = workEntity.toDomain(),
            edition = editionEntity.toDomain(),
            progress = progressEntity.toDomain(),
        )
    }

    override suspend fun updateProgress(
        editionId: String,
        chapterNumber: Double,
        positionMarker: String?,
        sourceUrl: String?,
        origin: ProgressOrigin,
    ) {
        val now = Instant.now().toString()
        val existing = dao.getProgressByEditionId(editionId)
        val updated = if (existing != null) {
            existing.copy(
                chapterNumber = chapterNumber.coerceAtLeast(0.0),
                positionMarker = positionMarker?.trim()?.takeIf { it.isNotEmpty() },
                sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotEmpty() } ?: existing.sourceUrl,
                origin = origin.name,
                lastReadAtUtc = now,
                updatedAtUtc = now,
                revision = existing.revision + 1L,
            )
        } else {
            ReadingProgressEntity(
                id = UUID.randomUUID().toString(),
                editionId = editionId,
                sourceWorkId = null,
                chapterExternalId = null,
                chapterNumber = chapterNumber.coerceAtLeast(0.0),
                positionMarker = positionMarker?.trim()?.takeIf { it.isNotEmpty() },
                sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotEmpty() },
                origin = origin.name,
                lastReadAtUtc = now,
                updatedAtUtc = now,
                revision = 1L,
                deletedAtUtc = null,
            )
        }
        dao.upsertProgress(updated)
    }

    override suspend fun updateStatus(entryId: String, status: ReadingStatus) {
        dao.updateEntryStatus(entryId, status.name, Instant.now().toString())
    }

    override suspend fun softDeleteEntry(entryId: String) {
        dao.softDeleteEntry(entryId, Instant.now().toString())
    }

    override suspend fun exportToJson(): String {
        val works = dao.getAllActiveWorks().map { entity ->
            val domain = entity.toDomain()
            WorkDto(
                id = domain.id,
                title = domain.title,
                originalTitle = domain.originalTitle,
                format = domain.format.name,
                synopsis = domain.synopsis,
                tags = domain.tags,
                createdAt = domain.createdAtUtc,
                updatedAt = domain.updatedAtUtc,
                revision = domain.revision,
            )
        }
        val editions = dao.getAllActiveEditions().map { entity ->
            EditionDto(
                id = entity.id,
                workId = entity.workId,
                language = entity.language,
                label = entity.label,
                totalChapters = entity.totalChapters,
                createdAt = entity.createdAtUtc,
                updatedAt = entity.updatedAtUtc,
                revision = entity.revision,
            )
        }
        val entries = dao.getAllActiveEntries().map { entity ->
            val domain = entity.toDomain()
            LibraryEntryDto(
                id = domain.id,
                workId = domain.workId,
                editionId = domain.editionId,
                status = domain.status.name,
                favorite = domain.favorite,
                lists = domain.lists,
                notes = domain.notes,
                preferredReadingMode = domain.preferredReadingMode.name,
                createdAt = domain.createdAtUtc,
                updatedAt = domain.updatedAtUtc,
                revision = domain.revision,
            )
        }
        val progresses = dao.getAllActiveProgress().map { entity ->
            ReadingProgressDto(
                id = entity.id,
                editionId = entity.editionId,
                sourceWorkId = entity.sourceWorkId,
                chapterExternalId = entity.chapterExternalId,
                chapterNumber = entity.chapterNumber,
                positionMarker = entity.positionMarker,
                sourceUrl = entity.sourceUrl,
                origin = entity.origin,
                lastReadAt = entity.lastReadAtUtc,
                updatedAt = entity.updatedAtUtc,
                revision = entity.revision,
            )
        }

        val document = ShioriExportDocument(
            exportedAt = Instant.now().toString(),
            data = ShioriExportPayload(
                works = works,
                editions = editions,
                libraryEntries = entries,
                readingProgress = progresses,
            ),
        )
        return ShioriExportSerializer.encode(document)
    }

    override suspend fun importFromJson(rawJson: String): Int {
        val document = ShioriExportSerializer.decode(rawJson)
        val payload = document.data

        payload.works.forEach { dto ->
            dao.upsertWork(
                WorkEntity(
                    id = dto.id,
                    title = dto.title,
                    originalTitle = dto.originalTitle,
                    format = dto.format,
                    synopsis = dto.synopsis,
                    tagsJson = listJson.encodeToString(dto.tags),
                    createdAtUtc = dto.createdAt,
                    updatedAtUtc = dto.updatedAt,
                    revision = dto.revision,
                    deletedAtUtc = null,
                )
            )
        }
        payload.editions.forEach { dto ->
            dao.upsertEdition(
                EditionEntity(
                    id = dto.id,
                    workId = dto.workId,
                    language = dto.language,
                    label = dto.label,
                    totalChapters = dto.totalChapters,
                    createdAtUtc = dto.createdAt,
                    updatedAtUtc = dto.updatedAt,
                    revision = dto.revision,
                    deletedAtUtc = null,
                )
            )
        }
        payload.libraryEntries.forEach { dto ->
            dao.upsertEntry(
                LibraryEntryEntity(
                    id = dto.id,
                    workId = dto.workId,
                    editionId = dto.editionId,
                    status = dto.status,
                    favorite = dto.favorite,
                    listsJson = listJson.encodeToString(dto.lists),
                    notes = dto.notes,
                    preferredReadingMode = dto.preferredReadingMode,
                    createdAtUtc = dto.createdAt,
                    updatedAtUtc = dto.updatedAt,
                    revision = dto.revision,
                    deletedAtUtc = null,
                )
            )
        }
        payload.readingProgress.forEach { dto ->
            dao.upsertProgress(
                ReadingProgressEntity(
                    id = dto.id,
                    editionId = dto.editionId,
                    sourceWorkId = dto.sourceWorkId,
                    chapterExternalId = dto.chapterExternalId,
                    chapterNumber = dto.chapterNumber,
                    positionMarker = dto.positionMarker,
                    sourceUrl = dto.sourceUrl,
                    origin = dto.origin,
                    lastReadAtUtc = dto.lastReadAt,
                    updatedAtUtc = dto.updatedAt,
                    revision = dto.revision,
                    deletedAtUtc = null,
                )
            )
        }

        return payload.libraryEntries.size
    }

    private fun WorkEntity.toDomain(): Work = Work(
        id = id,
        title = title,
        originalTitle = originalTitle,
        format = runCatching { WorkFormat.valueOf(format) }.getOrDefault(WorkFormat.BOOK),
        synopsis = synopsis,
        tags = runCatching { listJson.decodeFromString<List<String>>(tagsJson) }.getOrDefault(emptyList()),
        createdAtUtc = createdAtUtc,
        updatedAtUtc = updatedAtUtc,
        revision = revision,
        deletedAtUtc = deletedAtUtc,
    )

    private fun EditionEntity.toDomain(): Edition = Edition(
        id = id,
        workId = workId,
        language = language,
        label = label,
        totalChapters = totalChapters,
        createdAtUtc = createdAtUtc,
        updatedAtUtc = updatedAtUtc,
        revision = revision,
        deletedAtUtc = deletedAtUtc,
    )

    private fun LibraryEntryEntity.toDomain(): LibraryEntry = LibraryEntry(
        id = id,
        workId = workId,
        editionId = editionId,
        status = runCatching { ReadingStatus.valueOf(status) }.getOrDefault(ReadingStatus.READING),
        favorite = favorite,
        lists = runCatching { listJson.decodeFromString<List<String>>(listsJson) }.getOrDefault(emptyList()),
        notes = notes,
        preferredReadingMode = runCatching {
            ReadingModePreference.valueOf(preferredReadingMode)
        }.getOrDefault(ReadingModePreference.ADAPTIVE),
        createdAtUtc = createdAtUtc,
        updatedAtUtc = updatedAtUtc,
        revision = revision,
        deletedAtUtc = deletedAtUtc,
    )

    private fun ReadingProgressEntity.toDomain(): ReadingProgress = ReadingProgress(
        id = id,
        editionId = editionId,
        sourceWorkId = sourceWorkId,
        chapterExternalId = chapterExternalId,
        chapterNumber = chapterNumber,
        positionMarker = positionMarker,
        sourceUrl = sourceUrl,
        origin = runCatching { ProgressOrigin.valueOf(origin) }.getOrDefault(ProgressOrigin.MANUAL),
        lastReadAtUtc = lastReadAtUtc,
        updatedAtUtc = updatedAtUtc,
        revision = revision,
        deletedAtUtc = deletedAtUtc,
    )
}
