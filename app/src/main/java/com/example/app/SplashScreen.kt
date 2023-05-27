package com.example.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(navController: NavHostController, version: String) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnimation = animateFloatAsState(targetValue = if(startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 3000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(4000)
        navController.popBackStack()
        navController.navigate(Screen.Home.route)
    }
    SplashScreen(alphaAnimation = alphaAnimation.value, version)
}


@Composable
fun SplashScreen(alphaAnimation: Float, version: String) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,

    ) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {


            Icon(
                painter = painterResource(id = R.drawable.emeter),
                contentDescription = "Logo Icon",
                modifier = Modifier
                    .size(256.dp)
                    .alpha(alpha = alphaAnimation),
                tint = Color.Black
            )

            Text(text = "НОЭ контрольные обходы",  fontSize = 28.sp,
                modifier = Modifier.alpha(alpha = alphaAnimation))

            
            Spacer(modifier = Modifier.height(250.dp))

            Text(text = "Version: $version",  fontSize = 20.sp,
                modifier = Modifier.alpha(alpha = alphaAnimation))

        }


    }
}

@Composable
@Preview
fun showSplash() {
    SplashScreen(alphaAnimation = 1f, "1.0.0")
}