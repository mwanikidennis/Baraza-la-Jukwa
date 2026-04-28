package ke.jukwa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JukwaColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = SkyBlue,
    tertiary = SafetyOrange,
    background = Black,
    surface = DarkGray,
    onPrimary = Black,
    onSecondary = Black,
    onBackground = NeonGreen,
    onSurface = TextGray,
    error = AlertRed
)

@Composable
fun JukwaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JukwaColorScheme,
        typography = Typography,
        content = content
    )
}
