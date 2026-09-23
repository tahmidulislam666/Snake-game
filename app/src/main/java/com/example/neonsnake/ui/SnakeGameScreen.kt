package com.example.neonsnake.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neonsnake.R
import com.example.neonsnake.model.Direction
import com.example.neonsnake.model.GameState
import com.example.neonsnake.model.GameStatus
import com.example.neonsnake.ui.theme.NeonAccentGreen
import com.example.neonsnake.ui.theme.NeonBackground
import com.example.neonsnake.ui.theme.NeonCanvas
import com.example.neonsnake.ui.theme.NeonCyan
import com.example.neonsnake.ui.theme.NeonGreenBody
import com.example.neonsnake.ui.theme.NeonGreenHead
import com.example.neonsnake.ui.theme.NeonGridLine
import com.example.neonsnake.ui.theme.NeonMuted
import com.example.neonsnake.ui.theme.NeonPanel
import com.example.neonsnake.ui.theme.NeonPanelBorder
import com.example.neonsnake.ui.theme.NeonPink
import com.example.neonsnake.ui.theme.NeonText
import com.example.neonsnake.viewmodel.SnakeViewModel
import kotlin.math.abs

@Composable
fun SnakeGameScreen(
    viewModel: SnakeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.score) {
        if (state.score > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == GameStatus.GAME_OVER) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val pageGradient = Brush.radialGradient(
        colors = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
        center = Offset(200f, 100f),
        radius = 800f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NeonBackground)
            .background(pageGradient)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            GameHeader(
                score = state.score,
                bestScore = state.bestScore,
                status = state.status,
                onTogglePause = { viewModel.togglePause() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Game Board Wrap
            GameBoard(
                state = state,
                onDirectionChange = { viewModel.changeDirection(it) },
                onRestart = { viewModel.startGame() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // On-screen Touch Controls (D-Pad)
            TouchControls(
                onDirection = { viewModel.changeDirection(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer info
            GameFooter()

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GameHeader(
    score: Int,
    bestScore: Int,
    status: GameStatus,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = stringResource(R.string.eyebrow_text),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status == GameStatus.RUNNING || status == GameStatus.PAUSED) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("pause_button"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NeonPanel,
                        contentColor = NeonCyan
                    )
                ) {
                    Icon(
                        imageVector = if (status == GameStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (status == GameStatus.RUNNING) R.string.pause else R.string.resume)
                    )
                }
            }

            ScoreCard(
                label = stringResource(R.string.score_label),
                score = score,
                testTag = "score_display"
            )

            ScoreCard(
                label = stringResource(R.string.best_label),
                score = bestScore,
                testTag = "best_display"
            )
        }
    }
}

@Composable
private fun ScoreCard(
    label: String,
    score: Int,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .widthIn(min = 72.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = NeonPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPanelBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = label.uppercase(),
                color = NeonMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                color = NeonAccentGreen,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun GameBoard(
    state: GameState,
    onDirectionChange: (Direction) -> Unit,
    onRestart: () -> Unit
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(24.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(NeonCanvas)
            .border(1.dp, NeonPanelBorder, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    },
                    onDragEnd = {
                        val minDistance = 24f
                        if (abs(totalDragX) > minDistance || abs(totalDragY) > minDistance) {
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > 0) onDirectionChange(Direction.RIGHT)
                                else onDirectionChange(Direction.LEFT)
                            } else {
                                if (totalDragY > 0) onDirectionChange(Direction.DOWN)
                                else onDirectionChange(Direction.UP)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val tileSize = canvasSize / state.gridSize

            // Background
            drawRect(color = NeonCanvas, size = size)

            // Grid lines
            for (i in 1 until state.gridSize) {
                val pos = i * tileSize
                // Vertical lines
                drawLine(
                    color = NeonGridLine,
                    start = Offset(pos, 0f),
                    end = Offset(pos, canvasSize),
                    strokeWidth = 1f
                )
                // Horizontal lines
                drawLine(
                    color = NeonGridLine,
                    start = Offset(0f, pos),
                    end = Offset(canvasSize, pos),
                    strokeWidth = 1f
                )
            }

            // Draw Food (Apple)
            val foodCenterX = state.food.x * tileSize + tileSize / 2f
            val foodCenterY = state.food.y * tileSize + tileSize / 2f
            val foodRadius = tileSize * 0.35f

            // Food neon glow
            drawCircle(
                color = NeonPink.copy(alpha = 0.35f),
                radius = foodRadius * 1.8f,
                center = Offset(foodCenterX, foodCenterY)
            )
            // Food core
            drawCircle(
                color = NeonPink,
                radius = foodRadius,
                center = Offset(foodCenterX, foodCenterY)
            )

            // Draw Snake
            state.snake.forEachIndexed { index, segment ->
                val isHead = index == 0
                val inset = if (isHead) tileSize * 0.08f else tileSize * 0.12f
                val segX = segment.x * tileSize + inset
                val segY = segment.y * tileSize + inset
                val segSize = tileSize - inset * 2f
                val cornerRadius = CornerRadius(if (isHead) tileSize * 0.28f else tileSize * 0.2f)

                if (isHead) {
                    // Head neon glow
                    drawRoundRect(
                        color = NeonAccentGreen.copy(alpha = 0.3f),
                        topLeft = Offset(segX - 3f, segY - 3f),
                        size = Size(segSize + 6f, segSize + 6f),
                        cornerRadius = cornerRadius
                    )
                    // Head shape
                    drawRoundRect(
                        color = NeonGreenHead,
                        topLeft = Offset(segX, segY),
                        size = Size(segSize, segSize),
                        cornerRadius = cornerRadius
                    )
                } else {
                    // Body shape
                    drawRoundRect(
                        color = NeonGreenBody,
                        topLeft = Offset(segX, segY),
                        size = Size(segSize, segSize),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        // Overlay for Game Over
        AnimatedVisibility(
            visible = state.status == GameStatus.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD905080E)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 320.dp)
                        .testTag("game_over_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xF20D141F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAccentGreen.copy(alpha = 0.35f)),
                    shadowElevation = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.score >= state.bestScore && state.score > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonAccentGreen.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonAccentGreen),
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = "★ NEW HIGHEST SCORE ★",
                                    color = NeonAccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Score: ${state.score}   •   Best: ${state.bestScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = NeonText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ready for another run?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = NeonMuted
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("restart_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonAccentGreen,
                                contentColor = Color(0xFF0A1008)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.play_again_button),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Overlay for Paused
        AnimatedVisibility(
            visible = state.status == GameStatus.PAUSED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB305080E)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 280.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xF20D141F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Paused",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF0A1008)
                            )
                        ) {
                            Text(
                                text = "Resume",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TouchControls(
    onDirection: (Direction) -> Unit
) {
    Column(
        modifier = Modifier.testTag("touch_controls"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Up button
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            DpadButton(
                direction = Direction.UP,
                icon = Icons.Default.ArrowUpward,
                contentDescription = stringResource(R.string.move_up),
                testTag = "move_up_button",
                onClick = { onDirection(Direction.UP) }
            )
        }

        // Left, Down, Right buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DpadButton(
                direction = Direction.LEFT,
                icon = Icons.Default.ArrowLeft,
                contentDescription = stringResource(R.string.move_left),
                testTag = "move_left_button",
                onClick = { onDirection(Direction.LEFT) }
            )
            DpadButton(
                direction = Direction.DOWN,
                icon = Icons.Default.ArrowDownward,
                contentDescription = stringResource(R.string.move_down),
                testTag = "move_down_button",
                onClick = { onDirection(Direction.DOWN) }
            )
            DpadButton(
                direction = Direction.RIGHT,
                icon = Icons.Default.ArrowRight,
                contentDescription = stringResource(R.string.move_right),
                testTag = "move_right_button",
                onClick = { onDirection(Direction.RIGHT) }
            )
        }
    }
}

@Composable
private fun DpadButton(
    direction: Direction,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = NeonPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPanelBorder),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = NeonText,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun GameFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.footer_move),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = NeonMuted
        )
        Text(
            text = stringResource(R.string.footer_speed),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = NeonMuted
        )
    }
}
