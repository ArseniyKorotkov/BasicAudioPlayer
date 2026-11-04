package by.arsy.basicaudioplayer

data class AudioState(
    val isRun: Boolean = false,
    val isPlayedTimeChangeHandle: Boolean = false,
    val isLoad: Boolean = false,
    val playedTime: Long = 0L,
    val endTime: Long = 0L
)
