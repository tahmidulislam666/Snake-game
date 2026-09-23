package com.example.neonsnake

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.neonsnake.model.Direction
import com.example.neonsnake.ui.SnakeGameScreen
import com.example.neonsnake.ui.theme.NeonBackground
import com.example.neonsnake.ui.theme.NeonSnakeTheme
import com.example.neonsnake.viewmodel.SnakeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SnakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NeonSnakeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = NeonBackground,
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    SnakeGameScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                viewModel.changeDirection(Direction.UP)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                viewModel.changeDirection(Direction.DOWN)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> {
                viewModel.changeDirection(Direction.LEFT)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> {
                viewModel.changeDirection(Direction.RIGHT)
                true
            }
            KeyEvent.KEYCODE_SPACE -> {
                viewModel.togglePause()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
