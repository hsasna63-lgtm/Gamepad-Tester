package com.gamepadtester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GamepadTesterApp()
        }
    }
}

@Composable
fun GamepadTesterApp() {

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101114))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "🎮",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gamepad Tester",
                color = Color.White,
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Test your controller",
                color = Color.LightGray,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // سنضيف فحص الجويستك هنا لاحقًا
                }
            ) {
                Text("START TEST")
            }
        }
    }
}
