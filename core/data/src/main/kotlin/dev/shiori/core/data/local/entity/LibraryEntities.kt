package dev.shiori.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val originalTitle: String?,
    val format: String,
    val synopsis: String?,
    val tagsJson: String,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long,
    val deletedAtUtc: String?,
)

@Entity(tableName = "editions")
data class EditionEntity(
    @PrimaryKey val id: String,
    val workId: String,
    val language: String,
    val label: String,
    val totalChapters: Int?,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long,
    val deletedAtUtc: String?,
)

@Entity(tableName = "library_entries")
data class LibraryEntryEntity(
    @PrimaryKey val id: String,
    val workId: String,
    val editionId: String,
    val status: String,
    val favorite: Boolean,
    val listsJson: String,
    val notes: String,
    val preferredReadingMode: String,
    val createdAtUtc: String,
    val updatedAtUtc: String,
    val revision: Long,
    val deletedAtUtc: String?,
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: String,
    val editionId: String,
    val sourceWorkId: String?,
    val chapterExternalId: String?,
    val chapterNumber: Double,
    val positionMarker: String?,
    val sourceUrl: String?,
    val origin: String,
    val lastReadAtUtc: String?,
    val updatedAtUtc: String,
    val revision: Long,
    val deletedAtUtc: String?,
)
