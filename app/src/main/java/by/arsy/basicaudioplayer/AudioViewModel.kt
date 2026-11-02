package by.arsy.basicaudioplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioViewModel : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    private val _isRun = MutableStateFlow(false)
    val isRun = _isRun.asStateFlow()

    fun initPlayer(context: Context) {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isRun.value = isPlaying
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

    fun stopAudio() {
        exoPlayer?.pause()
    }

    companion object {
        const val LOG_TAG = "playerExp"
    }

}