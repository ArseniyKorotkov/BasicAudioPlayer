package by.arsy.basicaudioplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.arsy.basicaudioplayer.ui.theme.BasicAudioPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicAudioPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioScreen(
                        context = applicationContext,
                        modifier = Modifier.padding(paddingValues = innerPadding),
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer {
                                AudioViewModel(historyStorage = HistoryStorage())
                            }
                        })
                    )
                }
            }
        }
    }
}
