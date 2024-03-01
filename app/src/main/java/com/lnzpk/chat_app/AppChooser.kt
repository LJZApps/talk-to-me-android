package com.lnzpk.chat_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnzpk.chat_app.old.main.StartIcon
import com.lnzpk.chat_app.rewrite.modules.getstarted.ActivityGetStarted
import com.lnzpk.chat_app.rewrite.ui.components.AppSelect
import com.lnzpk.chat_app.rewrite.ui.components.TextDescription
import com.lnzpk.chat_app.rewrite.ui.theme.TalkToMeTheme

class AppChooser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TalkToMeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ChooseApp()
                }
            }
        }
    }
}

@Composable
@Preview
fun ChooseApp() {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)

    Column {
        Text(
            text = "App-Version auswählen",
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        )

        TextDescription(
            text = "This screen is only for development!",
            modifier = Modifier.padding(5.dp)
        )

        AppSelect(
            title = "New app with Jetpack Compose",
            modifier = Modifier.padding(5.dp)
                .fillMaxWidth(),
            onClick = {
                context.startActivity(Intent(context, ActivityGetStarted::class.java))
                activity?.finish()
            }
        )

        AppSelect(
            title = "Old App with XML",
            modifier = Modifier.padding(5.dp)
                .fillMaxWidth(),
            onClick = {
                context.startActivity(Intent(context, StartIcon::class.java))
                activity?.finish()
            }
        )
    }
}