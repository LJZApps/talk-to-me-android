package de.ljz.talktome.ui.features.setup.pages

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinci.androidtemplate.ui.navigation.animations.SlideHorizontallyAnimation
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ljz.talktome.core.compose.UIModePreviews
import de.ljz.talktome.core.mvi.EffectCollector
import de.ljz.talktome.ui.ds.theme.TalkToMeTheme
import de.ljz.talktome.ui.features.setup.SetupViewContract.Action
import de.ljz.talktome.ui.features.setup.SetupViewContract.State
import de.ljz.talktome.ui.features.setup.SetupViewModel
import de.ljz.talktome.ui.navigation.SetupNavGraph
import io.sentry.compose.SentryTraced

@OptIn(ExperimentalComposeUiApi::class)
@SetupNavGraph(start = true)
@Destination(style = SlideHorizontallyAnimation::class)
@Composable
fun SetupAppTheme(
  navigator: DestinationsNavigator,
  modifier: Modifier = Modifier,
  vm: SetupViewModel
) {
  val context = LocalContext.current

  val uiState: State by vm.state.collectAsStateWithLifecycle()

  EffectCollector(effect = vm.effect) { effect ->
    when (effect) {
      else -> {}
    }
  }

  TalkToMeTheme {
    SentryTraced(tag = "setup_app_theme") {
      SetupAppThemeContent(
        uiState = uiState,
        onAction = vm::onAction,
        modifier = modifier
      )
    }
  }
}

@Composable
private fun SetupAppThemeContent(
  uiState: State,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = {
      onAction(Action.ChangeTheme)
    }
  ) {
    Text(text = "dfdf")
  }
}

@UIModePreviews
@Composable
private fun SetupAppThemePreview() {
  TalkToMeTheme {
    SetupAppThemeContent(
      uiState = State(),
      onAction = {}
    )
  }
}