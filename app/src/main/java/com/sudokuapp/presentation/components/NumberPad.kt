package com.sudokuapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokuapp.domain.model.SudokuSize

@Composable
fun NumberPad(
    sudokuSize: SudokuSize,
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onNoteClick: (Int) -> Unit,
    isNoteMode: Boolean,
    modifier: Modifier = Modifier
) {
    val numbers = (1..sudokuSize.dimension).toList()

    // Calcular filas y columnas para el teclado numérico
    val columns = when (sudokuSize) {
        SudokuSize.SMALL -> 2 // Para 4x4, usamos 2 columnas
        SudokuSize.STANDARD -> 3 // Para 9x9, usamos 3 columnas
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fila de botones de control (Clear y Notes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón Clear
            ControlButton(
                text = "Clear",
                icon = Icons.Default.Backspace,
                onClick = onClearClick,
                isActive = false,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            // Botón Notes
            ControlButton(
                text = "Notes",
                icon = Icons.Default.Edit,
                onClick = { /* Manejar el cambio de modo */ },
                isActive = isNoteMode,
                backgroundColor = if (isNoteMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        // Crear la cuadrícula de números
        val chunkedNumbers = numbers.chunked(columns)
        chunkedNumbers.forEach { rowNumbers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until columns) {
                    if (i < rowNumbers.size) {
                        val number = rowNumbers[i]
                        NumberButton(
                            number = number,
                            onClick = {
                                if (isNoteMode) {
                                    onNoteClick(number)
                                } else {
                                    onNumberClick(number)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Espacio vacío para mantener la alineación
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isActive: Boolean,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(2.5f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor
            )
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Spacer(modifier: Modifier) {
    Box(modifier = modifier)
}