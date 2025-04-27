package com.sudokuapp.domain.usecase

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.repository.SudokuRepository
import javax.inject.Inject

class GenerateSudokuUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    suspend operator fun invoke(size: SudokuSize, difficulty: SudokuDifficulty): Result<Sudoku> {
        return repository.generateSudoku(size, difficulty)
    }
}