package de.ljz.talktome.ui.ds.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import de.ljz.talktome.ui.state.ThemeBehavior

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFFFF200),
)

private val LightColorScheme = lightColorScheme(
  primary = Purple40

)

@Composable
fun TalkToMeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  content: @Composable () -> Unit,
) {


  val colorScheme = when (vm.themeBehavior) {
    ThemeBehavior.DARK -> {
      if (vm.dynamicTheming && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        dynamicDarkColorScheme(context)
      } else {
        DarkColorScheme
      }
    }
    ThemeBehavior.LIGHT -> {
      if (vm.dynamicTheming && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        dynamicLightColorScheme(context)
      } else {
        LightColorScheme
      }
    }
    ThemeBehavior.SYSTEM_STANDARD -> {
      if (vm.dynamicTheming && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      } else {
        if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
      }
    }
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
//            (view.context as Activity).window.navigationBarColor = colorScheme.surfaceColorAtElevation(3.dp).toArgb()
      (view.context as Activity).window.navigationBarColor = colorScheme.background.toArgb()
      ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}