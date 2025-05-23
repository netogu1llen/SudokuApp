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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuCell
import com.sudokuapp.presentation.components.NumberPad
import com.sudokuapp.presentation.components.SudokuBoard
import com.sudokuapp.presentation.util.UiState
import kotlinx.coroutines.flow.collectLatest

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
    val isNoteMode by viewModel.isNoteMode.collectAsState()
    val verificationResult by viewModel.verificationResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Eventos de juego (mensajes)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is GameEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(isSolved) {
        if (isSolved) {
            snackbarHostState.showSnackbar("¡Felicidades! Has resuelto el puzzle correctamente.")
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
                    IconButton(onClick = { viewModel.toggleNoteMode() }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Toggle Notes",
                            tint = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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

                    // Mostrar indicador de carga durante la verificación de la API
                    if (verificationResult is UiState.Loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Verificando la solución con la API...")
                            }
                        }
                    } else {
                        GameContent(
                            sudoku = sudoku,
                            selectedCell = selectedCell,
                            isNoteMode = isNoteMode,
                            isSolved = isSolved,
                            onCellClick = { row, col -> viewModel.selectCell(row, col) },
                            onNumberClick = { number -> viewModel.setValueForSelectedCell(number) },
                            onClearClick = { viewModel.setValueForSelectedCell(null) },
                            onNoteClick = { number -> viewModel.toggleNote(number) },
                            onVerifyClick = { viewModel.verifySolution() }
                        )
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
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
    onNoteClick: (Int) -> Unit,
    onVerifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),  // Reducido de 8.dp
        verticalArrangement = Arrangement.spacedBy(4.dp)  // Reducido de 8.dp
    ) {
        // Información compacta del juego
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),  // Reducido de 4.dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Size: ${sudoku.size.dimension}x${sudoku.size.dimension}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Difficulty: ${sudoku.difficulty.name.lowercase().capitalize()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isSolved) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)  // Reducido de 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Solved!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Sudoku board - ocupando menos espacio vertical
        SudokuBoard(
            sudoku = sudoku,
            selectedCell = selectedCell,
            onCellClick = onCellClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)  // Reducido de 1f para que ocupe menos espacio vertical
                .padding(vertical = 2.dp)  // Reducido de 4.dp
        )

        // Mensaje de ayuda para modo de notas
        if (isNoteMode) {
            Text(
                text = "Note Mode: On",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Botón de verificación
        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)  // Reducido de 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Verificar Solución con API",
                modifier = Modifier.padding(end = 4.dp)  // Reducido de 8.dp
            )
            Text("Verificar Solución con API")
        }

        // Number pad - tamaño adecuado
        NumberPad(
            sudokuSize = sudoku.size,
            onNumberClick = onNumberClick,
            onClearClick = onClearClick,
            onNoteClick = onNoteClick,
            isNoteMode = isNoteMode,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)  // Reducido de 8.dp
        )
    }
}

private fun String.capitalize(): String {
    return if (this.isEmpty()) this else this.replaceFirstChar { it.uppercase() }
}