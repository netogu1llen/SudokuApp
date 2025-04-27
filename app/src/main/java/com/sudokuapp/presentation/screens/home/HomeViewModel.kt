package com.sudokuapp.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.usecase.GetSavedGamesUseCase
import com.sudokuapp.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSavedGamesUseCase: GetSavedGamesUseCase
) : ViewModel() {

    private val _savedGamesState = MutableStateFlow<UiState<List<Sudoku>>>(UiState.Loading)
    val savedGamesState: StateFlow<UiState<List<Sudoku>>> = _savedGamesState.asStateFlow()

    init {
        loadSavedGames()
    }

    fun loadSavedGames() {
        viewModelScope.launch {
            _savedGamesState.value = UiState.Loading

            getSavedGamesUseCase(onlyInProgress = true)
                .map { games -> games.sortedByDescending { it.timestamp }.take(5) }
                .catch { e ->
                    _savedGamesState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { games ->
                    _savedGamesState.value = UiState.Success(games)
                }
        }
    }
}