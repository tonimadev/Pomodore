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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import kotlinx.coroutines.delay


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var showPermissionDialog by mutableStateOf(false)
    private var intentAction by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intentAction = intent?.action

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

                LaunchedEffect(uiState.timerState) {
                    intentAction?.let { action ->
                        handleIntent(Intent().apply { this.action = action }, viewModel)
                        intentAction = null
                    }
                }

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

                LaunchedEffect(uiState.settings.keepScreenOn, uiState.timerState) {
                    if (uiState.settings.keepScreenOn &&
                        (uiState.timerState is TimerState.Running || uiState.timerState is TimerState.Paused)) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var hasNotificationPermission by remember { mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    ) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000)
                            val newPermissionStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                            if (newPermissionStatus != hasNotificationPermission) {
                                hasNotificationPermission = newPermissionStatus
                            }
                        }
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

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = {
                            Text(
                                text = "Permissão de Notificações",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "O aplicativo precisa de permissão para enviar notificações para que o timer funcione corretamente em segundo plano.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sem essa permissão, você não receberá notificações quando o timer estiver em execução.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPermissionDialog = false
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            ) {
                                Text("Permitir")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showPermissionDialog = false }
                            ) {
                                Text("Agora Não")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentAction = intent.action
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
