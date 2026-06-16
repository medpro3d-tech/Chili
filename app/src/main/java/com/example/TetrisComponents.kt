package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BlockBorderDark
import com.example.ui.theme.BlockBorderLight
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.GridColor

@Composable
fun TetrisBlock(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color)
            .border(1.dp, Color.Black)
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val pw = w * 0.15f
            
            // Borde Superior (Brillo)
            drawRect(
                color = BlockBorderLight,
                topLeft = Offset(0f, 0f),
                size = Size(w, pw)
            )
            // Borde Izquierdo (Brillo)
            drawRect(
                color = BlockBorderLight,
                topLeft = Offset(0f, 0f),
                size = Size(pw, h)
            )
            // Borde Inferior (Sombra)
            drawRect(
                color = BlockBorderDark,
                topLeft = Offset(0f, h - pw),
                size = Size(w, pw)
            )
            // Borde Derecho (Sombra)
            drawRect(
                color = BlockBorderDark,
                topLeft = Offset(w - pw, 0f),
                size = Size(pw, h)
            )
        }
    }
}

@Composable
fun TetrisGrid(
    board: Array<Array<Color?>>,
    currentPiece: PiezaActual?,
    modifier: Modifier = Modifier
) {
    val cols = 10
    val rows = 20

    Box(
        modifier = modifier
            .aspectRatio(cols.toFloat() / rows.toFloat())
            .background(BoardBackground)
            .border(2.dp, Color.Gray)
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows

            // Fondo de rejilla tenue
            for (i in 0..cols) {
                drawLine(
                    color = GridColor,
                    start = Offset(i * cellWidth, 0f),
                    end = Offset(i * cellWidth, size.height),
                    strokeWidth = 1f
                )
            }
            for (i in 0..rows) {
                drawLine(
                    color = GridColor,
                    start = Offset(0f, i * cellHeight),
                    end = Offset(size.width, i * cellHeight),
                    strokeWidth = 1f
                )
            }

            // Dibujar bloques fijos
            for (y in board.indices) {
                for (x in board[y].indices) {
                    val color = board[y][x]
                    if (color != null) {
                        drawBlock(x, y, cellWidth, cellHeight, color)
                    }
                }
            }

            // Dibujar pieza actual
            currentPiece?.let { piece ->
                val mat = piece.matriz
                for (i in mat.indices) {
                    for (j in mat[0].indices) {
                        if (mat[i][j] != 0) {
                            val targetX = piece.pos.x + j
                            val targetY = piece.pos.y + i
                            if (targetY >= 0) {
                                drawBlock(targetX, targetY, cellWidth, cellHeight, piece.tipo.color)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función helper dentro del Canvas para dibujar un bloque estilo "3D"
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlock(
    x: Int, y: Int, cellWidth: Float, cellHeight: Float, color: Color
) {
    val blockRect = androidx.compose.ui.geometry.Rect(
        left = x * cellWidth,
        top = y * cellHeight,
        right = (x + 1) * cellWidth,
        bottom = (y + 1) * cellHeight
    )
    
    // Relleno principal
    drawRect(
        color = color,
        topLeft = blockRect.topLeft,
        size = blockRect.size
    )

    val p = cellWidth * 0.15f
    // Superior (Luz)
    drawRect(color = BlockBorderLight, topLeft = blockRect.topLeft, size = Size(blockRect.width, p))
    // Izquierda (Luz)
    drawRect(color = BlockBorderLight, topLeft = blockRect.topLeft, size = Size(p, blockRect.height))
    // Abajo (Sombra)
    drawRect(color = BlockBorderDark, topLeft = Offset(blockRect.left, blockRect.bottom - p), size = Size(blockRect.width, p))
    // Derecha (Sombra)
    drawRect(color = BlockBorderDark, topLeft = Offset(blockRect.right - p, blockRect.top), size = Size(p, blockRect.height))
    
    // Contorno exterior
    drawRect(
        color = Color.Black,
        topLeft = blockRect.topLeft,
        size = blockRect.size,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
    )
}
