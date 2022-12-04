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

fun intValid (intText: String, limit: Int? = null): Boolean {
    if (intText.isEmpty()) return true
    val int = intText.toIntOrNull() ?: return false
    return if (limit != null) int <= limit else true
}

fun minutesValid (minutes: String): Boolean {
    if (minutes.isEmpty()) return true
    val minutesDouble = minutes.toDoubleOrNull() ?: return false
    return minutesDouble < 60.0
}

fun computeIcMinutes (minutes: String, icDirection: CelNavUtils.ICDirection): Double {
    val minutesDouble = if (minutes.isEmpty()) 0.0 else minutes.toDouble()
    return if (icDirection == CelNavUtils.ICDirection.On) minutesDouble*-1 else minutesDouble
}

@Composable
fun AngleDisplay(label: String, angle: Double, signPositive: String? = null, signNegative: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (degrees, minutes, sign) = CelNavUtils.degreesMinutesSign(angle)
        Text(text = label, style = Typography.titleLarge)
        Text(text = degrees.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "°", style = Typography.bodyLarge)
        Text(text = minutes.toString(), style = Typography.bodyLarge, modifier = fieldModifiers, textAlign = TextAlign.End)
        Text(text = "'", style = Typography.bodyLarge)

        if (!signNegative.isNullOrEmpty() or !signPositive.isNullOrEmpty()) {
            Text("${if (sign == 1) signPositive else signNegative }", style = Typography.titleLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AngleInput(label: String, onValueChanged: (newValue: Double)->Unit, initialValue: Double, maxDegrees: Int, signPositive: String? = null, signNegative: String? = null) {
    var initialize by remember { mutableStateOf(true) }
    var textInputDegrees by remember { mutableStateOf("") }
    var textInputMinutes by remember { mutableStateOf("") }
    var inputSign by remember { mutableStateOf(1) }

    if (initialize) {
        val (degrees, minutes, sign) = CelNavUtils.degreesMinutesSign(initialValue)
        textInputDegrees = degrees.toString()
        textInputMinutes = minutes.toString()
        inputSign = sign
        initialize = false
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = Typography.titleLarge)
        TextField(
            value = textInputDegrees,
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (intValid(it, maxDegrees)) {
                    textInputDegrees = it
                    val degrees = if (it.isEmpty()) 0 else it.toInt()
                    val minutes = if (textInputMinutes.isEmpty()) 0.0 else textInputMinutes.toDouble()
                    onValueChanged(CelNavUtils.angle(degrees, minutes, inputSign))
                }
            })
        Text(text = "°", style = Typography.bodyLarge)
        TextField(
            value = textInputMinutes,
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (minutesValid(it)) {
                    textInputMinutes = it
                    val degrees = if (textInputDegrees.isEmpty()) 0 else textInputDegrees.toInt()
                    val minutes = if (it.isEmpty()) 0.0 else it.toDouble()
                    onValueChanged(CelNavUtils.angle(degrees, minutes, inputSign))
                }
            })
        Text(text = "'", style = Typography.bodyLarge)
        if (!signNegative.isNullOrEmpty() or !signPositive.isNullOrEmpty()) {
            Button(onClick = {
                inputSign *= -1
                val degrees = if (textInputDegrees.isEmpty()) 0 else textInputDegrees.toInt()
                val minutes = if (textInputMinutes.isEmpty()) 0.0 else textInputMinutes.toDouble()
                onValueChanged(CelNavUtils.angle(degrees, minutes, inputSign))
            }) {
                Text("${if (inputSign == 1) signPositive else signNegative }")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IC(onValueChanged: (minutes: Double)->Unit, initialValue: Double) {
    var initialize by remember { mutableStateOf(true) }
    var textIcMinutes by remember { mutableStateOf("") }
    var icDirection by remember { mutableStateOf(CelNavUtils.ICDirection.Off) }

    if (initialize) {
        var initialIc = initialValue
        if (initialIc < 0.0) {
            icDirection = CelNavUtils.ICDirection.On
            initialIc *= -1.0
        }
        textIcMinutes = initialIc.toString()
        initialize = false
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "IC: ", style = Typography.titleLarge)
        TextField(
            value = textIcMinutes,
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (minutesValid(it)) {
                    textIcMinutes = it
                    onValueChanged(computeIcMinutes(it, icDirection))
                }
            })
        Text(text = "'", style = Typography.bodyLarge)
        Button(onClick = {
            icDirection = if (icDirection == CelNavUtils.ICDirection.On) CelNavUtils.ICDirection.Off else CelNavUtils.ICDirection.On
            onValueChanged(computeIcMinutes(textIcMinutes, icDirection))
        })
        {
            Text(if (icDirection == CelNavUtils.ICDirection.On) "On" else "Off")
        }
        Text(text = if (icDirection == CelNavUtils.ICDirection.On) "(Sub)" else "(Add)", style = Typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dip(dip: Double, onValueChanged: (newValue: Int)->Unit, initialValue: Int) {
    var initialize by remember { mutableStateOf(true) }
    var textEyeHeight by remember { mutableStateOf("") }

    if (initialize) {
        textEyeHeight = initialValue.toString()
        initialize = false
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "EyeHeight: ", style = Typography.titleLarge)
        TextField(value = textEyeHeight,
            textStyle = Typography.bodyLarge,
            modifier = fieldModifiers,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                if (intValid(it)) {
                    textEyeHeight = it
                    val eyeHeight = if (it.isEmpty()) 0 else it.toInt()
                    onValueChanged(eyeHeight)
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