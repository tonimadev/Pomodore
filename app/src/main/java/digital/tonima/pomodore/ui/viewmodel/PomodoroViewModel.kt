package digital.tonima.pomodore.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import digital.tonima.pomodore.data.model.PomodoroSettings
import digital.tonima.pomodore.data.model.PomodoroUiState
import digital.tonima.pomodore.data.model.TimerMode
import digital.tonima.pomodore.data.model.TimerState
import digital.tonima.pomodore.data.repository.PomodoroRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PomodoroRepository(application)

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.settings.collect { settings: PomodoroSettings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun startTimer(mode: TimerMode = TimerMode.WORK) {
        val settings = _uiState.value.settings
        val currentState = _uiState.value

        val shouldReset = currentState.completedSessions >= settings.totalCycles &&
                         currentState.timerState is TimerState.Idle

        val duration = when (mode) {
            TimerMode.WORK -> settings.workDurationMinutes
            TimerMode.SHORT_BREAK -> settings.shortBreakDurationMinutes
            TimerMode.LONG_BREAK -> settings.longBreakDurationMinutes
        } * 60 * 1000L

        _uiState.update {
            it.copy(
                timerState = TimerState.Running(
                    mode = mode,
                    timeRemainingMillis = duration,
                    totalTimeMillis = duration
                ),
                currentSession = if (shouldReset) 1 else it.currentSession,
                completedSessions = if (shouldReset) 0 else it.completedSessions,
                celebrationShown = if (shouldReset) false else it.celebrationShown
            )
        }

        runTimer()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        val currentState = _uiState.value.timerState
        if (currentState is TimerState.Running) {
            _uiState.update {
                it.copy(
                    timerState = TimerState.Paused(
                        mode = currentState.mode,
                        timeRemainingMillis = currentState.timeRemainingMillis,
                        totalTimeMillis = currentState.totalTimeMillis
                    )
                )
            }
        }
    }

    fun resumeTimer() {
        val currentState = _uiState.value.timerState
        if (currentState is TimerState.Paused) {
            _uiState.update {
                it.copy(
                    timerState = TimerState.Running(
                        mode = currentState.mode,
                        timeRemainingMillis = currentState.timeRemainingMillis,
                        totalTimeMillis = currentState.totalTimeMillis
                    )
                )
            }
            runTimer()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerState = TimerState.Idle,
                currentSession = 1,
                completedSessions = 0,
                celebrationShown = false
            )
        }
    }

    fun skipToNext() {
        timerJob?.cancel()
        val currentState = _uiState.value

        when (val state = currentState.timerState) {
            is TimerState.Running, is TimerState.Paused -> {
                val mode = if (state is TimerState.Running) state.mode else (state as TimerState.Paused).mode

                if (mode == TimerMode.WORK) {
                    val newCompletedSessions = currentState.completedSessions + 1

                    if (newCompletedSessions >= currentState.settings.totalCycles) {
                        _uiState.update {
                            it.copy(
                                completedSessions = newCompletedSessions,
                                currentSession = newCompletedSessions,
                                timerState = TimerState.Idle
                            )
                        }
                        return
                    }

                    val newSession = currentState.currentSession + 1

                    val nextMode = if (newCompletedSessions % currentState.settings.sessionsUntilLongBreak == 0) {
                        TimerMode.LONG_BREAK
                    } else {
                        TimerMode.SHORT_BREAK
                    }

                    _uiState.update {
                        it.copy(
                            completedSessions = newCompletedSessions,
                            currentSession = newSession
                        )
                    }
                    startTimer(nextMode)
                } else {
                    startTimer(TimerMode.WORK)
                }
            }
            else -> {}
        }
    }

    fun completeTimer() {
        val currentState = _uiState.value

        when (val state = currentState.timerState) {
            is TimerState.Running -> {
                if (state.mode == TimerMode.WORK) {
                    val newCompletedSessions = currentState.completedSessions + 1
                    val newSession = currentState.currentSession + 1

                    if (newCompletedSessions >= currentState.settings.totalCycles) {
                        _uiState.update {
                            it.copy(
                                completedSessions = newCompletedSessions,
                                currentSession = newCompletedSessions,
                                timerState = TimerState.Idle,
                                celebrationShown = false
                            )
                        }
                    } else {
                        val nextMode = if (newCompletedSessions % currentState.settings.sessionsUntilLongBreak == 0) {
                            TimerMode.LONG_BREAK
                        } else {
                            TimerMode.SHORT_BREAK
                        }

                        _uiState.update {
                            it.copy(
                                completedSessions = newCompletedSessions,
                                currentSession = newSession,
                                timerState = TimerState.Completed(state.mode)
                            )
                        }

                        startTimer(nextMode)
                    }
                } else {
                    _uiState.update {
                        it.copy(timerState = TimerState.Completed(state.mode))
                    }
                }
            }
            else -> {}
        }
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _uiState.value.timerState
                if (currentState is TimerState.Running) {
                    val newTime = currentState.timeRemainingMillis - 1000
                    if (newTime <= 0) {
                        completeTimer()
                        break
                    } else {
                        _uiState.update {
                            it.copy(
                                timerState = currentState.copy(timeRemainingMillis = newTime)
                            )
                        }
                    }
                } else {
                    break
                }
            }
        }
    }

    fun updateSettings(settings: PomodoroSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun markCelebrationShown() {
        _uiState.update {
            it.copy(celebrationShown = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
