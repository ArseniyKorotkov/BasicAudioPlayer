package by.arsy.basicaudioplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AudioViewModel : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    private val _audioState = MutableStateFlow(value = AudioState())

    val audioState = _audioState.asStateFlow()

    fun initPlayer(context: Context) {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _audioState.value = _audioState.value.copy(isRun = isPlaying)
                    updateCurrentPositon()
                    updateEndPosition()
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(LOG_TAG, "Ошибка ExoPlayer: ${error.message}", error)
                }
            })

        }
    }

    fun play(uri: String) {
        exoPlayer?.run {
            if (mediaItemCount == 0) {
                val audioItem = MediaItem.fromUri(uri)
                setMediaItem(audioItem)
                prepare()
            }
            play()
        }
    }

    private fun updateCurrentPositon() {
        viewModelScope.launch {
            while (exoPlayer?.isPlaying == true) {
                if (!_audioState.value.isPlayedTimeChangeHandle) {
                    exoPlayer?.currentPosition?.let {
                        updateAudioPlayedTime(it)
                    }
                }
                delay(duration = 200.milliseconds)
            }
        }
    }

    private fun updateEndPosition() {
        _audioState.value = _audioState.value.copy(endTime = exoPlayer?.duration ?: 0L)
    }

    fun stopAudio() {
        exoPlayer?.pause()
    }

    fun updateAudioPlayedTime(time: Long) {
        _audioState.value = _audioState.value.copy(playedTime = time)
    }

    fun startPlayedTimeChangeHandle() {
        _audioState.value = _audioState.value.copy(isPlayedTimeChangeHandle = true)
    }

    fun endPlayedTimeChangeHandle() {
        exoPlayer?.seekTo(_audioState.value.playedTime)
        _audioState.value = _audioState.value.copy(isPlayedTimeChangeHandle = false)
    }

    companion object {
        const val LOG_TAG = "playerExp"
    }

}