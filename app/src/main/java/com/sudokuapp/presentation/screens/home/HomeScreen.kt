package com.sudokuapp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.presentation.util.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartNewGame: (SudokuSize, SudokuDifficulty) -> Unit,
    onContinueGame: (String) -> Unit
) {
    val savedGamesState by viewModel.savedGamesState.collectAsState()

    var selectedSize by remember { mutableStateOf(SudokuSize.STANDARD) }
    var selectedDifficulty by remember { mutableStateOf(SudokuDifficulty.EASY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sudoku Game",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSavedGames() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Game Options
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "New Game",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Size options
                    Text(
                        text = "Select Size:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                            .padding(vertical = 8.dp)
                    ) {
                        SudokuSize.values().forEach { size ->
                            val sizeText = when(size) {
                                SudokuSize.SMALL -> "Small (4x4)"
                                SudokuSize.STANDARD -> "Standard (9x9)"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (size == selectedSize),
                                        onClick = { selectedSize = size },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (size == selectedSize),
                                    onClick = null  // null because we're handling the click above
                                )
                                Text(
                                    text = sizeText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Difficulty options
                    Text(
                        text = "Select Difficulty:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                            .padding(vertical = 8.dp)
                    ) {
                        SudokuDifficulty.values().forEach { difficulty ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (difficulty == selectedDifficulty),
                                        onClick = { selectedDifficulty = difficulty },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (difficulty == selectedDifficulty),
                                    onClick = null  // null because we're handling the click above
                                )
                                Text(
                                    text = difficulty.name.lowercase().capitalize(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Game Button
                    Button(
                        onClick = { onStartNewGame(selectedSize, selectedDifficulty) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start New Game")
                    }
                }
            }

            // Saved Games List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Continue Game",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (savedGamesState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Success -> {
                            val savedGames = (savedGamesState as UiState.Success<List<Sudoku>>).data
                            if (savedGames.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No saved games found",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(savedGames) { game ->
                                        SavedGameItem(
                                            game = game,
                                            onContinueClick = { onContinueGame(game.id) }
                                        )

                                        if (game != savedGames.last()) {
                                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error: ${(savedGamesState as UiState.Error).message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedGameItem(
    game: Sudoku,
    onContinueClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = "${game.size.dimension}x${game.size.dimension} - ${game.difficulty.name.lowercase().capitalize()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Created: ${formatDate(game.timestamp)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Progress: ${calculateProgress(game)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Button(onClick = onContinueClick) {
            Text("Continue")
        }
    }
}

private fun calculateProgress(game: Sudoku): Int {
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

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

// Extension function to capitalize only the first letter
private fun String.capitalize(): String {
    return if (this.isEmpty()) this else this.replaceFirstChar { it.uppercase() }
}