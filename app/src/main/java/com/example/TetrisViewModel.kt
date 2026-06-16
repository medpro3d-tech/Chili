package com.example

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SoundEvent {
    MOVER, ROTAR, CAIDA, LINEA, GAME_OVER
}

data class TetrisState(
    val gameState: GameState = GameState.INICIO,
    val score: Int = 0,
    val level: Int = 1,
    val lines: Int = 0,
    val highestScore: Int = 0, // Placeholder de puntuación máxima
    val isSoundEnabled: Boolean = true,
    val board: Array<Array<Color?>> = Array(20) { Array(10) { null } },
    val currentPiece: PiezaActual? = null,
    val nextPiece: TetrominoType? = null
)

class TetrisViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TetrisState())
    val uiState: StateFlow<TetrisState> = _uiState.asStateFlow()

    private val _soundEvents = MutableSharedFlow<SoundEvent>()
    val soundEvents = _soundEvents.asSharedFlow()

    private fun playSound(event: SoundEvent) {
        if (_uiState.value.isSoundEnabled) {
            viewModelScope.launch { _soundEvents.emit(event) }
        }
    }

    fun toggleSound() {
        _uiState.value = _uiState.value.copy(isSoundEnabled = !_uiState.value.isSoundEnabled)
    }

    fun resetHighScore() {
        _uiState.value = _uiState.value.copy(highestScore = 0)
    }

    private var gameJob: Job? = null
    
    // Configuración del tablero
    private val cols = 10
    private val rows = 20

    fun startGame() {
        val nextType = TetrominoType.values().random()
        _uiState.value = TetrisState(
            gameState = GameState.JUGANDO,
            highestScore = _uiState.value.highestScore,
            nextPiece = nextType
        )
        spawnPiece()
        startGameLoop()
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.JUGANDO) {
            _uiState.value = _uiState.value.copy(gameState = GameState.PAUSADO)
        } else if (_uiState.value.gameState == GameState.PAUSADO) {
            _uiState.value = _uiState.value.copy(gameState = GameState.JUGANDO)
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSADO) {
            _uiState.value = _uiState.value.copy(gameState = GameState.JUGANDO)
        }
    }

    fun resetGame() {
        startGame()
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                if (state.gameState == GameState.JUGANDO) {
                    val speedMs = (800 - (state.level * 50)).toLong().coerceAtLeast(100L)
                    delay(speedMs)
                    if (_uiState.value.gameState == GameState.JUGANDO) {
                        caidaSuave(fromGameLoop = true)
                    }
                } else {
                    delay(100) // Sleep if paused
                }
            }
        }
    }

    private fun spawnPiece() {
        val state = _uiState.value
        val tipoOriginal = state.nextPiece ?: TetrominoType.values().random()
        val nextType = TetrominoType.values().random()
        
        val nuevaPieza = PiezaActual(
            tipo = tipoOriginal,
            matriz = tipoOriginal.matrices,
            pos = Position(x = cols / 2 - tipoOriginal.matrices.size / 2, y = 0)
        )

        // Comprobar game over (colisión inmediata)
        if (!isValidPosition(nuevaPieza.matriz, nuevaPieza.pos.x, nuevaPieza.pos.y, state.board)) {
            _uiState.value = state.copy(
                gameState = GameState.GAME_OVER,
                highestScore = maxOf(state.highestScore, state.score)
            )
            playSound(SoundEvent.GAME_OVER)
            return
        }

        _uiState.value = state.copy(
            currentPiece = nuevaPieza,
            nextPiece = nextType
        )
    }

    // Movimiento Lateral
    fun moverIzquierda() {
        if (_uiState.value.gameState != GameState.JUGANDO) return
        val state = _uiState.value
        state.currentPiece?.let { piece ->
            if (isValidPosition(piece.matriz, piece.pos.x - 1, piece.pos.y, state.board)) {
                _uiState.value = state.copy(
                    currentPiece = piece.copy(pos = Position(piece.pos.x - 1, piece.pos.y))
                )
                playSound(SoundEvent.MOVER)
            }
        }
    }

    fun moverDerecha() {
        if (_uiState.value.gameState != GameState.JUGANDO) return
        val state = _uiState.value
        state.currentPiece?.let { piece ->
            if (isValidPosition(piece.matriz, piece.pos.x + 1, piece.pos.y, state.board)) {
                _uiState.value = state.copy(
                    currentPiece = piece.copy(pos = Position(piece.pos.x + 1, piece.pos.y))
                )
                playSound(SoundEvent.MOVER)
            }
        }
    }

    // Lógica Matemática de Rotación con Patada de Pared Básica (Wall Kick)
    fun rotarPieza() {
        if (_uiState.value.gameState != GameState.JUGANDO) return
        val state = _uiState.value
        state.currentPiece?.let { piece ->
            if (piece.tipo == TetrominoType.O) return // El cuadrado no rota

            val matrizRotada = piece.matrizRotada()
            // Intentar posición actual
            if (isValidPosition(matrizRotada, piece.pos.x, piece.pos.y, state.board)) {
                _uiState.value = state.copy(
                    currentPiece = piece.copy(matriz = matrizRotada)
                )
                playSound(SoundEvent.ROTAR)
            } else {
                // Wall Kick básico: intentar x-1 o x+1
                if (isValidPosition(matrizRotada, piece.pos.x - 1, piece.pos.y, state.board)) {
                    _uiState.value = state.copy(
                        currentPiece = piece.copy(matriz = matrizRotada, pos = Position(piece.pos.x - 1, piece.pos.y))
                    )
                    playSound(SoundEvent.ROTAR)
                } else if (isValidPosition(matrizRotada, piece.pos.x + 1, piece.pos.y, state.board)) {
                    _uiState.value = state.copy(
                        currentPiece = piece.copy(matriz = matrizRotada, pos = Position(piece.pos.x + 1, piece.pos.y))
                    )
                    playSound(SoundEvent.ROTAR)
                }
            }
        }
    }

    fun caidaSuave(fromGameLoop: Boolean = false) {
        if (_uiState.value.gameState != GameState.JUGANDO) return
        val state = _uiState.value
        state.currentPiece?.let { piece ->
            if (isValidPosition(piece.matriz, piece.pos.x, piece.pos.y + 1, state.board)) {
                _uiState.value = state.copy(
                    currentPiece = piece.copy(pos = Position(piece.pos.x, piece.pos.y + 1)),
                    score = if (!fromGameLoop) state.score + 1 else state.score // Punto por soft drop si fue manual
                )
            } else {
                lockPiece(piece)
            }
        }
    }

    fun caidaRapida() {
        if (_uiState.value.gameState != GameState.JUGANDO) return
        var state = _uiState.value
        state.currentPiece?.let { piece ->
            var newY = piece.pos.y
            var dropDistance = 0
            while (isValidPosition(piece.matriz, piece.pos.x, newY + 1, state.board)) {
                newY++
                dropDistance += 2 // 2 Puntos por cada bloque en hard drop
            }
            
            // Bloqueamos en la nueva posición
            val finalPiece = piece.copy(pos = Position(piece.pos.x, newY))
            _uiState.value = state.copy(score = state.score + dropDistance)
            lockPiece(finalPiece)
        }
    }

    private fun lockPiece(piece: PiezaActual) {
        val state = _uiState.value
        val newBoard = Array(state.board.size) { i -> state.board[i].clone() }

        // Transferir la pieza al tablero
        for (i in piece.matriz.indices) {
            for (j in piece.matriz[0].indices) {
                if (piece.matriz[i][j] != 0) {
                    val boardY = piece.pos.y + i
                    val boardX = piece.pos.x + j
                    if (boardY in 0 until rows && boardX in 0 until cols) {
                        newBoard[boardY][boardX] = piece.tipo.color
                    }
                }
            }
        }

        _uiState.value = state.copy(board = newBoard)
        clearLines()
    }

    private fun clearLines() {
        val state = _uiState.value
        val newBoard = Array(rows) { Array<Color?>(cols) { null } }
        var currentNewBoardRow = rows - 1
        var linesCleared = 0

        // Escanear de abajo hacia arriba
        for (y in rows - 1 downTo 0) {
            var isFullLine = true
            for (x in 0 until cols) {
                if (state.board[y][x] == null) {
                    isFullLine = false
                    break
                }
            }

            if (!isFullLine) {
                // Copiar fila si no está completa
                newBoard[currentNewBoardRow] = state.board[y].clone()
                currentNewBoardRow--
            } else {
                linesCleared++
            }
        }

        if (linesCleared > 0) {
            // Lógica de puntuación original
            val pointsMultiplier = when (linesCleared) {
                1 -> 100
                2 -> 300
                3 -> 500
                4 -> 800
                else -> 0
            }
            val newScore = state.score + (pointsMultiplier * state.level)
            val newLines = state.lines + linesCleared
            val newLevel = (newLines / 10) + 1 // Subir nivel cada 10 líneas

            _uiState.value = _uiState.value.copy(
                board = newBoard,
                score = newScore,
                lines = newLines,
                level = newLevel
            )
            playSound(SoundEvent.LINEA)
        } else {
            playSound(SoundEvent.CAIDA)
        }

        // Aparecer nueva pieza independientemente
        spawnPiece()
    }

    // Comprobación de Colisiones con bordes o bloques ya fijados en el tablero
    private fun isValidPosition(matriz: Array<IntArray>, testX: Int, testY: Int, board: Array<Array<Color?>>): Boolean {
        for (i in matriz.indices) {
            for (j in matriz[0].indices) {
                if (matriz[i][j] != 0) {
                    val boardY = testY + i
                    val boardX = testX + j
                    
                    // Colisión contra bordes
                    if (boardX < 0 || boardX >= cols || boardY >= rows) {
                        return false
                    }
                    // Colisión con piezas (evitar index out of bounds vertical arriba)
                    if (boardY >= 0) {
                        if (board[boardY][boardX] != null) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }
}
