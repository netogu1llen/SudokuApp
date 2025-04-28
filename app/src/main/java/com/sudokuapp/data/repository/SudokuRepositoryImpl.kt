package com.sudokuapp.data.repository

import com.sudokuapp.data.api.SudokuApiService
import com.sudokuapp.data.cache.dao.SudokuDao
import com.sudokuapp.data.cache.entity.SudokuEntity
import com.sudokuapp.data.model.SudokuVerificationRequest
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.model.VerificationResult
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

    override suspend fun verifySudokuWithApi(sudoku: Sudoku): Result<VerificationResult> {
        return try {
            // Enviar el puzzle original (no la solución del usuario) para que la API lo resuelva
            val request = SudokuVerificationRequest(board = sudoku.puzzle)

            // Llamamos a la API para resolver el puzzle original
            val response = apiService.verifySudokuSolution(apiKey, request)

            // Verificamos si la solución de la API es resoluble
            val isResolvable = response.solvable

            if (!isResolvable) {
                return Result.success(
                    VerificationResult(
                        isValid = false,
                        solution = null,
                        errorMessage = "La API indica que el puzzle no tiene solución."
                    )
                )
            }

            // Comparamos la solución del usuario con la solución de la API
            val userSolution = sudoku.currentState
            val apiSolution = response.solution

            val isCorrect = isUserSolutionCorrect(userSolution, apiSolution)

            val result = VerificationResult(
                isValid = isCorrect,
                solution = apiSolution,
                errorMessage = if (isCorrect) null else "Tu solución no coincide con la solución de la API."
            )

            // Si es correcto, actualizamos el estado del sudoku
            if (isCorrect) {
                updateSudokuState(sudoku.id, sudoku.currentState, true)
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isUserSolutionCorrect(userSolution: List<List<Int?>>, apiSolution: List<List<Int>>): Boolean {
        // Verificamos que estén completos
        for (row in userSolution) {
            for (cell in row) {
                if (cell == null) return false
            }
        }

        // Comparamos cada celda
        for (i in userSolution.indices) {
            for (j in userSolution[i].indices) {
                if (userSolution[i][j] != apiSolution[i][j]) {
                    return false
                }
            }
        }

        return true
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