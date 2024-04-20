package de.ljz.talktome.ui.features.loginAndRegister.pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinci.androidtemplate.ui.navigation.animations.SlideHorizontallyAnimation
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewModel
import de.ljz.talktome.ui.navigation.LoginAndRegisterNavGraph

@LoginAndRegisterNavGraph
@Destination(style = SlideHorizontallyAnimation::class)
@Composable
fun RegisterScreen(
  navigator: DestinationsNavigator,
  modifier: Modifier = Modifier,
  vm: LoginViewModel
) {
  val uiState: LoginViewContract.State by vm.state.collectAsStateWithLifecycle()

  Text(text = uiState.count.toString())
}