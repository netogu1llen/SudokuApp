package com.sudokuapp.presentation.screens.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuCell
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.usecase.GenerateSudokuUseCase
import com.sudokuapp.domain.usecase.GetSavedGamesUseCase
import com.sudokuapp.domain.usecase.SaveGameUseCase
import com.sudokuapp.domain.usecase.VerifySolutionUseCase
import com.sudokuapp.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val generateSudokuUseCase: GenerateSudokuUseCase,
    private val getSavedGamesUseCase: GetSavedGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val verifySolutionUseCase: VerifySolutionUseCase,
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
                }.onFailure { error ->
                    _sudokuState.value = UiState.Error(error.message ?: "Failed to generate sudoku")
                }
            } catch (e: Exception) {
                _sudokuState.value = UiState.Error(e.message ?: "Unknown error")
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
            } catch (e: Exception) {
                // Handle error
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
        val isSolved = verifySolutionUseCase(sudoku)
        _isSolved.value = isSolved

        if (isSolved) {
            viewModelScope.launch {
                saveGameUseCase.updateGameState(sudoku.id, sudoku.currentState, true)
            }
        }
    }
}