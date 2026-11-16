package by.arsy.basicaudioplayer

import android.content.Context
import android.text.format.DateUtils.formatElapsedTime
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
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


@Composable
fun AudioScreen(
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
            driveTextScroll(
                listState =  textScroll,
                currentFirstItemIndex = currentFirstItemIndex,
                text = viewModel.text(),
                currentEndTime = audioState.endTime,
                changeCurrentTextDuration = { currentTextDuration = it },
                currentTextDuration = currentTextDuration,
            )
        }
    }

    LaunchedEffect(currentFirstItemIndex) {
        currentFirstItemIndex.run {
            driveTextScroll(
                listState = translateTextScroll,
                currentFirstItemIndex = currentFirstItemIndex,
                text = viewModel.text(),
                currentEndTime = audioState.endTime,
                changeCurrentTextDuration = { currentTextDuration = it },
                currentTextDuration = currentTextDuration,
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
                    viewModel.play("https://sefon.pro/api/mp3_download/direct/116590/3vUCAFX01eMwqAjcWYcpTtSm1FKpaYUnSZrznf3aG1o_fio6BVdq7Jx32RkGNjNmsn1MROhDO1klWf_yZfg5tr3TvDWI47Qe9SAIB4M3glF4eh3nDZCV9pxQz68cw5mF2PHbqzf1c9Y2famljZS-YZTfr-6DlvQfVUo6Ns4/")
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

private fun Long.getTime(): String {
    return formatElapsedTime( /* elapsedSeconds = */ this / 1000)
}

private suspend fun driveTextScroll(
    listState: LazyListState,
    currentFirstItemIndex: Int,
    text: List<AudioViewModel.TextEntity>,
    currentEndTime: Long,
    changeCurrentTextDuration: (Int) -> Unit,
    currentTextDuration: Int,
) {
    val scrollInfo = listState.layoutInfo
    val currentTextView = scrollInfo.visibleItemsInfo
        .firstOrNull { it.index == currentFirstItemIndex }

    currentTextView?.run {
        val currentText = text[currentTextView.index].endTime
        val nextText = text
            .getOrNull(currentTextView.index + 1)?.endTime
            ?: currentEndTime

        changeCurrentTextDuration.invoke((nextText - currentText).toInt() + 240)
        listState.animateScrollBy(
            value = currentTextView.offset.toFloat(),
            animationSpec = TweenSpec(
                durationMillis = currentTextDuration,
                easing = LinearEasing
            )
        )
    } ?: listState.animateScrollToItem(
        index = currentFirstItemIndex,
        scrollOffset = 0
    )
}