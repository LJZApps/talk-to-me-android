package de.ljz.talktome.rewrite.app.module.login.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import de.ljz.talktome.rewrite.app.module.login.LoginViewModel
import de.ljz.talktome.rewrite.core.navigation.LoginNavGraph
import com.ramcosta.composedestinations.annotation.Destination

@LoginNavGraph(start = true)
@Destination
@Composable
fun LoginMain(
    modifier: Modifier = Modifier,
    vm: LoginViewModel
) {
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
            modifier = Modifier.constrainAs(iconRef) {
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
            onClick = { /*TODO*/ },
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
                //navigator.navigate() to login screen
                      vm.login()
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