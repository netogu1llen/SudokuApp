package com.sudokuapp.domain.usecase

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.repository.SudokuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedGamesUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    operator fun invoke(onlyInProgress: Boolean = false): Flow<List<Sudoku>> {
        return if (onlyInProgress) {
            repository.getInProgressSudokus()
        } else {
            repository.getAllSudokus()
        }
    }

    suspend fun getSudokuById(id: String): Sudoku? {
        return repository.getSudokuById(id)
    }
}