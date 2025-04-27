

package com.sudokuapp.data.repository

import com.sudokuapp.data.api.SudokuApiService
import com.sudokuapp.data.cache.dao.SudokuDao
import com.sudokuapp.data.cache.entity.SudokuEntity
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.repository.SudokuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuRepositoryImpl @Inject constructor(
    private val apiService: SudokuApiService,
    private val sudokuDao: SudokuDao,
    private val apiKey: String
) : SudokuRepository {

    override suspend fun generateSudoku(size: SudokuSize, difficulty: SudokuDifficulty): Result<Sudoku> {
        return try {
            val dimension = size.dimension
            val response = apiService.generateSudoku(
                apiKey = apiKey,
                difficulty = difficulty.apiValue,
                width = dimension / 2,  // API requires width/height for box dimensions
                height = dimension / 2
            )

            val sudoku = Sudoku(
                id = UUID.randomUUID().toString(),
                puzzle = response.puzzle,
                solution = response.solution,
                currentState = response.puzzle.map { it.toMutableList() },
                size = size,
                difficulty = difficulty
            )

            // Cache the sudoku
            saveSudoku(sudoku)

            Result.success(sudoku)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSudoku(sudoku: Sudoku) {
        val entity = SudokuEntity(
            id = sudoku.id,
            puzzle = sudoku.puzzle,
            solution = sudoku.solution,
            currentState = sudoku.currentState,
            size = sudoku.size,
            difficulty = sudoku.difficulty,
            timestamp = sudoku.timestamp,
            isCompleted = sudoku.isCompleted
        )
        sudokuDao.saveSudoku(entity)
    }

    override suspend fun getSudokuById(id: String): Sudoku? {
        val entity = sudokuDao.getSudokuById(id) ?: return null
        return mapEntityToDomain(entity)
    }

    override fun getAllSudokus(): Flow<List<Sudoku>> {
        return sudokuDao.getAllSudokus().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override fun getInProgressSudokus(): Flow<List<Sudoku>> {
        return sudokuDao.getInProgressSudokus().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override fun getCompletedSudokus(): Flow<List<Sudoku>> {
        return sudokuDao.getCompletedSudokus().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override suspend fun deleteSudoku(id: String) {
        sudokuDao.deleteSudoku(id)
    }

    override suspend fun updateSudokuState(id: String, newState: List<List<Int?>>, isCompleted: Boolean) {
        val sudoku = sudokuDao.getSudokuById(id) ?: return
        val updatedSudoku = sudoku.copy(
            currentState = newState,
            isCompleted = isCompleted
        )
        sudokuDao.saveSudoku(updatedSudoku)
    }

    private fun mapEntityToDomain(entity: SudokuEntity): Sudoku {
        return Sudoku(
            id = entity.id,
            puzzle = entity.puzzle,
            solution = entity.solution,
            currentState = entity.currentState,
            size = entity.size,
            difficulty = entity.difficulty,
            timestamp = entity.timestamp,
            isCompleted = entity.isCompleted
        )
    }
}