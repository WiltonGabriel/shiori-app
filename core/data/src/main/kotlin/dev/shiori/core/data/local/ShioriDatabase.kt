package dev.shiori.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.shiori.core.data.local.dao.LibraryDao
import dev.shiori.core.data.local.entity.EditionEntity
import dev.shiori.core.data.local.entity.LibraryEntryEntity
import dev.shiori.core.data.local.entity.ReadingProgressEntity
import dev.shiori.core.data.local.entity.WorkEntity

@Database(
    entities = [
        WorkEntity::class,
        EditionEntity::class,
        LibraryEntryEntity::class,
        ReadingProgressEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ShioriDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
