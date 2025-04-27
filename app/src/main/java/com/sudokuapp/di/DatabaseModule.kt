package com.sudokuapp.di

import android.content.Context
import com.sudokuapp.data.cache.SudokuDatabase
import com.sudokuapp.data.cache.dao.SudokuDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSudokuDatabase(@ApplicationContext context: Context): SudokuDatabase {
        return SudokuDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSudokuDao(database: SudokuDatabase): SudokuDao {
        return database.sudokuDao()
    }
}