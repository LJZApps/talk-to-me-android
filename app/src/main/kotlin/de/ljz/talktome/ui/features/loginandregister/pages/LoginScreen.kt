package de.ljz.talktome.ui.features.loginandregister.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.State
import de.ljz.talktome.ui.features.loginandregister.LoginViewModel
import de.ljz.talktome.ui.navigation.LoginAndRegisterNavGraph
import de.ljz.talktome.ui.navigation.destinations.ErrorDialogDestination
import de.ljz.talktome.ui.navigation.destinations.LoginScreenDestination
import de.ljz.talktome.ui.navigation.destinations.RegisterScreenDestination
import de.ljz.talktome.ui.navigation.destinations.SetupAppThemeDestination
import io.sentry.compose.SentryTraced

@OptIn(ExperimentalComposeUiApi::class)
@LoginAndRegisterNavGraph
@Destination(style = SlideHorizontallyAnimation::class)
@Composable
fun LoginScreen(
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
    SentryTraced(tag = "login_screen") {
      LoginScreenContent(
        uiState = uiState,
        navigator = navigator,
        onAction = vm::onAction,
        modifier = modifier,
        onUpdatePassword = {
          vm.updatePassword(it)
        },
        onUpdateUsername = {
          vm.updateUsername(it)
        },
        onTogglePasswordVisibility = {
          vm.togglePasswordVisibility()
        },
      )
    }
  }
}

@Composable
private fun LoginScreenContent(
  uiState: State,
  navigator: DestinationsNavigator,
  onAction: (Action) -> Unit,
  onUpdatePassword: (String) -> Unit,
  onUpdateUsername: (String) -> Unit,
  onTogglePasswordVisibility: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface {
    ConstraintLayout(
      modifier = modifier
        .fillMaxSize()
    ) {
      val (
        iconRef,
        titleRef,
        usernameRef,
        passwordRef,
        loginButtonRef
      ) = createRefs()

      Icon(
        imageVector = Icons.AutoMirrored.Filled.Login,
        contentDescription = null,
        modifier = Modifier
          .constrainAs(iconRef) {
            top.linkTo(parent.top, 12.dp)
            start.linkTo(parent.start, 12.dp)
          }
          .size(40.dp)
      )

      Text(
        text = "Login with existing account",
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

      OutlinedTextField(
        modifier = Modifier
          .constrainAs(usernameRef) {
            top.linkTo(titleRef.bottom, 16.dp)
            start.linkTo(parent.start, 12.dp)
            end.linkTo(parent.end, 12.dp)

            width = Dimension.fillToConstraints
          },
        value = uiState.loginState.username,
        onValueChange = {
          onUpdateUsername(it)
        },
        shape = RoundedCornerShape(16.dp),
        label = { Text(text = "Email or username") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next
        ),
        singleLine = true,
        enabled = !uiState.isLoading
      )

      OutlinedTextField(
        modifier = Modifier
          .constrainAs(passwordRef) {
            top.linkTo(usernameRef.bottom, 8.dp)
            start.linkTo(parent.start, 12.dp)
            end.linkTo(parent.end, 12.dp)

            width = Dimension.fillToConstraints
          },
        value = uiState.loginState.password,
        onValueChange = onUpdatePassword,
        shape = RoundedCornerShape(16.dp),
        label = { Text(text = "Password") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        singleLine = true,
        visualTransformation = if (uiState.loginState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
          val image = if (uiState.loginState.passwordVisible)
            Icons.Filled.Visibility
          else Icons.Filled.VisibilityOff

          val description =
            if (uiState.registerState.passwordVisible) "Hide password" else "Show password"

          IconButton(
            onClick = onTogglePasswordVisibility
          ) {
            Icon(imageVector = image, description)
          }
        },
        enabled = !uiState.isLoading
      )

      Button(
        onClick = {
          onAction(Action.OnLoginButtonClick)
        },
        modifier = Modifier.constrainAs(loginButtonRef) {
          bottom.linkTo(parent.bottom, 8.dp)
          start.linkTo(parent.start, 12.dp)
          end.linkTo(parent.end, 12.dp)

          width = Dimension.fillToConstraints
        },
        enabled = !uiState.isLoading
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing)
        ) {
          AnimatedVisibility(visible = uiState.isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(ButtonDefaults.IconSize),
              strokeWidth = 2.dp
            )
          }
          AnimatedVisibility(visible = !uiState.isLoading) {
            Text("Login")
          }
          AnimatedVisibility(visible = uiState.loadingText.isNotEmpty()) {
            Text(text = uiState.loadingText)
          }
        }
      }

      if (uiState.loginState.isLoginErrorShown) {
        navigator.navigate(
          ErrorDialogDestination(
            title = "Login error",
            message = uiState.loginState.loginErrorMessage,
          )
        )
      }
    }
  }
}

/* Previews

@UIModePreviews
@Composable
private fun LoginScreenPreview() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithModal() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        loginState = LoginViewContract.LoginState(
          isLoginErrorShown = true,
          loginErrorMessage = "Your e-mail or password is incorrect."
        )
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithLoading() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        isLoading = true
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithErrors() {
  TalkToMeTheme{
    LoginScreenContent(
      uiState = State(
        loginState = LoginViewContract.LoginState(
          loginErrorMessage = "Müll"
        )
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {}
    )
  }
}*/
