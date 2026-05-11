package ke.jukwa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ke.jukwa.presentation.navigation.JukwaNavGraph
import ke.jukwa.ui.theme.JukwaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JukwaTheme {
                JukwaNavGraph()
            }
        }
    }
}
