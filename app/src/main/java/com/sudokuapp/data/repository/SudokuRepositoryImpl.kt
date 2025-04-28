package com.sudokuapp.data.repository

import com.sudokuapp.data.api.SudokuApiService
import com.sudokuapp.data.cache.dao.SudokuDao
import com.sudokuapp.data.cache.entity.SudokuEntity
import com.sudokuapp.data.model.SudokuApiResponse
import com.sudokuapp.data.model.SudokuVerificationRequest
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.model.VerificationResult
import com.sudokuapp.domain.repository.SudokuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

            try {
                // Intentar obtener el sudoku de la API
                val response = apiService.generateSudoku(
                    apiKey = apiKey,
                    difficulty = difficulty.apiValue,
                    width = dimension / 2,  // API requires width/height for box dimensions
                    height = dimension / 2
                )

                createAndSaveSudoku(response, size, difficulty)
            } catch (e: Exception) {
                // Creación en caso de falla
                val localSudoku = generateLocalSudoku(size, difficulty)
                Result.success(localSudoku)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createAndSaveSudoku(
        response: SudokuApiResponse,
        size: SudokuSize,
        difficulty: SudokuDifficulty
    ): Result<Sudoku> {
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

        return Result.success(sudoku)
    }

    // Función para generar un sudoku localmente cuando la API falla
    private suspend fun generateLocalSudoku(size: SudokuSize, difficulty: SudokuDifficulty): Sudoku {
        return withContext(Dispatchers.Default) {
            // Generamos un sudoku simple según el tamaño
            val dimension = size.dimension

            // Para un sudoku 4x4
            val puzzle4x4 = listOf(
                listOf(1, null, null, 4),
                listOf(null, 4, 1, null),
                listOf(null, 1, 4, null),
                listOf(4, null, null, 1)
            )

            // Para un sudoku 9x9
            val puzzle9x9 = listOf(
                listOf(5, 3, null, null, 7, null, null, null, null),
                listOf(6, null, null, 1, 9, 5, null, null, null),
                listOf(null, 9, 8, null, null, null, null, 6, null),
                listOf(8, null, null, null, 6, null, null, null, 3),
                listOf(4, null, null, 8, null, 3, null, null, 1),
                listOf(7, null, null, null, 2, null, null, null, 6),
                listOf(null, 6, null, null, null, null, 2, 8, null),
                listOf(null, null, null, 4, 1, 9, null, null, 5),
                listOf(null, null, null, null, 8, null, null, 7, 9)
            )

            // Para un sudoku 4x4
            val solution4x4 = listOf(
                listOf(1, 2, 3, 4),
                listOf(3, 4, 1, 2),
                listOf(2, 1, 4, 3),
                listOf(4, 3, 2, 1)
            )

            // Para un sudoku 9x9
            val solution9x9 = listOf(
                listOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
                listOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
                listOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
                listOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
                listOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
                listOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
                listOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
                listOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
                listOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
            )

            // Seleccionar el puzzle y la solución según el tamaño
            val (puzzle, solution) = when (size) {
                SudokuSize.SMALL -> Pair(puzzle4x4, solution4x4)
                SudokuSize.STANDARD -> Pair(puzzle9x9, solution9x9)
            }

            val sudoku = Sudoku(
                id = UUID.randomUUID().toString(),
                puzzle = puzzle,
                solution = solution,
                currentState = puzzle.map { it.toMutableList() },
                size = size,
                difficulty = difficulty
            )

            // Cache the sudoku
            saveSudoku(sudoku)

            sudoku
        }
    }

    override suspend fun verifySudokuWithApi(sudoku: Sudoku): Result<VerificationResult> {
        return try {
            try {
                // Intentar usar la API primero
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
                // Si la API falla, verificamos contra la solución original guardada
                val isCorrect = isUserSolutionCorrect(sudoku.currentState, sudoku.solution)

                val result = VerificationResult(
                    isValid = isCorrect,
                    solution = sudoku.solution,
                    errorMessage = if (isCorrect) null else "Tu solución no coincide con la solución correcta. (Verificación local)"
                )

                // Si es correcto, actualizamos el estado del sudoku
                if (isCorrect) {
                    updateSudokuState(sudoku.id, sudoku.currentState, true)
                }

                Result.success(result)
            }
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