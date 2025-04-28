package com.sudokuapp.presentation.screens.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuCell
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.model.VerificationResult
import com.sudokuapp.domain.usecase.GenerateSudokuUseCase
import com.sudokuapp.domain.usecase.GetSavedGamesUseCase
import com.sudokuapp.domain.usecase.SaveGameUseCase
import com.sudokuapp.domain.usecase.VerifySolutionUseCase
import com.sudokuapp.domain.usecase.VerifySudokuWithApiUseCase
import com.sudokuapp.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val generateSudokuUseCase: GenerateSudokuUseCase,
    private val getSavedGamesUseCase: GetSavedGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val verifySolutionUseCase: VerifySolutionUseCase,
    private val verifySudokuWithApiUseCase: VerifySudokuWithApiUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _sudokuState = MutableStateFlow<UiState<Sudoku>>(UiState.Loading)
    val sudokuState: StateFlow<UiState<Sudoku>> = _sudokuState.asStateFlow()

    private val _selectedCell = MutableStateFlow<SudokuCell?>(null)
    val selectedCell: StateFlow<SudokuCell?> = _selectedCell.asStateFlow()

    private val _isSolved = MutableStateFlow(false)
    val isSolved: StateFlow<Boolean> = _isSolved.asStateFlow()

    private val _isGameSaved = MutableStateFlow(false)
    val isGameSaved: StateFlow<Boolean> = _isGameSaved.asStateFlow()

    private val _isNoteMode = MutableStateFlow(false)
    val isNoteMode: StateFlow<Boolean> = _isNoteMode.asStateFlow()

    private val _verificationResult = MutableStateFlow<UiState<VerificationResult>?>(null)
    val verificationResult: StateFlow<UiState<VerificationResult>?> = _verificationResult.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GameEvent>()
    val uiEvent: SharedFlow<GameEvent> = _uiEvent.asSharedFlow()

    init {
        val gameId = savedStateHandle.get<String>("gameId")
        if (gameId != null) {
            loadGame(gameId)
        } else {
            val sizeParam = savedStateHandle.get<String>("size") ?: SudokuSize.STANDARD.name
            val difficultyParam = savedStateHandle.get<String>("difficulty") ?: SudokuDifficulty.EASY.name

            val size = SudokuSize.valueOf(sizeParam)
            val difficulty = SudokuDifficulty.valueOf(difficultyParam)

            generateNewSudoku(size, difficulty)
        }
    }

    private fun loadGame(gameId: String) {
        viewModelScope.launch {
            _sudokuState.value = UiState.Loading
            try {
                val sudoku = getSavedGamesUseCase.getSudokuById(gameId)
                if (sudoku != null) {
                    _sudokuState.value = UiState.Success(sudoku)
                    _isGameSaved.value = true
                    checkIfSolved(sudoku)
                } else {
                    _sudokuState.value = UiState.Error("Game not found")
                }
            } catch (e: Exception) {
                _sudokuState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun generateNewSudoku(size: SudokuSize, difficulty: SudokuDifficulty) {
        viewModelScope.launch {
            _sudokuState.value = UiState.Loading
            _selectedCell.value = null
            _isSolved.value = false
            _isGameSaved.value = false

            try {
                val result = generateSudokuUseCase(size, difficulty)
                result.onSuccess { sudoku ->
                    _sudokuState.value = UiState.Success(sudoku)
                    _isGameSaved.value = true

                    // Si el tamaño es SMALL (4x4) y la dificultad es HARD, mostramos un mensaje
                    // porque sabemos que la API tiene problemas con esta combinación
                    if (size == SudokuSize.SMALL && difficulty == SudokuDifficulty.HARD) {
                        _uiEvent.emit(GameEvent.ShowMessage("Se ha generado un sudoku localmente debido a limitaciones de la API con sudokus 4x4 difíciles."))
                    }
                }.onFailure { error ->
                    _sudokuState.value = UiState.Error(error.message ?: "Failed to generate sudoku")
                    _uiEvent.emit(GameEvent.ShowMessage("Error al generar sudoku: ${error.message}"))
                }
            } catch (e: Exception) {
                _sudokuState.value = UiState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(GameEvent.ShowMessage("Error: ${e.message}"))
            }
        }
    }

    fun selectCell(rowIndex: Int, colIndex: Int) {
        val currentState = _sudokuState.value
        if (currentState !is UiState.Success) return

        val sudoku = currentState.data

        // Don't allow selection of original cells
        if (sudoku.puzzle[rowIndex][colIndex] != null) return

        val cell = SudokuCell(
            rowIndex = rowIndex,
            colIndex = colIndex,
            value = sudoku.currentState[rowIndex][colIndex],
            isOriginal = false,
            isValid = verifySolutionUseCase.verifyCellValidity(sudoku, rowIndex, colIndex),
            notes = emptySet() // Get actual notes if implemented
        )

        _selectedCell.value = cell
    }

    fun setValueForSelectedCell(value: Int?) {
        val cell = _selectedCell.value ?: return
        val currentState = _sudokuState.value

        if (currentState !is UiState.Success) return

        val sudoku = currentState.data
        val newState = sudoku.currentState.map { it.toMutableList() }
        newState[cell.rowIndex][cell.colIndex] = value

        val updatedSudoku = sudoku.copy(currentState = newState)
        _sudokuState.value = UiState.Success(updatedSudoku)

        viewModelScope.launch {
            saveGameUseCase.updateGameState(sudoku.id, newState, false)
        }

        // Update selected cell
        _selectedCell.value = cell.copy(
            value = value,
            isValid = verifySolutionUseCase.verifyCellValidity(updatedSudoku, cell.rowIndex, cell.colIndex)
        )

        checkIfSolved(updatedSudoku)
    }

    private fun checkIfSolved(sudoku: Sudoku) {
        val isSolved = verifySolutionUseCase(sudoku)
        _isSolved.value = isSolved

        if (isSolved) {
            viewModelScope.launch {
                saveGameUseCase.updateGameState(sudoku.id, sudoku.currentState, true)
            }
        }
    }

    fun saveGame() {
        val currentState = _sudokuState.value
        if (currentState !is UiState.Success) return

        viewModelScope.launch {
            try {
                saveGameUseCase(currentState.data)
                _isGameSaved.value = true
                _uiEvent.emit(GameEvent.ShowMessage("Juego guardado correctamente"))
            } catch (e: Exception) {
                _uiEvent.emit(GameEvent.ShowMessage("Error al guardar el juego: ${e.message}"))
            }
        }
    }

    fun resetGame() {
        val currentState = _sudokuState.value
        if (currentState !is UiState.Success) return

        val sudoku = currentState.data
        val resetState = sudoku.puzzle.map { it.toMutableList() }

        val resetSudoku = sudoku.copy(
            currentState = resetState,
            isCompleted = false
        )

        _sudokuState.value = UiState.Success(resetSudoku)
        _selectedCell.value = null
        _isSolved.value = false

        viewModelScope.launch {
            saveGameUseCase.updateGameState(sudoku.id, resetState, false)
            _uiEvent.emit(GameEvent.ShowMessage("Tablero reiniciado"))
        }
    }

    fun toggleNoteMode() {
        _isNoteMode.value = !_isNoteMode.value
    }

    fun toggleNote(number: Int) {
        val cell = _selectedCell.value ?: return
        val currentState = _sudokuState.value

        if (currentState !is UiState.Success) return

        // Only allow notes on empty cells
        if (cell.value != null) return

        val notes = cell.notes.toMutableSet()
        if (notes.contains(number)) {
            notes.remove(number)
        } else {
            notes.add(number)
        }

        _selectedCell.value = cell.copy(notes = notes)
    }

    fun verifySolution() {
        val currentState = _sudokuState.value
        if (currentState !is UiState.Success) return

        val sudoku = currentState.data

        // Realizar verificación local primero para comprobar si está completo
        val isComplete = verifySolutionUseCase(sudoku)
        if (!isComplete) {
            viewModelScope.launch {
                _uiEvent.emit(GameEvent.ShowMessage("El tablero no está completo o tiene errores. Por favor verifica tus respuestas."))
            }
            return
        }

        // Si el tablero está completo, verificar con la API
        viewModelScope.launch {
            _verificationResult.value = UiState.Loading
            try {
                _uiEvent.emit(GameEvent.ShowMessage("Consultando a la API para verificar la solución..."))

                val result = verifySudokuWithApiUseCase(sudoku)
                result.onSuccess { verification ->
                    _verificationResult.value = UiState.Success(verification)

                    if (verification.isValid) {
                        _isSolved.value = true
                        saveGameUseCase.updateGameState(sudoku.id, sudoku.currentState, true)

                        // Mensaje diferente dependiendo de si usamos la API o fallback local
                        if (verification.solution == sudoku.solution) {
                            _uiEvent.emit(GameEvent.ShowMessage("¡Felicidades! Tu solución es correcta. (Verificación local)"))
                        } else {
                            _uiEvent.emit(GameEvent.ShowMessage("¡Felicidades! Tu solución coincide con la proporcionada por la API."))
                        }
                    } else {
                        _uiEvent.emit(GameEvent.ShowMessage("Tu solución no es correcta. ${verification.errorMessage ?: ""}"))
                    }
                }.onFailure { error ->
                    _verificationResult.value = UiState.Error(error.message ?: "Error en la verificación")
                    _uiEvent.emit(GameEvent.ShowMessage("Error al verificar con la API: ${error.message}. Se usará verificación local."))

                    // Verificar localmente como fallback
                    verifyLocally(sudoku)
                }
            } catch (e: Exception) {
                _verificationResult.value = UiState.Error(e.message ?: "Error desconocido")
                _uiEvent.emit(GameEvent.ShowMessage("Error al verificar con la API: ${e.message}. Se usará verificación local."))

                // Verificar localmente como fallback
                verifyLocally(sudoku)
            }
        }
    }

    private fun verifyLocally(sudoku: Sudoku) {
        viewModelScope.launch {
            // Verificar contra la solución guardada
            var isCorrect = true
            for (i in sudoku.currentState.indices) {
                for (j in sudoku.currentState[i].indices) {
                    if (sudoku.currentState[i][j] != sudoku.solution[i][j]) {
                        isCorrect = false
                        break
                    }
                }
                if (!isCorrect) break
            }

            if (isCorrect) {
                _isSolved.value = true
                saveGameUseCase.updateGameState(sudoku.id, sudoku.currentState, true)
                _uiEvent.emit(GameEvent.ShowMessage("¡Felicidades! Tu solución es correcta. (Verificación local)"))
            } else {
                _uiEvent.emit(GameEvent.ShowMessage("Tu solución no coincide con la solución correcta. (Verificación local)"))
            }

            // Actualizar el estado de verificación
            _verificationResult.value = UiState.Success(
                VerificationResult(
                    isValid = isCorrect,
                    solution = sudoku.solution,
                    errorMessage = if (isCorrect) null else "La solución no es correcta."
                )
            )
        }
    }
}

sealed class GameEvent {
    data class ShowMessage(val message: String) : GameEvent()
}