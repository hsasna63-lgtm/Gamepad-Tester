package com.gamepadtester

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private var keyListener: ((KeyEvent) -> Unit)? = null
    private var motionListener: ((MotionEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GamepadTesterApp(
                registerKeyListener = { keyListener = it },
                registerMotionListener = { motionListener = it }
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        keyListener?.invoke(event)

        return if (isGamepadEvent(event)) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        keyListener?.invoke(event)

        return if (isGamepadEvent(event)) {
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {

        if (isGamepadMotionEvent(event)) {
            motionListener?.invoke(event)
            return true
        }

        return super.dispatchGenericMotionEvent(event)
    }

    private fun isGamepadEvent(event: KeyEvent): Boolean {
        return (event.source and InputDevice.SOURCE_GAMEPAD) ==
                InputDevice.SOURCE_GAMEPAD ||
                (event.source and InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK
    }

    private fun isGamepadMotionEvent(event: MotionEvent): Boolean {
        return (event.source and InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK
    }
}

data class ControllerInfo(
    val id: Int,
    val name: String,
    val descriptor: String,
    val vendorId: Int,
    val productId: Int,
    val sources: String
)

@Composable
fun GamepadTesterApp(
    registerKeyListener: (((KeyEvent) -> Unit) -> Unit),
    registerMotionListener: (((MotionEvent) -> Unit) -> Unit)
) {

    var controller by remember {
        mutableStateOf<ControllerInfo?>(null)
    }

    var pressedButtons by remember {
        mutableStateOf(setOf<String>())
    }

    var lastButton by remember {
        mutableStateOf("None")
    }

    var lastAction by remember {
        mutableStateOf("Waiting...")
    }

    var leftX by remember { mutableFloatStateOf(0f) }
    var leftY by remember { mutableFloatStateOf(0f) }

    var rightX by remember { mutableFloatStateOf(0f) }
    var rightY by remember { mutableFloatStateOf(0f) }

    var leftTrigger by remember { mutableFloatStateOf(0f) }
    var rightTrigger by remember { mutableFloatStateOf(0f) }

    var dpadX by remember { mutableFloatStateOf(0f) }
    var dpadY by remember { mutableFloatStateOf(0f) }

    var axisCount by remember { mutableIntStateOf(0) }

    var vibrationSupported by remember {
        mutableStateOf(false)
    }

    var statusMessage by remember {
        mutableStateOf("Scan for a controller")
    }

    fun findController(): InputDevice? {

        val ids = InputDevice.getDeviceIds()

        for (id in ids) {

            val device = InputDevice.getDevice(id) ?: continue

            val sources = device.sources

            val gamepad =
                (sources and InputDevice.SOURCE_GAMEPAD) ==
                        InputDevice.SOURCE_GAMEPAD

            val joystick =
                (sources and InputDevice.SOURCE_JOYSTICK) ==
                        InputDevice.SOURCE_JOYSTICK

            if (gamepad || joystick) {
                return device
            }
        }

        return null
    }

    fun scan() {

        val device = findController()

        if (device == null) {

            controller = null
            vibrationSupported = false
            statusMessage = "No controller detected"

        } else {

            val sourceText = buildList {

                if ((device.sources and InputDevice.SOURCE_GAMEPAD) != 0) {
                    add("GAMEPAD")
                }

                if ((device.sources and InputDevice.SOURCE_JOYSTICK) != 0) {
                    add("JOYSTICK")
                }

            }.joinToString(" + ")

            controller = ControllerInfo(
                id = device.id,
                name = device.name ?: "Unknown Controller",
                descriptor = device.descriptor ?: "Unknown",
                vendorId = device.vendorId,
                productId = device.productId,
                sources = sourceText
            )

            axisCount = device.motionRanges.size

            vibrationSupported =
                device.vibrator != null

            statusMessage = "Controller connected"
        }
    }

    fun vibrate(context: Context) {

        try {

            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {

                val manager =
                    context.getSystemService(
                        Context.VIBRATOR_MANAGER_SERVICE
                    ) as VibratorManager

                manager.defaultVibrator

            } else {

                @Suppress("DEPRECATION")
                context.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator
            }

            if (vibrator.hasVibrator()) {

                if (android.os.Build.VERSION.SDK_INT >= 26) {

                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            300,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

                } else {

                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }

        } catch (_: Exception) {
        }
    }

    LaunchedEffect(Unit) {

        registerKeyListener { event ->

            val device = event.device ?: return@registerKeyListener

            controller = ControllerInfo(
                id = device.id,
                name = device.name ?: "Unknown Controller",
                descriptor = device.descriptor ?: "Unknown",
                vendorId = device.vendorId,
                productId = device.productId,
                sources = "GAMEPAD / JOYSTICK"
            )

            vibrationSupported = device.vibrator != null

            val buttonName =
                friendlyButtonName(event.keyCode)

            lastButton = buttonName

            lastAction =
                if (event.action == KeyEvent.ACTION_DOWN) {
                    "PRESSED"
                } else {
                    "RELEASED"
                }

            pressedButtons =
                if (event.action == KeyEvent.ACTION_DOWN) {

                    pressedButtons + buttonName

                } else {

                    pressedButtons - buttonName
                }
        }

        registerMotionListener { event ->

            val device = event.device ?: return@registerMotionListener

            controller = ControllerInfo(
                id = device.id,
                name = device.name ?: "Unknown Controller",
                descriptor = device.descriptor ?: "Unknown",
                vendorId = device.vendorId,
                productId = device.productId,
                sources = "JOYSTICK"
            )

            vibrationSupported = device.vibrator != null

            val x = event.getAxisValue(
                MotionEvent.AXIS_X
            )

            val y = event.getAxisValue(
                MotionEvent.AXIS_Y
            )

            val rx = event.getAxisValue(
                MotionEvent.AXIS_Z
            )

            val ry = event.getAxisValue(
                MotionEvent.AXIS_RZ
            )

            val lt = event.getAxisValue(
                MotionEvent.AXIS_LTRIGGER
            )

            val rt = event.getAxisValue(
                MotionEvent.AXIS_RTRIGGER
            )

            val hatX = event.getAxisValue(
                MotionEvent.AXIS_HAT_X
            )

            val hatY = event.getAxisValue(
                MotionEvent.AXIS_HAT_Y
            )

            leftX = x
            leftY = y

            rightX = rx
            rightY = ry

            leftTrigger = lt
            rightTrigger = rt

            dpadX = hatX
            dpadY = hatY

            axisCount = device.motionRanges.size
        }

        scan()
    }

    MaterialTheme {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101114))
                .padding(16.dp)
        ) {

            item {

                Text(
                    text = "🎮 Gamepad Tester",
                    color = Color.White,
                    fontSize = 30.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Controller diagnostic tool",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1C21)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = if (controller != null)
                                "🟢 CONTROLLER CONNECTED"
                            else
                                "🔴 NO CONTROLLER",
                            color = Color.White,
                            fontSize = 19.sp
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = controller?.name
                                ?: "No controller detected",
                            color = Color.LightGray,
                            fontSize = 17.sp
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                scan()
                            }
                        ) {
                            Text("SCAN CONTROLLER")
                        }

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = statusMessage,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("ℹ️ CONTROLLER INFORMATION")

                InfoCard(
                    if (controller == null) {

                        "No controller information"

                    } else {

                        """
                        Name: ${controller!!.name}
                        Device ID: ${controller!!.id}
                        Vendor ID: ${controller!!.vendorId}
                        Product ID: ${controller!!.productId}
                        Sources: ${controller!!.sources}
                        Axes detected: $axisCount
                        Vibration: ${
                            if (vibrationSupported) "Supported"
                            else "Not detected"
                        }
                        Descriptor:
                        ${controller!!.descriptor}
                        """.trimIndent()
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("🔘 BUTTON TEST")

                InfoCard(
                    """
                    Last Button: $lastButton
                    Status: $lastAction

                    Press any controller button.
                    The button will appear here.
                    """.trimIndent()
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Currently pressed:",
                    color = Color.LightGray
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                if (pressedButtons.isEmpty()) {

                    Text(
                        text = "None",
                        color = Color.Gray
                    )

                } else {

                    pressedButtons.forEach { button ->

                        Text(
                            text = "● $button",
                            color = Color.Green,
                            fontSize = 17.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("🕹️ ANALOG STICKS")

                StickCard(
                    title = "LEFT STICK",
                    x = leftX,
                    y = leftY
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                StickCard(
                    title = "RIGHT STICK",
                    x = rightX,
                    y = rightY
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("🎚️ TRIGGERS")

                AxisCard(
                    title = "LEFT TRIGGER / L2",
                    value = leftTrigger
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AxisCard(
                    title = "RIGHT TRIGGER / R2",
                    value = rightTrigger
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("⬆️ D-PAD")

                InfoCard(
                    """
                    X: ${formatValue(dpadX)}
                    Y: ${formatValue(dpadY)}

                    Direction: ${dpadDirection(dpadX, dpadY)}
                    """.trimIndent()
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("🎯 DEAD ZONE")

                InfoCard(
                    """
                    Left Stick magnitude:
                    ${
                        formatValue(
                            stickMagnitude(leftX, leftY)
                        )
                    }

                    Right Stick magnitude:
                    ${
                        formatValue(
                            stickMagnitude(rightX, rightY)
                        )
                    }

                    Recommended dead zone:
                    0.10
                    """.trimIndent()
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("📳 VIBRATION")

                Button(
                    enabled = vibrationSupported,
                    onClick = {
                        vibrate(
                            this@GamepadTesterApp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        if (vibrationSupported)
                            "TEST VIBRATION"
                        else
                            "VIBRATION NOT DETECTED"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {

                SectionTitle("🧪 FULL TEST")

                InfoCard(
                    """
                    Controller:
                    ${
                        if (controller != null)
                            "CONNECTED"
                        else
                            "NOT CONNECTED"
                    }

                    Buttons:
                    ${
                        if (lastButton != "None")
                            "INPUT DETECTED"
                        else
                            "WAITING"
                    }

                    Analog:
                    ${
                        if (
                            abs(leftX) > 0.01f ||
                            abs(leftY) > 0.01f ||
                            abs(rightX) > 0.01f ||
                            abs(rightY) > 0.01f
                        )
                            "INPUT DETECTED"
                        else
                            "CENTERED"
                    }

                    Triggers:
                    ${
                        if (
                            leftTrigger > 0.01f ||
                            rightTrigger > 0.01f
                        )
                            "INPUT DETECTED"
                        else
                            "RELEASED"
                    }

                    D-Pad:
                    ${
                        dpadDirection(
                            dpadX,
                            dpadY
                        )
                    }
                    """.trimIndent()
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {

    Text(
        text = title,
        color = Color.White,
        fontSize = 21.sp
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )
}

@Composable
fun InfoCard(text: String) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C21)
        )
    ) {

        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 15.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun StickCard(
    title: String,
    x: Float,
    y: Float
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C21)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                StickVisualizer(
                    x = x,
                    y = y
                )

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                Column {

                    Text(
                        text = "X: ${formatValue(x)}",
                        color = Color.White
                    )

                    Text(
                        text = "Y: ${formatValue(y)}",
                        color = Color.White
                    )

                    Text(
                        text = "Magnitude: ${
                            formatValue(
                                stickMagnitude(x, y)
                            )
                        }",
                        color = Color.LightGray
                    )

                    Text(
                        text = if (
                            abs(x) < 0.10f &&
                            abs(y) < 0.10f
                        )
                            "CENTER"
                        else
                            "MOVING",
                        color = if (
                            abs(x) < 0.10f &&
                            abs(y) < 0.10f
                        )
                            Color.Green
                        else
                            Color.Yellow
                    )
                }
            }
        }
    }
}

@Composable
fun StickVisualizer(
    x: Float,
    y: Float
) {

    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0E10))
            .border(
                1.dp,
                Color.Gray,
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color.Gray, CircleShape)
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(
                    start = ((x.coerceIn(-1f, 1f) + 1f) / 2f * 100f).dp,
                    top = ((y.coerceIn(-1f, 1f) + 1f) / 2f * 100f).dp
                )
        )
    }
}

@Composable
fun AxisCard(
    title: String,
    value: Float
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C21)
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = title,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = formatValue(value),
                color = Color.White,
                fontSize = 22.sp
            )

            Text(
                text = if (value > 0.01f)
                    "ACTIVE"
                else
                    "RELEASED",
                color = if (value > 0.01f)
                    Color.Green
                else
                    Color.Gray
            )
        }
    }
}

fun friendlyButtonName(keyCode: Int): String {

    return when (keyCode) {

        KeyEvent.KEYCODE_BUTTON_A ->
            "A"

        KeyEvent.KEYCODE_BUTTON_B ->
            "B"

        KeyEvent.KEYCODE_BUTTON_X ->
            "X"

        KeyEvent.KEYCODE_BUTTON_Y ->
            "Y"

        KeyEvent.KEYCODE_BUTTON_L1 ->
            "L1 / LB"

        KeyEvent.KEYCODE_BUTTON_R1 ->
            "R1 / RB"

        KeyEvent.KEYCODE_BUTTON_L2 ->
            "L2"

        KeyEvent.KEYCODE_BUTTON_R2 ->
            "R2"

        KeyEvent.KEYCODE_BUTTON_THUMBL ->
            "LEFT STICK"

        KeyEvent.KEYCODE_BUTTON_THUMBR ->
            "RIGHT STICK"

        KeyEvent.KEYCODE_BUTTON_START ->
            "START"

        KeyEvent.KEYCODE_BUTTON_SELECT ->
            "SELECT"

        KeyEvent.KEYCODE_BUTTON_MODE ->
            "MODE / HOME"

        KeyEvent.KEYCODE_DPAD_UP ->
            "D-PAD UP"

        KeyEvent.KEYCODE_DPAD_DOWN ->
            "D-PAD DOWN"

        KeyEvent.KEYCODE_DPAD_LEFT ->
            "D-PAD LEFT"

        KeyEvent.KEYCODE_DPAD_RIGHT ->
            "D-PAD RIGHT"

        else ->
            KeyEvent.keyCodeToString(keyCode)
    }
}

fun formatValue(value: Float): String {
    return String.format(
        java.util.Locale.US,
        "%.2f",
        value
    )
}

fun stickMagnitude(
    x: Float,
    y: Float
): Float {

    return sqrt(
        x * x + y * y
    ).coerceAtMost(1f)
}

fun dpadDirection(
    x: Float,
    y: Float
): String {

    val horizontal =
        when {
            x < -0.5f -> "LEFT"
            x > 0.5f -> "RIGHT"
            else -> ""
        }

    val vertical =
        when {
            y < -0.5f -> "UP"
            y > 0.5f -> "DOWN"
            else -> ""
        }

    return listOf(
        vertical,
        horizontal
    )
        .filter { it.isNotEmpty() }
        .joinToString(" + ")
        .ifEmpty { "CENTER" }
}
