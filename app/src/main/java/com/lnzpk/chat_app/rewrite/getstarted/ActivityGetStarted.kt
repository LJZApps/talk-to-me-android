package com.lnzpk.chat_app.rewrite.getstarted

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.lnzpk.chat_app.rewrite.ui.components.TextDescription
import com.lnzpk.chat_app.rewrite.ui.components.TextTitle
import com.lnzpk.chat_app.rewrite.ui.theme.TalkToMeTheme
import kotlinx.coroutines.launch

private const val TAG = "ActivityGetStarted"
class ActivityGetStarted : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            TalkToMeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) {innerPadding ->
                    ConstraintLayout (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val (
                            titleRef,
                            descriptionRef,
                            nextButtonRef
                        ) = createRefs()

                        TextTitle(
                            text = "Welcome to the new\nTalk to me!",
                            modifier = Modifier.constrainAs(titleRef) {
                                top.linkTo(parent.top, 12.dp)
                                start.linkTo(parent.start, 12.dp)
                                end.linkTo(parent.end, 12.dp)
                            }
                        )

                        TextDescription(
                            text = "With this update, we made some huge improvements to the app.\n\n" +
                                    "The app got a brand new design while adding many new features.",
                            modifier = Modifier.constrainAs(descriptionRef) {
                                top.linkTo(titleRef.bottom, 12.dp)
                                start.linkTo(parent.start, 12.dp)
                                end.linkTo(parent.end, 12.dp)

                                width = Dimension.fillToConstraints
                            }
                        )

                        Button(
                            onClick = {

                            },
                            modifier = Modifier.constrainAs(nextButtonRef) {
                                bottom.linkTo(parent.bottom, 12.dp)
                                start.linkTo(parent.start, 12.dp)
                                end.linkTo(parent.end, 12.dp)

                                width = Dimension.fillToConstraints
                            }
                        ) {
                            Text(
                                text = "Get started"
                            )
                        }
                    }
                }
            }
        }
    }

}