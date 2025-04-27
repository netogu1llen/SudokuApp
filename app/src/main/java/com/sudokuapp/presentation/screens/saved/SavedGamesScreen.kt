package com.sudokuapp.presentation.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.presentation.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedGamesScreen(
    viewModel: SavedGamesViewModel,
    onNavigateBack: () -> Unit,
    onGameSelected: (String) -> Unit
) {
    val gamesState by viewModel.savedGamesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Games",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (gamesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val games = (gamesState as UiState.Success<List<Sudoku>>).data
                    if (games.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No saved games found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        SavedGamesList(
                            games = games,
                            onGameSelected = onGameSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${(gamesState as UiState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedGamesList(
    games: List<Sudoku>,
    onGameSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(games) { game ->
            SavedGameCard(
                game = game,
                onClick = { onGameSelected(game.id) }
            )

            if (game != games.last()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedGameCard(
    game: Sudoku,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${game.size.dimension}x${game.size.dimension} - ${game.difficulty.name.capitalize()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Created: ${formatDateTime(game.timestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Completion: ${calculateCompletionPercentage(game)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (game.isCompleted) {
                Text(
                    text = "COMPLETED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun String.capitalize(): String {
    return if (this.isEmpty()) this else this.replaceFirstChar { it.uppercase() }
}

private fun formatDateTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

private fun calculateCompletionPercentage(game: Sudoku): Int {
    var filledCells = 0
    var totalCells = 0

    for (row in game.currentState) {
        for (cell in row) {
            totalCells++
            if (cell != null) {
                filledCells++
            }
        }
    }

    return ((filledCells.toFloat() / totalCells) * 100).toInt()
}