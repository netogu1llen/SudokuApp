package com.sudokuapp.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokuapp.domain.model.SudokuCell

@Composable
fun SudokuCellComponent(
    cell: SudokuCell,
    isSelected: Boolean,
    onCellClick: (SudokuCell) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        cell.isOriginal -> MaterialTheme.colorScheme.surfaceVariant
        !cell.isValid -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        !cell.isValid -> MaterialTheme.colorScheme.error
        cell.isOriginal -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !cell.isValid -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.extraSmall)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clickable(enabled = !cell.isOriginal) { onCellClick(cell) }
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cell.value != null) {
                Text(
                    text = cell.value.toString(),
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = if (cell.isOriginal) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            } else if (cell.notes.isNotEmpty()) {
                NotesGrid(notes = cell.notes, maxNumber = 9)
            }
        }
    }
}

@Composable
fun NotesGrid(notes: Set<Int>, maxNumber: Int) {
    val rowCount = when {
        maxNumber <= 4 -> 2
        maxNumber <= 9 -> 3
        else -> 4
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until rowCount) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until rowCount) {
                    val number = row * rowCount + col + 1
                    if (number <= maxNumber) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (notes.contains(number)) {
                                Text(
                                    text = number.toString(),
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}