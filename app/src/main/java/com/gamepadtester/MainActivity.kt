package com.gamepadtester

import android.os.Bundle
import android.view.InputDevice
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

    var controllerName by remember {
        mutableStateOf("No controller detected")
    }

    fun scanController() {

        val deviceIds = InputDevice.getDeviceIds()

        var foundController: InputDevice? = null

        for (id in deviceIds) {

            val device = InputDevice.getDevice(id)

            if (device != null) {

                val sources = device.sources

                val isGamepad =
                    (sources and InputDevice.SOURCE_GAMEPAD) ==
                            InputDevice.SOURCE_GAMEPAD

                val isJoystick =
                    (sources and InputDevice.SOURCE_JOYSTICK) ==
                            InputDevice.SOURCE_JOYSTICK

                if (isGamepad || isJoystick) {
                    foundController = device
                    break
                }
            }
        }

        controllerName =
            foundController?.name
                ?: "No controller detected"
    }

    LaunchedEffect(Unit) {
        scanController()
    }

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
            fontSize = 70.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gamepad Tester",
            color = Color.White,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Controller",
            color = Color.LightGray,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = controllerName,
            color = Color.White,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                scanController()
            }
        ) {
            Text("SCAN CONTROLLER")
        }
    }
}
