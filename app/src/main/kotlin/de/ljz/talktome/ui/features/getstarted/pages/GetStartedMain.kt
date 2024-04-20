package de.ljz.talktome.ui.features.getstarted.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ljz.talktome.R
import de.ljz.talktome.ui.features.NavGraphs
import de.ljz.talktome.ui.features.getstarted.GetStartedViewModel
import de.ljz.talktome.ui.navigation.GetStartedNavGraph
import de.ljz.talktome.util.bounceClick

@GetStartedNavGraph(start = true)
@Destination
@Composable
fun GetStartedMain(
  modifier: Modifier = Modifier,
  navigator: DestinationsNavigator,
  vm: GetStartedViewModel
) {
  ConstraintLayout(
    modifier = modifier
      .fillMaxSize()
  ) {
    val (
      logoAndTitleRef,
    ) = createRefs()

    Column(
      modifier = Modifier
        .constrainAs(logoAndTitleRef) {
          top.linkTo(parent.top)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          bottom.linkTo(parent.bottom)

          width = Dimension.fillToConstraints
        },
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Welcome to",
        modifier = Modifier
          .padding(bottom = 24.dp),
        textAlign = TextAlign.Center,
        fontSize = 32.sp,
      )

      Image(
        painter = painterResource(id = R.drawable.ic_launcher_playstore_new),
        contentDescription = "Talk to me logo",
        modifier = Modifier
          .fillMaxWidth(0.5f)
          .clip(RoundedCornerShape(20.dp)),
        contentScale = ContentScale.FillWidth
      )

      Button(
        onClick = {
          navigator.navigate(NavGraphs.loginAndRegister)
        },
        modifier = Modifier
          .padding(top = 24.dp)
          .bounceClick()
      ) {
        Image(
          imageVector = Icons.AutoMirrored.Filled.NavigateNext,
          contentDescription = null
        )
      }
    }
  }
}