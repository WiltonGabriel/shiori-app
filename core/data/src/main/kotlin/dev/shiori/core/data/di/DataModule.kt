package dev.shiori.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.shiori.core.data.local.ShioriDatabase
import dev.shiori.core.data.local.dao.LibraryDao
import dev.shiori.core.data.repository.RoomLibraryRepository
import dev.shiori.core.domain.repository.LibraryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: RoomLibraryRepository): LibraryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvideModule {
    @Provides
    @Singleton
    fun provideShioriDatabase(@ApplicationContext context: Context): ShioriDatabase =
        Room.databaseBuilder(
            context,
            ShioriDatabase::class.java,
            "shiori_local.db",
        ).build()

    @Provides
    fun provideLibraryDao(database: ShioriDatabase): LibraryDao = database.libraryDao()
}
