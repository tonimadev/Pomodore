package digital.tonima.pomodore.data.model

data class PomodoroUiState(
    val timerState: TimerState = TimerState.Idle,
    val currentSession: Int = 1,
    val completedSessions: Int = 0,
    val settings: PomodoroSettings = PomodoroSettings(),
    val celebrationShown: Boolean = false
)

