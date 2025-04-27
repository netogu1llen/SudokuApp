package com.sudokuapp.domain.model

enum class SudokuSize(val dimension: Int) {
    SMALL(4),
    STANDARD(9)
}

enum class SudokuDifficulty(val apiValue: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard")
}

data class Sudoku(
    val id: String = "",
    val puzzle: List<List<Int?>>,
    val solution: List<List<Int>>,
    val currentState: List<List<Int?>>,
    val size: SudokuSize,
    val difficulty: SudokuDifficulty,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

data class SudokuCell(
    val rowIndex: Int,
    val colIndex: Int,
    val value: Int?,
    val isOriginal: Boolean,
    val isValid: Boolean = true,
    val notes: Set<Int> = emptySet()
)

// Utility extension function to get a cell
fun Sudoku.getCell(row: Int, col: Int): SudokuCell {
    val value = currentState[row][col]
    val isOriginal = puzzle[row][col] != null
    val isValid = checkCellValidity(row, col)
    return SudokuCell(row, col, value, isOriginal, isValid)
}

// Utility extension function to check if a cell value is valid
fun Sudoku.checkCellValidity(row: Int, col: Int): Boolean {
    val value = currentState[row][col] ?: return true // Empty cells are valid

    // Check row
    for (c in currentState[row].indices) {
        if (c != col && currentState[row][c] == value) {
            return false
        }
    }

    // Check column
    for (r in currentState.indices) {
        if (r != row && currentState[r][col] == value) {
            return false
        }
    }

    // Check box
    val boxSize = when (size) {
        SudokuSize.SMALL -> 2
        SudokuSize.STANDARD -> 3
    }

    val boxStartRow = (row / boxSize) * boxSize
    val boxStartCol = (col / boxSize) * boxSize

    for (r in boxStartRow until boxStartRow + boxSize) {
        for (c in boxStartCol until boxStartCol + boxSize) {
            if (r != row && c != col && currentState[r][c] == value) {
                return false
            }
        }
    }

    return true
}

// Utility extension function to check if the puzzle is complete and correct
fun Sudoku.isComplete(): Boolean {
    // Check if all cells are filled
    for (row in currentState) {
        for (cell in row) {
            if (cell == null) return false
        }
    }

    // Check if all rows are valid
    for (rowIndex in currentState.indices) {
        for (colIndex in currentState[rowIndex].indices) {
            if (!checkCellValidity(rowIndex, colIndex)) {
                return false
            }
        }
    }

    return true
}