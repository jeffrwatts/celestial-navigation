package com.jeffrwatts.celestialnavigation.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import kotlin.math.pow

val fieldModifiers = Modifier
    .width(75.dp)

@Composable
fun AngleDisplay(label: String, angle: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (degrees, minutes, _) = CelNavUtils.degreesMinutesSign(angle)
        Text(text = label, style = Typography.titleLarge)
        Text(text = degrees.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "°", style = Typography.bodyLarge)
        Text(text = minutes.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "'", style = Typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AngleInput(label: String, angle: Double, onValueChanged: (newValue: Double)->Unit, signPositive: String? = null, signNegative: String? = null) {
    var textInputDegrees by remember { mutableStateOf("") }
    var textInputMinutes by remember { mutableStateOf("") }
    val (degrees, minutes, sign) = CelNavUtils.degreesMinutesSign(angle)

    // TODO: Figure out the initialization case problem where I need to put (angle==0.0)

    Row(verticalAlignment = Alignment.CenterVertically) {

        Text(text = label, style = Typography.titleLarge)
        TextField(value = if (textInputDegrees.isEmpty() and (angle==0.0)) "" else degrees.toString(),
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                textInputDegrees = it
                val newDegrees = textToDegrees(it)
                if (newDegrees != -1) {
                    onValueChanged(CelNavUtils.angle(newDegrees, minutes, sign))
                }
            })
        Text(text = "°", style = Typography.bodyLarge)
        TextField(value = if (textInputMinutes.isEmpty() and (angle==0.0)) "" else minutes.toString(),
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                textInputMinutes = it
                val newMinutes = textToMinutes(it)
                if (newMinutes != -1.0) {
                    onValueChanged(CelNavUtils.angle(degrees, newMinutes, sign))
                }
            })
        Text(text = "'", style = Typography.bodyLarge)
        if (!signNegative.isNullOrEmpty() or !signPositive.isNullOrEmpty()) {
            Button(onClick = {
                val newSign = if (sign == 1) -1 else 1
                onValueChanged(CelNavUtils.angle(degrees, minutes, newSign))
            }) {
                Text("${if (sign == 1) signPositive else signNegative }")
            }
        }
    }
}

fun textToDegrees (text: String): Int {
    return try {
        val degrees =  text.toInt()
        if (degrees < 360) degrees else -1
    } catch (e: NumberFormatException) {
        -1
    }
}

fun textToMinutes (text: String): Double {
    return try {
        val degrees =  text.toDouble()
        if (degrees < 60) degrees else -1.0
    } catch (e: NumberFormatException) {
        -1.0
    }
}

fun calculateNewMinutes(newValue: String, prevMinutes: Double, prevDecimalPrecision: Int): Pair<Double, Int> {
    var minutes = if (newValue.isEmpty()) 0.0 else newValue.toDouble()
    var decimalIndex = newValue.indexOf('.')

    if (decimalIndex == -1) {
        // The '.' was just deleted, adjust the new value by the last known decimal precision.
        minutes /= 10.0.pow(prevDecimalPrecision)
        decimalIndex = minutes.toString().indexOf('.')
    }

    if (minutes >=60) {
        minutes = prevMinutes
    }

    val newDecimalPrecision = minutes.toString().length-decimalIndex-1

    return Pair(minutes, newDecimalPrecision)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IC(icMinutes: Double, onValueChanged: (minutes: Double)->Unit) {
    var textIcMinutes by remember { mutableStateOf("") }
    val icDirection = if (icMinutes < 0) CelNavUtils.ICDirection.On else CelNavUtils.ICDirection.Off

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "IC: ", style = Typography.titleLarge)
        TextField(value = if (textIcMinutes.isEmpty()) "" else icMinutes.toString(),
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                textIcMinutes = it
                val newMinutes = textToMinutes(it)
                if (newMinutes != -1.0) {
                    onValueChanged(newMinutes)
                }
            })
        Button(onClick = {
            val newValue = icMinutes * -1
            onValueChanged(newValue)
        })
        {
            Text(if (icDirection == CelNavUtils.ICDirection.On) "On" else "Off")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dip(eyeHeight: Int, dip: Double, onValueChanged: (newValue: Int)->Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "EyeHeight: ", style = Typography.titleLarge)
        TextField(value = eyeHeight.toString(),
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                if (it.isNotEmpty()) {
                    onValueChanged(it.toInt())
                }
            })
        Text(text = "Dip:", style = Typography.titleLarge)
        Text(text = "$dip", style = Typography.bodyLarge)
    }
}

@Composable
fun Dec(angle: Double) {
    // To distinguish declination from latitude the convention is to put the hemisphere
    // before the angle when referring to declination, and after when referring to latitude.
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (degrees, minutes, sign) = CelNavUtils.degreesMinutesSign(angle)
        Text(text = "dec:", style = Typography.titleLarge)
        Text(text = if (sign > 0) "N" else "S", style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = degrees.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "°", style = Typography.bodyLarge)
        Text(text = minutes.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "'", style = Typography.bodyLarge)
    }
}

@Composable
fun Intercept (intercept: Double, direction: CelNavUtils.LOPDirection) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "a:", style = Typography.titleLarge)
        Text(text = "$intercept nm; ${if (direction == CelNavUtils.LOPDirection.Away) "Away" else "Towards"}", style = Typography.bodyLarge)
    }
}