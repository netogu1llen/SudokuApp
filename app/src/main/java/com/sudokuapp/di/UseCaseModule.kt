package com.sudokuapp.di

import com.sudokuapp.domain.repository.SudokuRepository
import com.sudokuapp.domain.usecase.GenerateSudokuUseCase
import com.sudokuapp.domain.usecase.GetSavedGamesUseCase
import com.sudokuapp.domain.usecase.SaveGameUseCase
import com.sudokuapp.domain.usecase.VerifySolutionUseCase
import com.sudokuapp.domain.usecase.VerifySudokuWithApiUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGenerateSudokuUseCase(repository: SudokuRepository): GenerateSudokuUseCase {
        return GenerateSudokuUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetSavedGamesUseCase(repository: SudokuRepository): GetSavedGamesUseCase {
        return GetSavedGamesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveGameUseCase(repository: SudokuRepository): SaveGameUseCase {
        return SaveGameUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideVerifySolutionUseCase(): VerifySolutionUseCase {
        return VerifySolutionUseCase()
    }

    @Provides
    @Singleton
    fun provideVerifySudokuWithApiUseCase(repository: SudokuRepository): VerifySudokuWithApiUseCase {
        return VerifySudokuWithApiUseCase(repository)
    }
}