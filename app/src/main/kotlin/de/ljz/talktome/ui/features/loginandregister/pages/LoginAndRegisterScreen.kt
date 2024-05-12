package de.ljz.talktome.ui.features.loginandregister.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinci.androidtemplate.ui.navigation.animations.SlideHorizontallyAnimation
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ljz.talktome.core.compose.UIModePreviews
import de.ljz.talktome.core.mvi.EffectCollector
import de.ljz.talktome.ui.ds.theme.TalkToMeTheme
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.State
import de.ljz.talktome.ui.features.loginandregister.LoginViewModel
import de.ljz.talktome.ui.navigation.LoginAndRegisterNavGraph
import de.ljz.talktome.ui.navigation.destinations.LoginScreenDestination
import de.ljz.talktome.ui.navigation.destinations.RegisterScreenDestination
import de.ljz.talktome.ui.navigation.destinations.SetupAppThemeDestination

@LoginAndRegisterNavGraph(start = true)
@Destination(style = SlideHorizontallyAnimation::class)
@Composable
fun LoginAndRegisterScreen(
  navigator: DestinationsNavigator,
  modifier: Modifier = Modifier,
  vm: LoginViewModel
) {
  val context = LocalContext.current

  val uiState: State by vm.state.collectAsStateWithLifecycle()

  EffectCollector(effect = vm.effect) { effect ->
    when (effect) {
      Effect.NavigateRegisterScreen -> navigator.navigate(RegisterScreenDestination)
      Effect.NavigateLoginScreen -> navigator.navigate(LoginScreenDestination)
      Effect.NavigateBack -> navigator.navigateUp()
      Effect.NavigateSetupScreen -> {
        navigator.navigate(SetupAppThemeDestination)
      }
    }
  }

  TalkToMeTheme {
    LoginAndRegisterScreenContent(
      uiState = uiState,
      onAction = vm::onAction,
      modifier = modifier
    )
  }
}

@Composable
private fun LoginAndRegisterScreenContent(
  uiState: State,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface {
    ConstraintLayout(
      modifier = modifier
        .fillMaxSize()
    ) {
      val (
        iconRef,
        titleRef,
        descriptionRef,
        registerButtonRef,
        loginButtonRef
      ) = createRefs()

      Icon(
        imageVector = Icons.Outlined.AccountCircle,
        contentDescription = null,
        modifier = Modifier
          .constrainAs(iconRef) {
            top.linkTo(parent.top, 12.dp)
            start.linkTo(parent.start, 12.dp)
          }
          .size(40.dp)
      )

      Text(
        text = "Let's start with your account",
        modifier = Modifier.constrainAs(titleRef) {
          top.linkTo(iconRef.bottom, 6.dp)
          start.linkTo(parent.start, 12.dp)
          end.linkTo(parent.end, 12.dp)

          width = Dimension.fillToConstraints
        },
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Left
      )

      Text(
        text = "Easily create a new account or log in with your existing one to get started right away.",
        modifier = Modifier.constrainAs(descriptionRef) {
          top.linkTo(titleRef.bottom, 6.dp)
          start.linkTo(parent.start, 12.dp)
          end.linkTo(parent.end, 12.dp)

          width = Dimension.fillToConstraints
        },
        style = TextStyle(
          fontSize = 16.sp
        ),
      )

      Button(
        onClick = {
          onAction(Action.OnOpenRegisterButtonClick)
        },
        modifier = Modifier.constrainAs(registerButtonRef) {
          bottom.linkTo(loginButtonRef.top, 8.dp)
          start.linkTo(parent.start, 12.dp)
          end.linkTo(parent.end, 12.dp)

          width = Dimension.fillToConstraints
        }
      ) {
        Text(text = "Create account")
      }

      OutlinedButton(
        onClick = {
          onAction(Action.OnOpenLoginButtonClick)
        },
        modifier = Modifier.constrainAs(loginButtonRef) {
          bottom.linkTo(parent.bottom, 12.dp)
          start.linkTo(parent.start, 12.dp)
          end.linkTo(parent.end, 12.dp)

          width = Dimension.fillToConstraints
        }
      ) {
        Text(text = "Login")
      }
    }
  }
}

@UIModePreviews
@Composable
private fun LoginAndRegisterScreenPreview() {
  TalkToMeTheme {
    LoginAndRegisterScreenContent(
      uiState = State(),
      onAction = {}
    )
  }
}