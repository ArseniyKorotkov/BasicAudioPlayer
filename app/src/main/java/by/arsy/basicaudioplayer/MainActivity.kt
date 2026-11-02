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
                    viewModel.play("https://sefon.pro/api/mp3_download/direct/1233798/3vUCADNzBWFY4RFewdV9RW9bFnX0aMyLy0yggE7VbT_gPgGHQJAAtJmVts37OUb6DiwIkNoIubA0ZE7mE8h149WEp886d9kdHE4Z9Ifao2Vgco0qNXEM35uwjcN4vFnz9RlcQqqG9mxDFVamWz_IwhZLBqHz5PgcROl-4w/")
                }
            }
        ) {
            Text(
                text = if (isRun) "||" else ">",
                fontSize = 42.sp
            )
        }
        Text(text = "$playedTime")
    }

}
