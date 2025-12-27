package digital.tonima.pomodore.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.pomodore.R
import digital.tonima.pomodore.data.model.PomodoroUiState
import digital.tonima.pomodore.data.model.TimerMode
import digital.tonima.pomodore.data.model.TimerState
import digital.tonima.pomodore.ui.components.CelebrationAnimation
import digital.tonima.pomodore.ui.components.CircularProgressTimer
import digital.tonima.pomodore.ui.components.PomodoroButton
import digital.tonima.pomodore.ui.components.SessionCounter
import digital.tonima.pomodore.ui.components.TimerDisplay
import digital.tonima.pomodore.ui.theme.getModeColors
import kotlinx.coroutines.delay

@Composable
fun PomodoroScreen(
    uiState: PomodoroUiState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onSkipClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCelebrationShown: () -> Unit,
    hasNotificationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    // Get current mode
    val currentMode = when (val state = uiState.timerState) {
        is TimerState.Running -> state.mode
        is TimerState.Paused -> state.mode
        is TimerState.Completed -> state.mode
        else -> TimerMode.WORK
    }

    // Get colors for current mode
    val modeColors = getModeColors(currentMode)

    // Animate background color transitions
    val animatedBackgroundColor by animateColorAsState(
        targetValue = modeColors.background,
        animationSpec = tween(durationMillis = 1000),
        label = "background_color"
    )

    val animatedPrimaryColor by animateColorAsState(
        targetValue = modeColors.primary,
        animationSpec = tween(durationMillis = 1000),
        label = "primary_color"
    )

    // Check if timer is active (running or paused)
    val isTimerActive = uiState.timerState is TimerState.Running ||
                       uiState.timerState is TimerState.Paused

    // Check if all cycles completed and celebration not shown yet
    val shouldShowCelebration = uiState.completedSessions >= uiState.settings.totalCycles &&
            uiState.timerState is TimerState.Idle &&
            !uiState.celebrationShown &&
            uiState.completedSessions > 0

    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(shouldShowCelebration) {
        if (shouldShowCelebration) {
            showCelebration = true
            delay(5000) // Show for 5 seconds
            showCelebration = false
            onCelebrationShown()  // Mark as shown
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedBackgroundColor,
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with current mode and settings button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        // Settings button aligned to the right
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.align(Alignment.TopEnd),
                                enabled = !isTimerActive
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings),
                                    tint = if (isTimerActive)
                                        animatedPrimaryColor.copy(alpha = 0.3f)
                                    else
                                        animatedPrimaryColor
                                )
                            }
                        }

                        Text(
                            text = when (val state = uiState.timerState) {
                                is TimerState.Running -> getModeString(state.mode)
                            is TimerState.Paused -> getModeString(state.mode)
                            is TimerState.Completed -> getModeString(state.mode)
                            else -> stringResource(R.string.work_session)
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = animatedPrimaryColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SessionCounter(
                        currentSession = uiState.currentSession,
                        completedSessions = uiState.completedSessions,
                        totalCycles = uiState.settings.totalCycles,
                        color = animatedPrimaryColor
                    )
                }

                // Permission Warning Banner
                if (!hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PermissionWarningBanner()
                }
            }

                // Timer display with circular progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    val (progress, timeRemaining) = when (val state = uiState.timerState) {
                        is TimerState.Running -> {
                            Pair(
                                1f - (state.timeRemainingMillis.toFloat() / state.totalTimeMillis.toFloat()),
                                state.timeRemainingMillis
                            )
                        }
                        is TimerState.Paused -> {
                            Pair(
                                1f - (state.timeRemainingMillis.toFloat() / state.totalTimeMillis.toFloat()),
                                state.timeRemainingMillis
                            )
                        }
                        else -> Pair(0f, 0L)
                    }

                    CircularProgressTimer(
                        progress = progress,
                        color = animatedPrimaryColor,
                        trackColor = modeColors.accent.copy(alpha = 0.3f)
                    )
                    TimerDisplay(
                        timeRemainingMillis = timeRemaining,
                        color = animatedPrimaryColor
                    )
                }

                // Control buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    when (uiState.timerState) {
                        is TimerState.Idle, is TimerState.Completed -> {
                            PomodoroButton(
                                text = stringResource(R.string.start),
                                onClick = onStartClick,
                                modifier = Modifier.fillMaxWidth(0.6f),
                                backgroundColor = animatedPrimaryColor
                            )
                        }
                        is TimerState.Running -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PomodoroButton(
                                    text = stringResource(R.string.pause),
                                    onClick = onPauseClick,
                                    backgroundColor = animatedPrimaryColor
                                )
                                PomodoroButton(
                                    text = stringResource(R.string.skip),
                                    onClick = onSkipClick,
                                    isPrimary = false,
                                    backgroundColor = modeColors.secondary
                                )
                            }
                        }
                        is TimerState.Paused -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PomodoroButton(
                                    text = stringResource(R.string.resume),
                                    onClick = onResumeClick,
                                    backgroundColor = animatedPrimaryColor
                                )
                                PomodoroButton(
                                    text = stringResource(R.string.stop),
                                    onClick = onStopClick,
                                    isPrimary = false,
                                    backgroundColor = modeColors.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Celebration overlay
        if (showCelebration) {
            CelebrationAnimation(
                message = stringResource(R.string.all_cycles_completed)
            )
        }
    }
}

@Composable
private fun getModeString(mode: TimerMode): String {
    return when (mode) {
        TimerMode.WORK -> stringResource(R.string.work_session)
        TimerMode.SHORT_BREAK -> stringResource(R.string.short_break)
        TimerMode.LONG_BREAK -> stringResource(R.string.long_break)
    }
}

@Composable
private fun PermissionWarningBanner() {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.notification_permission_denied),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF856404)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.notification_permission_explanation),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF856404)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF856404)
                ),
                modifier = Modifier
            ) {
                Text(
                    text = stringResource(R.string.enable_notifications),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

