package com.sudokuapp.domain.usecase

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.repository.SudokuRepository
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    suspend operator fun invoke(sudoku: Sudoku) {
        repository.saveSudoku(sudoku)
    }

    suspend fun updateGameState(id: String, currentState: List<List<Int?>>, isCompleted: Boolean) {
        repository.updateSudokuState(id, currentState, isCompleted)
    }

    suspend fun deleteGame(id: String) {
        repository.deleteSudoku(id)
    }
}