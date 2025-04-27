package com.sudokuapp.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sudokuapp.data.cache.entity.SudokuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSudoku(sudoku: SudokuEntity)

    @Query("SELECT * FROM sudoku_games WHERE id = :id")
    suspend fun getSudokuById(id: String): SudokuEntity?

    @Query("SELECT * FROM sudoku_games ORDER BY timestamp DESC")
    fun getAllSudokus(): Flow<List<SudokuEntity>>

    @Query("SELECT * FROM sudoku_games WHERE isCompleted = 0 ORDER BY timestamp DESC")
    fun getInProgressSudokus(): Flow<List<SudokuEntity>>

    @Query("SELECT * FROM sudoku_games WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedSudokus(): Flow<List<SudokuEntity>>

    @Query("DELETE FROM sudoku_games WHERE id = :id")
    suspend fun deleteSudoku(id: String)
}