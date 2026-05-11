package ke.jukwa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Green700,
    onPrimary = White,
    secondary = Amber,
    onSecondary = Black,
    tertiary = InfoBlue,
    onTertiary = White,
    error = EmergencyRed,
    onError = White,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceVariant,
    onSurface = DarkOnSurfaceVariant,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    outline = Gray600,
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = White,
    secondary = Amber,
    onSecondary = Black,
    tertiary = InfoBlue,
    onTertiary = White,
    error = EmergencyRed,
    onError = White,
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray600,
    outline = Gray400,
)

@Composable
fun JukwaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
