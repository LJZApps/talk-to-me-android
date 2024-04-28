package de.ljz.talktome.ui.features.loginAndRegister.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import de.ljz.talktome.ui.features.destinations.LoginScreenDestination
import de.ljz.talktome.ui.features.destinations.RegisterScreenDestination
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.State
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewModel
import de.ljz.talktome.ui.navigation.LoginAndRegisterNavGraph

@Destination<LoginAndRegisterNavGraph>(style = SlideHorizontallyAnimation::class)
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
    }
  }

  TalkToMeTheme {
    LoginScreenContent(
      uiState = uiState,
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
      onDissmissDialog = {
        vm.dismissDialog()
      }
    )
  }
}

@Composable
private fun LoginScreenContent(
  uiState: State,
  onAction: (Action) -> Unit,
  onUpdatePassword: (String) -> Unit,
  onUpdateUsername: (String) -> Unit,
  onTogglePasswordVisibility: () -> Unit,
  onDissmissDialog: () -> Unit,
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
        value = uiState.username,
        onValueChange = {
          onUpdateUsername(it)
        },
        shape = RoundedCornerShape(16.dp),
        label = { Text(text = "Username") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next
        ),
        singleLine = true
      )

      OutlinedTextField(
        modifier = Modifier
          .constrainAs(passwordRef) {
            top.linkTo(usernameRef.bottom, 8.dp)
            start.linkTo(parent.start, 12.dp)
            end.linkTo(parent.end, 12.dp)

            width = Dimension.fillToConstraints
          },
        value = uiState.password,
        onValueChange = onUpdatePassword,
        shape = RoundedCornerShape(16.dp),
        label = { Text(text = "Password") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        singleLine = true,
        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
          val image = if (uiState.passwordVisible)
            Icons.Filled.Visibility
          else Icons.Filled.VisibilityOff

          val description =
            if (uiState.passwordVisible) "Hide password" else "Show password"

          IconButton(
            onClick = onTogglePasswordVisibility
          ) {
            Icon(imageVector = image, description)
          }
        },
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
        Text(text = "Login")
      }

      if (uiState.isLoginErrorShown) {
        AlertDialog(
          onDismissRequest = {
            onDissmissDialog()
          },
          title = {
            Text(text = "Login failed")
          },
          text = {
            Text(
              text = uiState.loginErrorMessage
            )
          },
          confirmButton = {
            TextButton(
              onClick = {
                onDissmissDialog()
              }
            ) {
              Text(text = "Got it")
            }
          },
        )
      }
    }
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreview() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        username = "lnzpk.dev@gmail.com",
        password = "PASSWORD"
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {},
      onDissmissDialog = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithModal() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        username = "lnzpk.dev@gmail.com",
        password = "PASSWORD",
        isLoginErrorShown = true,
        loginErrorMessage = "Your e-mail or password is incorrect."
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {},
      onDissmissDialog = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithLoading() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        username = "lnzpk.dev@gmail.com",
        password = "PASSWORD",
        isLoading = true
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {},
      onDissmissDialog = {}
    )
  }
}

@UIModePreviews
@Composable
private fun LoginScreenPreviewWithErrors() {
  TalkToMeTheme {
    LoginScreenContent(
      uiState = State(
        username = "lnzpk.dev@gmail.com",
        password = "PASSWORD",
        loginErrorMessage = "Arschloch"
      ),
      onAction = {},
      onUpdatePassword = {},
      onUpdateUsername = {},
      onTogglePasswordVisibility = {},
      onDissmissDialog = {}
    )
  }
}