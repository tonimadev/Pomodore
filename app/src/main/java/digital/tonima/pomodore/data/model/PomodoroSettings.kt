package digital.tonima.pomodore.data.model

data class PomodoroSettings(
    val workDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val sessionsUntilLongBreak: Int = 4,
    val totalCycles: Int = 4,
    val keepScreenOn: Boolean = false
)

