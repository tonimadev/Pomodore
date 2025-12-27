package digital.tonima.pomodore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import digital.tonima.pomodore.R
import digital.tonima.pomodore.data.model.PomodoroSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: PomodoroSettings,
    onSaveSettings: (PomodoroSettings) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workDuration by remember { mutableStateOf(settings.workDurationMinutes.toString()) }
    var shortBreakDuration by remember { mutableStateOf(settings.shortBreakDurationMinutes.toString()) }
    var longBreakDuration by remember { mutableStateOf(settings.longBreakDurationMinutes.toString()) }
    var sessionsUntilLongBreak by remember { mutableStateOf(settings.sessionsUntilLongBreak.toString()) }
    var totalCycles by remember { mutableStateOf(settings.totalCycles.toString()) }
    var keepScreenOn by remember { mutableStateOf(settings.keepScreenOn) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Work Duration
            OutlinedTextField(
                value = workDuration,
                onValueChange = { workDuration = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.work_duration)) },
                suffix = { Text(stringResource(R.string.minutes)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Short Break Duration
            OutlinedTextField(
                value = shortBreakDuration,
                onValueChange = { shortBreakDuration = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.short_break_duration)) },
                suffix = { Text(stringResource(R.string.minutes)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Long Break Duration
            OutlinedTextField(
                value = longBreakDuration,
                onValueChange = { longBreakDuration = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.long_break_duration)) },
                suffix = { Text(stringResource(R.string.minutes)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Sessions Until Long Break
            OutlinedTextField(
                value = sessionsUntilLongBreak,
                onValueChange = { sessionsUntilLongBreak = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.sessions_until_long_break)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Total Cycles
            OutlinedTextField(
                value = totalCycles,
                onValueChange = { totalCycles = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.total_cycles)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Keep Screen On
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.keep_screen_on),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.keep_screen_on_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val newSettings = PomodoroSettings(
                            workDurationMinutes = workDuration.toIntOrNull() ?: 25,
                            shortBreakDurationMinutes = shortBreakDuration.toIntOrNull() ?: 5,
                            longBreakDurationMinutes = longBreakDuration.toIntOrNull() ?: 15,
                            sessionsUntilLongBreak = sessionsUntilLongBreak.toIntOrNull() ?: 4,
                            totalCycles = totalCycles.toIntOrNull() ?: 4,
                            keepScreenOn = keepScreenOn
                        )
                        onSaveSettings(newSettings)
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

