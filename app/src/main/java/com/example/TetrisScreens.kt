package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// Fuentes retro
val RetroFont = FontFamily.Monospace

@Composable
fun MenuScreen(
    highestScore: Int,
    isSoundEnabled: Boolean,
    onStartParams: () -> Unit,
    onToggleSound: () -> Unit,
    onResetHighScore: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "TETRIS",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RetroFont
            )

            Text(
                text = "HIGH SCORE: $highestScore",
                color = TextMutedDark,
                fontSize = 20.sp,
                fontFamily = RetroFont
            )

            Button(
                onClick = onStartParams,
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Text("JUGAR", fontSize = 24.sp, fontFamily = RetroFont, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { showSettings = true },
                modifier = Modifier
                    .size(width = 200.dp, height = 48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonColor)
            ) {
                Text("AJUSTES", fontSize = 16.sp, fontFamily = RetroFont, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (showSettings) {
            SettingsOverlay(
                isSoundEnabled = isSoundEnabled,
                onToggleSound = onToggleSound,
                onResetScore = onResetHighScore,
                onClose = { showSettings = false }
            )
        }
    }
}

@Composable
fun GameScreen(
    state: TetrisState,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onPause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(horizontal = 8.dp)
    ) {
        // Cabecera top 
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp, top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TETRIS CLASSIC", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = FontFamily.SansSerif)
                Text("PARTIDA EN CURSO", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.SansSerif, letterSpacing = (-1).sp)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ButtonColor)
                    .clickable { onPause() },
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(50)).background(Color.White))
                    Box(modifier = Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(50)).background(Color.White))
                }
            }
        }

        // Zona del Tablero y Panel lateral
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tablero Principal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor)
                    .border(2.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                TetrisGrid(
                    board = state.board,
                    currentPiece = state.currentPiece,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Panel Lateral
            Column(
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Next Piece
                NextPieceBox(nextPiece = state.nextPiece)
                // Score Card
                ScoreCardBox(
                    score = state.score.toString(),
                    lines = state.lines.toString(),
                    level = state.level.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Botonera de Control Inferior
        ControlPad(
            onLeft = onLeft,
            onRight = onRight,
            onRotate = onRotate,
            onSoftDrop = onSoftDrop,
            onHardDrop = onHardDrop
        )
    }

    // Overlay de Pausa
    if (state.gameState == GameState.PAUSADO) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayColor),
            contentAlignment = Alignment.Center
        ) {
            Text("PAUSADO", color = Color.White, fontSize = 48.sp, fontFamily = RetroFont, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScoreCardBox(score: String, lines: String, level: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("PUNTOS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, modifier = Modifier.padding(bottom = 4.dp))
                Text(score.padStart(6, '0'), color = ScoreCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = RetroFont)
            }
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("LÍNEAS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, modifier = Modifier.padding(bottom = 4.dp))
                Text(lines.padStart(2, '0'), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = RetroFont)
            }
        }
        Column {
            Text("NIVEL", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, modifier = Modifier.padding(bottom = 4.dp))
            Text(level.padStart(2, '0'), color = Orange500, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = RetroFont)
        }
    }
}

@Composable
fun NextPieceBox(nextPiece: TetrominoType?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SIGUIENTE",
            color = TextMutedDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            if (nextPiece != null) {
                val matriz = nextPiece.matrices
                val cols = matriz[0].size
                val rows = matriz.size
                Column {
                    for (i in 0 until rows) {
                        Row {
                            for (j in 0 until cols) {
                                if (matriz[i][j] != 0) {
                                    TetrisBlock(color = nextPiece.color, modifier = Modifier.size(14.dp).padding(0.5.dp))
                                } else {
                                    Box(modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlPad(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // D-Pad Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(56.dp)) // empty top-left
                ControlButton("↑", onClick = onRotate)
                Box(modifier = Modifier.size(56.dp)) // empty top-right
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ControlButton("←", onClick = onLeft)
                ControlButton("↓", onClick = onSoftDrop)
                ControlButton("→", onClick = onRight)
            }
        }

        // Action Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(ButtonActiveColor)
                    .border(4.dp, BorderColor, androidx.compose.foundation.shape.CircleShape)
                    .clickable { onRotate() },
                contentAlignment = Alignment.Center
            ) {
                Text("GIRAR", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = FontFamily.SansSerif)
            }
            Button(
                onClick = onHardDrop,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("HARD DROP", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = FontFamily.SansSerif)
            }
        }
    }
}

@Composable
fun ControlButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ButtonColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextPrimary, fontSize = 24.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .background(SurfaceColor, RoundedCornerShape(16.dp))
                .border(2.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(32.dp)
        ) {
            Text(
                "GAME OVER", 
                color = Color.Red, 
                fontSize = 48.sp, 
                fontFamily = RetroFont, 
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "SCORE: $score", 
                color = Color.White, 
                fontSize = 24.sp, 
                fontFamily = RetroFont
            )

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("REINTENTAR", fontSize = 18.sp, fontFamily = RetroFont, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsOverlay(
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onResetScore: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayColor)
            .clickable(onClick = onClose), // Close when clicking outside
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .clickable { /* prevent closing when clicking inside */ }
                .background(SurfaceColor, RoundedCornerShape(16.dp))
                .border(2.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(32.dp)
                .widthIn(min = 250.dp)
        ) {
            Text(
                "AJUSTES", 
                color = Color.White, 
                fontSize = 32.sp, 
                fontFamily = RetroFont, 
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onToggleSound,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonColor)
            ) {
                Text(if (isSoundEnabled) "SONIDO: ON" else "SONIDO: OFF", fontSize = 16.sp, fontFamily = RetroFont, color = TextPrimary)
            }

            Button(
                onClick = onResetScore,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA12020)) // Darker red
            ) {
                Text("RESETEAR PUNTAJE", fontSize = 14.sp, fontFamily = RetroFont, color = Color.White)
            }
            
            Button(
                onClick = onClose,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("CERRAR", fontSize = 16.sp, fontFamily = RetroFont, color = TextMuted)
            }
        }
    }
}
