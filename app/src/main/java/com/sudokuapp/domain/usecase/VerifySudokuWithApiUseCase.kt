package com.sudokuapp.domain.usecase

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.VerificationResult
import com.sudokuapp.domain.repository.SudokuRepository
import javax.inject.Inject

class VerifySudokuWithApiUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    suspend operator fun invoke(sudoku: Sudoku): Result<VerificationResult> {
        // Verificar primero si el tablero está completo
        val allCellsFilled = sudoku.currentState.all { row -> row.all { cell -> cell != null } }

        if (!allCellsFilled) {
            return Result.failure(IllegalStateException("El tablero no está completo"))
        }

        // Llamar a la API para verificar la solución
        return repository.verifySudokuWithApi(sudoku)
    }
}