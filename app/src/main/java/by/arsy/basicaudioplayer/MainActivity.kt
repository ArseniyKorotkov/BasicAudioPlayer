package by.arsy.basicaudioplayer

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils.formatElapsedTime
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val audioState by viewModel.audioState.collectAsState()
    val activity = LocalActivity.current
    val textScroll = rememberLazyListState()
    val translateTextScroll = rememberLazyListState()
    val currentFirstItemIndex by remember(audioState.playedTime) {
        derivedStateOf {
            viewModel.text().indexOfFirst { it.endTime > audioState.playedTime }
        }
    }
    var currentTextDuration by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(currentFirstItemIndex) {
        currentFirstItemIndex.run {
            val scrollInfo = textScroll.layoutInfo
            val currentTextView = scrollInfo.visibleItemsInfo
                .firstOrNull { it.index == currentFirstItemIndex }

            currentTextView?.run {
                val currentText = viewModel.text()[currentTextView.index].endTime
                val nextText = viewModel.text()
                    .getOrNull(currentTextView.index + 1)?.endTime
                    ?: audioState.endTime

                currentTextDuration = (nextText - currentText).toInt() + 240
                textScroll.animateScrollBy(
                    value = currentTextView.offset.toFloat(),
                    animationSpec = TweenSpec(
                        durationMillis = currentTextDuration,
                        easing = LinearEasing
                    )
                )
            } ?: textScroll.animateScrollToItem(index = currentFirstItemIndex, scrollOffset = 0)
        }
    }

    LaunchedEffect(currentFirstItemIndex) {
        currentFirstItemIndex.run {
            val scrollInfo = translateTextScroll.layoutInfo
            val currentTextView = scrollInfo.visibleItemsInfo
                .firstOrNull { it.index == currentFirstItemIndex }

            currentTextView?.run {
                val currentText = viewModel.text()[currentTextView.index].endTime
                val nextText = viewModel.text()
                    .getOrNull(currentTextView.index + 1)?.endTime
                    ?: audioState.endTime

                currentTextDuration = (nextText - currentText).toInt() + 240
                translateTextScroll.animateScrollBy(
                    value = currentTextView.offset.toFloat(),
                    animationSpec = TweenSpec(
                        durationMillis = currentTextDuration,
                        easing = LinearEasing
                    )
                )
            } ?: translateTextScroll.animateScrollToItem(
                index = currentFirstItemIndex,
                scrollOffset = 0
            )
        }
    }

    DisposableEffect(key1 = audioState.isRun) {
        val window = activity?.window
        if (audioState.isRun) {
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
                if (audioState.isRun) {
                    viewModel.stopAudio()
                } else {
                    viewModel.play("https://sefon.pro/api/mp3_download/direct/116590/3vUCAGSlTLuMkDKTbxi2zX3j6qxS-J7nbb18HhfXCOizhVxkI9lWZd70iiNmiwbmNlT8BzS2J_8NKyiDljqsYDKjAbf5w-COraW__0ZQtxXzIkP_Yhv22VIW8sLt39SH4fvda7q4Yj0Lm6uXFJxyBH8P4PgJhOOT1ugE0g/")
                }
            }
        ) {
            Text(
                text = if (audioState.isRun) "||" else {
                    if (audioState.isLoad) "o" else ">"
                },
                fontSize = 42.sp
            )
        }
        Slider(
            value = audioState.playedTime / 1000f,
            onValueChange = {
                viewModel.startPlayedTimeChangeHandle()
                viewModel.updateAudioPlayedTime(it.toLong() * 1000)
            },
            onValueChangeFinished = {
                viewModel.endPlayedTimeChangeHandle()
            },
            valueRange = 0f..audioState.endTime / 1000f
        )
        Text(
            text = "${audioState.playedTime.getTime()} / ${audioState.endTime.getTime()}"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = textScroll
        ) {
            items(
                items = viewModel.text(),
                key = { it.endTime }
            ) {
                val animateColorAsState by animateColorAsState(
                    targetValue = if (viewModel.text().indexOf(it) > currentFirstItemIndex)
                        Color.Gray else Color.White,
                    animationSpec = TweenSpec(durationMillis = currentTextDuration)
                )
                Text(
                    text = it.text,
                    fontSize = 24.sp,
                    color = animateColorAsState
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = translateTextScroll
        ) {
            items(
                items = viewModel.text(),
                key = { it.endTime }
            ) {
                val animateColorAsState by animateColorAsState(
                    targetValue = if (viewModel.text().indexOf(it) > currentFirstItemIndex)
                        Color.Gray else Color.White,
                    animationSpec = TweenSpec(durationMillis = currentTextDuration)
                )
                Text(
                    text = it.translatedText,
                    fontSize = 24.sp,
                    color = animateColorAsState
                )
            }
        }
    }

}

fun Long.getTime(): String {
    return formatElapsedTime( /* elapsedSeconds = */ this / 1000)
}
