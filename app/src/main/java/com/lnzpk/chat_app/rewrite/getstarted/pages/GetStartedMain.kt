package com.lnzpk.chat_app.rewrite.getstarted.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.glance.action.actionStartActivity
import androidx.navigation.NavController
import com.lnzpk.chat_app.rewrite.getstarted.*

@Composable
fun GetStartedMain(
    modifier: Modifier = Modifier,
    navController: NavController,
    vm: GetStartedViewModel
) {
    ConstraintLayout (
        modifier = modifier.fillMaxSize()
    ) {
        val (
            titleRef,
            descriptionRef,
            nextButtonRef,
            logoRef
        ) = createRefs()

        Text(
            text = "Welcome to Talk to me",
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    width = Dimension.fillToConstraints
                }
                .padding(12.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        Text(
            text = "This version has new features and a new interface.",
            modifier = Modifier.constrainAs(descriptionRef) {
                top.linkTo(titleRef.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)

                width = Dimension.fillToConstraints
            },
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )

        Button(
            onClick = {
                  navController.navigate(GET_STARTED_NEWS)
            },
            modifier = Modifier.constrainAs(nextButtonRef) {
                bottom.linkTo(parent.bottom, 6.dp)
                start.linkTo(parent.start, 6.dp)
                end.linkTo(parent.end, 6.dp)

                width = Dimension.fillToConstraints
            }
        ) {
            Text(
                text = "Get started",
                textAlign = TextAlign.Center,
            )
        }

        /*
        LazyColumn{
            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ElevatedCard (
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {}
                ) {
                    Column (
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Consectetur pariatur aute nulla ex occaecat. Velit nostrud est eiusmod. Cupidatat eiusmod culpa Lorem nulla duis enim mollit consectetur do adipisicing laborum aliquip incididunt consequat voluptate.",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
         */
        /*
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
         */
    }
}