package digital.tonima.pomodore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.pomodore.data.model.TimerState
import digital.tonima.pomodore.service.PomodoroForegroundService
import digital.tonima.pomodore.ui.screens.PomodoroScreen
import digital.tonima.pomodore.ui.screens.SettingsScreen
import digital.tonima.pomodore.ui.theme.PomodoreTheme
import digital.tonima.pomodore.ui.viewmodel.PomodoroViewModel
import digital.tonima.pomodore.util.Constants


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            PomodoreTheme {
                val viewModel: PomodoroViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                // Track intent action changes with state
                var lastProcessedAction by remember { mutableStateOf<String?>(null) }
                val currentAction = rememberUpdatedState(intent?.action)

                // Handle notification actions - process each action when it changes
                LaunchedEffect(currentAction.value) {
                    val action = currentAction.value
                    // Only process if this is a new action we haven't processed yet
                    if (action != null && action != lastProcessedAction) {
                        handleIntent(Intent().apply { this.action = action }, viewModel)
                        lastProcessedAction = action
                        // Clear action after brief delay to allow re-triggering same action
                        kotlinx.coroutines.delay(100)
                        lastProcessedAction = null
                    }
                }

                // Update foreground service when timer state changes
                LaunchedEffect(uiState.timerState) {
                    when (val state = uiState.timerState) {
                        is TimerState.Running -> {
                            PomodoroForegroundService.startService(this@MainActivity, state)
                        }
                        is TimerState.Paused -> {
                            PomodoroForegroundService.updateService(this@MainActivity, state)
                        }
                        is TimerState.Idle -> {
                            PomodoroForegroundService.stopService(this@MainActivity)
                        }
                        is TimerState.Completed -> {
                            PomodoroForegroundService.stopService(this@MainActivity)
                        }
                    }
                }

                // Control screen on/off based on settings and timer state
                LaunchedEffect(uiState.settings.keepScreenOn, uiState.timerState) {
                    if (uiState.settings.keepScreenOn &&
                        (uiState.timerState is TimerState.Running || uiState.timerState is TimerState.Paused)) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Check notification permission status
                    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "pomodoro",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("pomodoro") {
                            PomodoroScreen(
                                uiState = uiState,
                                onStartClick = { viewModel.startTimer() },
                                onPauseClick = { viewModel.pauseTimer() },
                                onResumeClick = { viewModel.resumeTimer() },
                                onStopClick = { viewModel.stopTimer() },
                                onSkipClick = { viewModel.skipToNext() },
                                onSettingsClick = {
                                    // Only navigate if timer is not active
                                    val isTimerActive = uiState.timerState is TimerState.Running ||
                                                       uiState.timerState is TimerState.Paused
                                    if (!isTimerActive) {
                                        navController.navigate("settings")
                                    }
                                },
                                onCelebrationShown = { viewModel.markCelebrationShown() },
                                hasNotificationPermission = hasNotificationPermission
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                settings = uiState.settings,
                                onSaveSettings = { newSettings ->
                                    viewModel.updateSettings(newSettings)
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Re-trigger intent handling when activity receives a new intent from notification
    }

    private fun handleIntent(intent: Intent?, viewModel: PomodoroViewModel) {
        when (intent?.action) {
            Constants.ACTION_START -> viewModel.startTimer()
            Constants.ACTION_PAUSE -> viewModel.pauseTimer()
            Constants.ACTION_RESUME -> viewModel.resumeTimer()
            Constants.ACTION_SKIP -> viewModel.skipToNext()
            Constants.ACTION_STOP -> viewModel.stopTimer()
        }
    }
}

