package com.sudokuapp.presentation.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuCell
import com.sudokuapp.presentation.components.NumberPad
import com.sudokuapp.presentation.components.SudokuBoard
import com.sudokuapp.presentation.util.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val sudokuState by viewModel.sudokuState.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()
    val isSolved by viewModel.isSolved.collectAsState()
    val isGameSaved by viewModel.isGameSaved.collectAsState()

    var isNoteMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isSolved) {
        if (isSolved) {
            snackbarHostState.showSnackbar("Congratulations! You solved the puzzle!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sudoku",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isNoteMode = !isNoteMode }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Toggle Notes",
                            tint = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.resetGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                    if (!isGameSaved) {
                        IconButton(onClick = { viewModel.saveGame() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (sudokuState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val sudoku = (sudokuState as UiState.Success<Sudoku>).data
                    GameContent(
                        sudoku = sudoku,
                        selectedCell = selectedCell,
                        isNoteMode = isNoteMode,
                        isSolved = isSolved,
                        onCellClick = { row, col -> viewModel.selectCell(row, col) },
                        onNumberClick = { number -> viewModel.setValueForSelectedCell(number) },
                        onClearClick = { viewModel.setValueForSelectedCell(null) },
                        onNoteClick = { number -> viewModel.toggleNote(number) }
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${(sudokuState as UiState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameContent(
    sudoku: Sudoku,
    selectedCell: SudokuCell?,
    isNoteMode: Boolean,
    isSolved: Boolean,
    onCellClick: (Int, Int) -> Unit,
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onNoteClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Size: ${sudoku.size.dimension}x${sudoku.size.dimension}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Difficulty: ${sudoku.difficulty.name.lowercase().capitalize()}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Created: ${formatDate(sudoku.timestamp)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Sudoku board
        SudokuBoard(
            sudoku = sudoku,
            selectedCell = selectedCell,
            onCellClick = onCellClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        )

        // Completed indicator
        if (isSolved) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Puzzle Completed!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Number pad
        NumberPad(
            sudokuSize = sudoku.size,
            onNumberClick = onNumberClick,
            onClearClick = onClearClick,
            onNoteClick = onNoteClick,
            isNoteMode = isNoteMode
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}