package com.gamepadtester

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

    private var onGamepadKey: ((KeyEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GamepadTesterApp(
                registerKeyListener = { listener ->
                    onGamepadKey = listener
                }
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        onGamepadKey?.invoke(event)

        if (event.source and InputDevice.SOURCE_GAMEPAD ==
            InputDevice.SOURCE_GAMEPAD
        ) {
            return true
        }

        if (event.source and InputDevice.SOURCE_JOYSTICK ==
            InputDevice.SOURCE_JOYSTICK
        ) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {

        onGamepadKey?.invoke(event)

        if (event.source and InputDevice.SOURCE_GAMEPAD ==
            InputDevice.SOURCE_GAMEPAD
        ) {
            return true
        }

        if (event.source and InputDevice.SOURCE_JOYSTICK ==
            InputDevice.SOURCE_JOYSTICK
        ) {
            return true
        }

        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun GamepadTesterApp(
    registerKeyListener: (((KeyEvent) -> Unit) -> Unit)
) {

    var controllerName by remember {
        mutableStateOf("No controller detected")
    }

    var pressedButton by remember {
        mutableStateOf("None")
    }

    var buttonAction by remember {
        mutableStateOf("Waiting...")
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

        registerKeyListener { event ->

            val device = event.device

            if (device != null) {

                val isGamepad =
                    (event.source and InputDevice.SOURCE_GAMEPAD) ==
                            InputDevice.SOURCE_GAMEPAD

                val isJoystick =
                    (event.source and InputDevice.SOURCE_JOYSTICK) ==
                            InputDevice.SOURCE_JOYSTICK

                if (isGamepad || isJoystick) {

                    controllerName = device.name

                    pressedButton =
                        KeyEvent.keyCodeToString(event.keyCode)

                    buttonAction =
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            "PRESSED"
                        } else {
                            "RELEASED"
                        }
                }
            }
        }

        scanController()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101114))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "🎮 Gamepad Tester",
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Controller",
            color = Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = controllerName,
            color = Color.White,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "BUTTON TEST",
            color = Color.LightGray,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = pressedButton,
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buttonAction,
            color = Color.Green,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(35.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    scanController()
                }
            ) {
                Text("SCAN")
            }
        }
    }
}
