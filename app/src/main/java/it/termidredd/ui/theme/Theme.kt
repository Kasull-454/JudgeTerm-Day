package it.termidredd.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Palette Chiyuki (da ~/.local/share/color-schemes/Chiyuki.colors e Konsole Chiyuki.colorscheme).
 */
object Chiyuki {
    val Night = Color(0xFF2E3440)      // Header/Tooltip background
    val View = Color(0xFF343A47)       // View background
    val Window = Color(0xFF3B4252)     // Window/Button background
    val Alternate = Color(0xFF383F4D)
    val Raised = Color(0xFF434C5E)
    val Outline = Color(0xFF6E7891)    // Color0Intense

    val Foreground = Color(0xFFEADFE6)
    val ForegroundInactive = Color(0xFFD0C3CC)
    val ForegroundFaint = Color(0xFFA19DA6)

    val Accent = Color(0xFFEBB5ED)     // DecorationFocus / Selection
    val AccentDeep = Color(0xFFB15D96)
    val OnAccent = Color(0xFF48204E)
    val Purple = Color(0xFFD8A9E0)

    val Red = Color(0xFFBF616A)
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF93000A)
    val Yellow = Color(0xFFEBCB8B)
    val Green = Color(0xFFA3BE8C)
    val Blue = Color(0xFF81A1C1)
    val Cyan = Color(0xFF88C0D0)
}

private val ChiyukiScheme = darkColorScheme(
    primary = Chiyuki.Accent,
    onPrimary = Chiyuki.OnAccent,
    primaryContainer = Chiyuki.AccentDeep,
    onPrimaryContainer = Chiyuki.Foreground,
    secondary = Chiyuki.Cyan,
    onSecondary = Chiyuki.Night,
    tertiary = Chiyuki.Yellow,
    onTertiary = Chiyuki.Night,
    background = Chiyuki.Night,
    onBackground = Chiyuki.Foreground,
    surface = Chiyuki.Night,
    onSurface = Chiyuki.Foreground,
    surfaceVariant = Chiyuki.Window,
    onSurfaceVariant = Chiyuki.ForegroundFaint,
    surfaceContainerLowest = Chiyuki.Night,
    surfaceContainerLow = Chiyuki.View,
    surfaceContainer = Chiyuki.View,
    surfaceContainerHigh = Chiyuki.Window,
    surfaceContainerHighest = Chiyuki.Raised,
    outline = Chiyuki.Outline,
    outlineVariant = Chiyuki.Window,
    error = Chiyuki.Error,
    onError = Chiyuki.OnError,
)

private val base = Typography()
private val ChiyukiTypography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
)

private val ChiyukiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
)

/** L'app usa sempre il tema scuro Chiyuki. */
@Composable
fun JudgeTermDayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChiyukiScheme,
        typography = ChiyukiTypography,
        shapes = ChiyukiShapes,
        content = content,
    )
}
