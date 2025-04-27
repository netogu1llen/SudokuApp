package com.sudokuapp.domain.usecase

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.isComplete

class VerifySolutionUseCase {
    operator fun invoke(sudoku: Sudoku): Boolean {
        return sudoku.isComplete()
    }

    fun verifyCellValidity(sudoku: Sudoku, rowIndex: Int, colIndex: Int): Boolean {
        // Empty cell is always valid
        val value = sudoku.currentState[rowIndex][colIndex] ?: return true

        // Check row
        for (c in sudoku.currentState[rowIndex].indices) {
            if (c != colIndex && sudoku.currentState[rowIndex][c] == value) {
                return false
            }
        }

        // Check column
        for (r in sudoku.currentState.indices) {
            if (r != rowIndex && sudoku.currentState[r][colIndex] == value) {
                return false
            }
        }

        // Check box
        val boxSize = when (sudoku.size.dimension) {
            4 -> 2
            9 -> 3
            16 -> 4
            else -> throw IllegalArgumentException("Unsupported sudoku size: ${sudoku.size}")
        }

        val boxStartRow = (rowIndex / boxSize) * boxSize
        val boxStartCol = (colIndex / boxSize) * boxSize

        for (r in boxStartRow until boxStartRow + boxSize) {
            for (c in boxStartCol until boxStartCol + boxSize) {
                if (r != rowIndex && c != colIndex && sudoku.currentState[r][c] == value) {
                    return false
                }
            }
        }

        return true
    }
}