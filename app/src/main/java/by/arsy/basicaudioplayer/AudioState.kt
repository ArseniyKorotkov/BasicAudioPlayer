package by.arsy.basicaudioplayer

data class AudioState(
    val isRun: Boolean = false,
    val playedTime: Long = 0L,
    val endTime: Long = 0L
)
