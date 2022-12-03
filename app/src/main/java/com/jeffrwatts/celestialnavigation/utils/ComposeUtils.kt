package com.jeffrwatts.celestialnavigation.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import kotlin.math.pow

val fieldModifiers = Modifier
    .width(80.dp)

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
        TextField(value = if (textIcMinutes.isEmpty() and (icMinutes==0.0)) "" else icMinutes.toString(),
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

@Composable
fun CelestialBodyDropDown(
    onGetSight: (selected: String)->Unit
) {
    var selected by remember { mutableStateOf("None") }
    var expanded by remember { mutableStateOf(false) }
    val listItems = stringArrayResource(R.array.celestial_bodies)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Sight: ", style = Typography.titleLarge)
        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = selected, style = Typography.titleLarge)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, stringResource(id = R.string.app_name))
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                listItems.forEach { itemValue ->
                    DropdownMenuItem(text = { Text(text = itemValue, style = Typography.bodyLarge) },
                        onClick = {
                            selected = itemValue
                            expanded = false
                        })
                    Divider()
                }
            }
        }
        Button(onClick = { onGetSight(selected) }, enabled = (selected != "None")) {
            Text("Get GP")
        }
    }
}

@Composable
fun LimbDropDown(
    onValueChanged: (limb: CelNavUtils.Limb) -> Unit
) {
    var selected by remember { mutableStateOf(CelNavUtils.Limb.Center) }
    var expanded by remember { mutableStateOf(false) }

    val listItems = listOf(CelNavUtils.Limb.Upper, CelNavUtils.Limb.Lower, CelNavUtils.Limb.Center)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Limb: ", style = Typography.titleLarge)
        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(text = limbToDisplayName(selected), style = Typography.titleLarge)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, stringResource(id = R.string.app_name))
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                listItems.forEach { itemValue ->
                    DropdownMenuItem(text = { Text(text = limbToDisplayName(itemValue), style = Typography.bodyLarge) },
                        onClick = {
                            selected = itemValue
                            onValueChanged(itemValue)
                            expanded = false
                        })
                    Divider()
                }
            }
        }
    }
}

fun limbToDisplayName(limb: CelNavUtils.Limb): String {
    return when (limb) {
        CelNavUtils.Limb.Upper -> "Upper Limb"
        CelNavUtils.Limb.Lower -> "Lower Limb"
        CelNavUtils.Limb.Center -> "Center"
    }
}

@Composable
fun UTC (utc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "UTC:", style = Typography.titleLarge)
        Text(text = utc, style = Typography.bodyLarge)
    }
}

@Composable
fun MinutesValue(label: String, minutes: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = Typography.titleLarge)
        Text(text = minutes.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "'", style = Typography.bodyLarge)
    }
}

@Composable
fun LoadingContent(
    loading: Boolean,
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = onRefresh,
            modifier = modifier,
            content = content,
        )
    }
}