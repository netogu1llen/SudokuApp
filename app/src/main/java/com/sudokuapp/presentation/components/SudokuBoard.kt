package com.sudokuapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sudokuapp.domain.model.Sudoku
import com.sudokuapp.domain.model.SudokuCell
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.domain.model.getCell

@Composable
fun SudokuBoard(
    sudoku: Sudoku,
    selectedCell: SudokuCell?,
    onCellClick: (rowIndex: Int, colIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimension = sudoku.size.dimension
    val boxSize = when (sudoku.size) {
        SudokuSize.SMALL -> 2
        SudokuSize.STANDARD -> 3
        SudokuSize.LARGE -> 4
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.background)
    ) {
        for (rowIndex in 0 until dimension) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (colIndex in 0 until dimension) {
                    val cell = sudoku.getCell(rowIndex, colIndex)
                    val isSelected = selectedCell?.rowIndex == rowIndex && selectedCell.colIndex == colIndex

                    val boxRow = rowIndex / boxSize
                    val boxCol = colIndex / boxSize
                    val boxIndex = boxRow * boxSize + boxCol
                    val isEvenBox = boxIndex % 2 == 0

                    val boxBackground = if (isEvenBox) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(boxBackground)
                    ) {
                        SudokuCellComponent(
                            cell = cell,
                            isSelected = isSelected,
                            onCellClick = { onCellClick(rowIndex, colIndex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}