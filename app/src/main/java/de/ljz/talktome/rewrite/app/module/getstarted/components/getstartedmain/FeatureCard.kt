package de.ljz.talktome.rewrite.app.module.getstarted.components.getstartedmain

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import de.ljz.talktome.R
import de.ljz.talktome.rewrite.core.ui.theme.TalkToMeTheme

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    image: Painter,
    contentDescription: String? = null
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = true
            ) {
                // DO SOMETHING FUNNNYYYYYY
            }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (
                featureTitleRef,
                featureDescriptionRef,
                featureImageRef
            ) = createRefs()

            Image(
                painter = image,
                contentDescription = contentDescription,
                modifier = Modifier
                    .constrainAs(featureImageRef) {
                        top.linkTo(parent.top, 6.dp)
                        start.linkTo(parent.start, 6.dp)
                        bottom.linkTo(parent.bottom, 6.dp)
                    }
                    .size(35.dp)
                    .clip(CircleShape)
            )

            Text(
                text = title,
                modifier = Modifier
                    .constrainAs(featureTitleRef) {
                        top.linkTo(parent.top, 6.dp)
                        start.linkTo(featureImageRef.end, 6.dp)
                        end.linkTo(parent.end, 6.dp)

                        width = Dimension.fillToConstraints
                    }
                    .padding(12.dp),
                fontSize = 20.sp,
                maxLines = 1
            )

            Text(
                text = description,
                modifier = Modifier
                    .constrainAs(featureDescriptionRef) {
                        top.linkTo(featureTitleRef.bottom, 6.dp)
                        start.linkTo(featureImageRef.end, 6.dp)
                        end.linkTo(parent.end)

                        width = Dimension.fillToConstraints
                    }
                    .padding(12.dp),
            )
        }
    }
}

@Composable
@Preview
fun FeatureCardPreview() {
    TalkToMeTheme {
        FeatureCard(
            title = "FEATURE_NAME",
            description = "LONG_FEATURE_DESCRIPTION",
            image = painterResource(
                id = R.drawable.no_profile_pic
            )
        )
    }
}