package com.lnzpk.chat_app.rewrite.app.module.getstarted.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.lnzpk.chat_app.rewrite.app.module.getstarted.GetStartedViewModel
import com.lnzpk.chat_app.rewrite.core.navigation.GetStartedNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@GetStartedNavGraph
@Destination
@Composable
fun GetStartedNews(
    modifier: Modifier = Modifier,
    vm: GetStartedViewModel,
    navigator: DestinationsNavigator,
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val featurePages = listOf(
            "first",
            "second",
            "third"
        )
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { featurePages.size })
        val coroutineScope = rememberCoroutineScope()
        val (
            titleRef,
            pagerRef,
            dotsRef,
            nextAndSkipButtonRef,
            getStartedButtonRef
        ) = createRefs()

        Text(
            text = "Features",
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    width = Dimension.fillToConstraints
                }
                .padding(8.dp),
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.constrainAs(pagerRef) {
                top.linkTo(titleRef.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                if (pagerState.currentPage == featurePages.lastIndex) {
                    bottom.linkTo(getStartedButtonRef.top)
                } else {
                    bottom.linkTo(nextAndSkipButtonRef.top)
                }

                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) { page ->
            ElevatedCard(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
            }
        }

        if (pagerState.currentPage == featurePages.lastIndex) {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(getStartedButtonRef) {
                        top.linkTo(pagerRef.bottom)
                        start.linkTo(parent.start, 8.dp)
                        end.linkTo(parent.end, 8.dp)
                        bottom.linkTo(parent.bottom, 8.dp)

                        width = Dimension.fillToConstraints
                    },
            ) {
                Text(text = "Setup your account")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(nextAndSkipButtonRef) {
                        top.linkTo(pagerRef.bottom)
                        start.linkTo(parent.start, 8.dp)
                        end.linkTo(parent.end, 8.dp)
                        bottom.linkTo(parent.bottom, 8.dp)

                        width = Dimension.fillToConstraints
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(featurePages.lastIndex)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(end = 4.dp),
                ) {
                    Text(text = "Skip")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 4.dp),
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}