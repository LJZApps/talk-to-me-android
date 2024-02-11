package com.lnzpk.chat_app.rewrite.getstarted

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.constraintlayout.compose.ConstraintLayout
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
                            buttonRef
                        ) = createRefs()

                        Button(
                            modifier = Modifier.constrainAs(buttonRef) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "YOU ARE DEAD\nAnd a",
                                        actionLabel = "AND THIS IS A dfoksdoifjsdoifj",
                                        duration = SnackbarDuration.Short,
                                        withDismissAction = true
                                    )
                                }
                            }
                        )
                        {
                            Text(text = "Click me")
                        }
                    }
                }
            }
        }
    }

}