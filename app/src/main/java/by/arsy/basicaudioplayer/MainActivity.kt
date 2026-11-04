package by.arsy.basicaudioplayer

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.arsy.basicaudioplayer.ui.theme.BasicAudioPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicAudioPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioPlayerExample(
                        context = applicationContext,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlayerExample(
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: AudioViewModel = viewModel()
) {
    val isRun by viewModel.isRun.collectAsState()
    val playedTime by viewModel.playedTime.collectAsState()
    val trackTime by viewModel.trackTime.collectAsState()
    val activity = LocalActivity.current

    DisposableEffect(isRun) {
        val window = activity?.window
        if (isRun) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Column(
        modifier = modifier
    ) {
        Button(
            onClick = {
                viewModel.initPlayer(context = context)
                if (isRun) {
                    viewModel.stopAudio()
                } else {
                    viewModel.play("https://sefon.pro/api/mp3_download/direct/473774/3vUCACkHO2N_DuccBwV3XXxWD9P2S9kWLSU022fZQMpj1INC31rq8g2K9sqP5Dx62xfZiGLemz3BrdPjwjrp0_jYCvvsHlvmqUOV84DfybNz8sqHe8BGlk1pQmpDtausjk_4W-b14mfzL0NV-NnQmwhP-eVGCt_n_iSrHvU/")
                }
            }
        ) {
            Text(
                text = if (isRun) "||" else ">",
                fontSize = 42.sp
            )
        }
        Text(text = "$playedTime:$trackTime")
    }

}
