package com.sudokuapp.domain.repository

import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import kotlinx.coroutines.flow.Flow

interface SudokuRepository {
    suspend fun generateSudoku(size: SudokuSize, difficulty: SudokuDifficulty): Result<Sudoku>
    suspend fun saveSudoku(sudoku: Sudoku)
    suspend fun getSudokuById(id: String): Sudoku?
    fun getAllSudokus(): Flow<List<Sudoku>>
    fun getInProgressSudokus(): Flow<List<Sudoku>>
    fun getCompletedSudokus(): Flow<List<Sudoku>>
    suspend fun deleteSudoku(id: String)
    suspend fun updateSudokuState(id: String, newState: List<List<Int?>>, isCompleted: Boolean)
}