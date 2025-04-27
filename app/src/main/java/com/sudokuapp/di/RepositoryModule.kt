package com.sudokuapp.di

import com.sudokuapp.data.repository.SudokuRepositoryImpl
import com.sudokuapp.domain.repository.SudokuRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSudokuRepository(
        repositoryImpl: SudokuRepositoryImpl
    ): SudokuRepository {
        return repositoryImpl
    }
}