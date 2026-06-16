package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .safeDrawingPadding()) {
            TetrisApp()
          }
        }
      }
    }
  }
}

@Composable
fun TetrisApp(viewModel: TetrisViewModel = viewModel()) {
  val state by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val soundManager = remember { SoundManager(context) }

  LaunchedEffect(viewModel) {
      viewModel.soundEvents.collect { event ->
          when (event) {
              SoundEvent.MOVER -> soundManager.playMover()
              SoundEvent.ROTAR -> soundManager.playRotar()
              SoundEvent.CAIDA -> soundManager.playCaida()
              SoundEvent.LINEA -> soundManager.playLinea()
              SoundEvent.GAME_OVER -> soundManager.playGameOver()
          }
      }
  }

  DisposableEffect(Unit) {
      onDispose {
          soundManager.release()
      }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    when (state.gameState) {
      GameState.INICIO -> {
        MenuScreen(
          highestScore = state.highestScore,
          isSoundEnabled = state.isSoundEnabled,
          onStartParams = { viewModel.startGame() },
          onToggleSound = { viewModel.toggleSound() },
          onResetHighScore = { viewModel.resetHighScore() }
        )
      }
      GameState.JUGANDO, GameState.PAUSADO -> {
        GameScreen(
          state = state,
          onLeft = { viewModel.moverIzquierda() },
          onRight = { viewModel.moverDerecha() },
          onRotate = { viewModel.rotarPieza() },
          onSoftDrop = { viewModel.caidaSuave() },
          onHardDrop = { viewModel.caidaRapida() },
          onPause = { viewModel.pauseGame() }
        )
      }
      GameState.GAME_OVER -> {
        GameScreen(
          state = state,
          onLeft = {},
          onRight = {},
          onRotate = {},
          onSoftDrop = {},
          onHardDrop = {},
          onPause = {}
        )
        GameOverScreen(
          score = state.score,
          onRestart = { viewModel.resetGame() }
        )
      }
    }
  }
}
