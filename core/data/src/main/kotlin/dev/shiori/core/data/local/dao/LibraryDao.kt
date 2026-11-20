package dev.shiori.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.shiori.core.data.local.entity.EditionEntity
import dev.shiori.core.data.local.entity.LibraryEntryEntity
import dev.shiori.core.data.local.entity.ReadingProgressEntity
import dev.shiori.core.data.local.entity.WorkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library_entries WHERE deletedAtUtc IS NULL ORDER BY updatedAtUtc DESC")
    fun observeActiveEntries(): Flow<List<LibraryEntryEntity>>

    @Query("SELECT * FROM library_entries WHERE deletedAtUtc IS NULL AND status = :status ORDER BY updatedAtUtc DESC")
    fun observeActiveEntriesByStatus(status: String): Flow<List<LibraryEntryEntity>>

    @Query("SELECT * FROM works WHERE deletedAtUtc IS NULL")
    fun observeActiveWorks(): Flow<List<WorkEntity>>

    @Query("SELECT * FROM editions WHERE deletedAtUtc IS NULL")
    fun observeActiveEditions(): Flow<List<EditionEntity>>

    @Query("SELECT * FROM reading_progress WHERE deletedAtUtc IS NULL")
    fun observeActiveProgress(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM library_entries WHERE id = :entryId AND deletedAtUtc IS NULL LIMIT 1")
    suspend fun getEntryById(entryId: String): LibraryEntryEntity?

    @Query("SELECT * FROM works WHERE id = :workId AND deletedAtUtc IS NULL LIMIT 1")
    suspend fun getWorkById(workId: String): WorkEntity?

    @Query("SELECT * FROM editions WHERE id = :editionId AND deletedAtUtc IS NULL LIMIT 1")
    suspend fun getEditionById(editionId: String): EditionEntity?

    @Query("SELECT * FROM reading_progress WHERE editionId = :editionId AND deletedAtUtc IS NULL LIMIT 1")
    suspend fun getProgressByEditionId(editionId: String): ReadingProgressEntity?

    @Query("SELECT * FROM works WHERE deletedAtUtc IS NULL")
    suspend fun getAllActiveWorks(): List<WorkEntity>

    @Query("SELECT * FROM editions WHERE deletedAtUtc IS NULL")
    suspend fun getAllActiveEditions(): List<EditionEntity>

    @Query("SELECT * FROM library_entries WHERE deletedAtUtc IS NULL")
    suspend fun getAllActiveEntries(): List<LibraryEntryEntity>

    @Query("SELECT * FROM reading_progress WHERE deletedAtUtc IS NULL")
    suspend fun getAllActiveProgress(): List<ReadingProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWork(work: WorkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEdition(edition: EditionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: LibraryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)

    @Transaction
    suspend fun insertNewLibraryItem(
        work: WorkEntity,
        edition: EditionEntity,
        entry: LibraryEntryEntity,
        progress: ReadingProgressEntity,
    ) {
        upsertWork(work)
        upsertEdition(edition)
        upsertEntry(entry)
        upsertProgress(progress)
    }

    @Query(
        "UPDATE library_entries SET status = :status, updatedAtUtc = :updatedAtUtc, revision = revision + 1 WHERE id = :entryId AND deletedAtUtc IS NULL"
    )
    suspend fun updateEntryStatus(entryId: String, status: String, updatedAtUtc: String)

    @Query(
        "UPDATE library_entries SET deletedAtUtc = :deletedAtUtc, updatedAtUtc = :deletedAtUtc, revision = revision + 1 WHERE id = :entryId AND deletedAtUtc IS NULL"
    )
    suspend fun softDeleteEntry(entryId: String, deletedAtUtc: String)
}
