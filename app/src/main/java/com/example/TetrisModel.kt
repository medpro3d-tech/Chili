package com.example

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

// Modelo básico de Coordenadas
data class Position(var x: Int, var y: Int)

// Estado general del juego
enum class GameState {
    INICIO,
    JUGANDO,
    PAUSADO,
    GAME_OVER
}

// Modelado de los Tetrominós
enum class TetrominoType(val color: Color, val matrices: Array<IntArray>) {
    I(
        TColor_Cyan,
        arrayOf(
            intArrayOf(0, 0, 0, 0),
            intArrayOf(1, 1, 1, 1),
            intArrayOf(0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0)
        )
    ),
    J(
        TColor_Blue,
        arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    ),
    L(
        TColor_Orange,
        arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    ),
    O(
        TColor_Yellow,
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        )
    ),
    S(
        TColor_Green,
        arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 0, 0)
        )
    ),
    T(
        TColor_Purple,
        arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    ),
    Z(
        TColor_Red,
        arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1),
            intArrayOf(0, 0, 0)
        )
    )
}

// Representa la pieza actual que cae en el tablero
data class PiezaActual(
    var tipo: TetrominoType,
    var matriz: Array<IntArray>,
    var pos: Position
) {
    // Generar copia rotada (90 grados en sentido horario)
    fun matrizRotada(): Array<IntArray> {
        val n = matriz.size
        val nuevaMatriz = Array(n) { IntArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                // Transpuesta e invertida para rotación horaria
                nuevaMatriz[j][n - 1 - i] = matriz[i][j]
            }
        }
        return nuevaMatriz
    }
}
