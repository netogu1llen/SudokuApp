package com.sudokuapp.presentation.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.usecase.GetSavedGamesUseCase
import com.sudokuapp.domain.usecase.SaveGameUseCase
import com.sudokuapp.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedGamesViewModel @Inject constructor(
    private val getSavedGamesUseCase: GetSavedGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase
) : ViewModel() {

    private val _savedGamesState = MutableStateFlow<UiState<List<Sudoku>>>(UiState.Loading)
    val savedGamesState: StateFlow<UiState<List<Sudoku>>> = _savedGamesState.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    init {
        loadSavedGames()
    }

    fun loadSavedGames() {
        viewModelScope.launch {
            _savedGamesState.value = UiState.Loading

            getSavedGamesUseCase(onlyInProgress = _filterType.value == FilterType.IN_PROGRESS)
                .catch { e ->
                    _savedGamesState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { games ->
                    val filteredGames = when (_filterType.value) {
                        FilterType.ALL -> games
                        FilterType.IN_PROGRESS -> games.filter { !it.isCompleted }
                        FilterType.COMPLETED -> games.filter { it.isCompleted }
                    }
                    _savedGamesState.value = UiState.Success(filteredGames)
                }
        }
    }

    fun setFilterType(filterType: FilterType) {
        _filterType.value = filterType
        loadSavedGames()
    }

    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            try {
                saveGameUseCase.deleteGame(gameId)
                loadSavedGames()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

enum class FilterType {
    ALL,
    IN_PROGRESS,
    COMPLETED
}