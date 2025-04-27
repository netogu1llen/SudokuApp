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
    }

    // Grosor de los bordes
    val thinBorder = 0.5.dp
    val thickBorder = 2.dp

    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .border(thickBorder, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.background)
            .padding(2.dp)
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

                    // Determinar si la celda está en un borde
                    val isRightBorderThick = (colIndex + 1) % boxSize == 0 && colIndex < dimension - 1
                    val isBottomBorderThick = (rowIndex + 1) % boxSize == 0 && rowIndex < dimension - 1

                    // Determinar color de fondo alternado para las cajas
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
                            .aspectRatio(1f)
                            .background(boxBackground)
                            // Añadir bordes para una mejor separación visual
                            .border(
                                width = if (isRightBorderThick) thickBorder else thinBorder,
                                color = MaterialTheme.colorScheme.outline.copy(
                                    alpha = if (isRightBorderThick) 0.7f else 0.3f
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .border(
                                width = if (isBottomBorderThick) thickBorder else thinBorder,
                                color = MaterialTheme.colorScheme.outline.copy(
                                    alpha = if (isBottomBorderThick) 0.7f else 0.3f
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(1.dp)
                    ) {
                        SudokuCellComponent(
                            cell = cell,
                            isSelected = isSelected,
                            onCellClick = { onCellClick(rowIndex, colIndex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}