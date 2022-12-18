package com.jeffrwatts.celestialnavigation.celestialbody

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.CelestialBodyTopAppBar
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.CelestialObjectBodyType
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun CelestialBodyScreen(
    onClick:(celestialBody: CelestialBody) -> Unit,
    onBack:() -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CelestialBodyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = rememberPullRefreshState(uiState.isRefreshing, viewModel::refresh)

    Scaffold(
        topBar = { CelestialBodyTopAppBar(onBack) },
        modifier = modifier.fillMaxSize())
    { paddingValues ->
        Box(Modifier.pullRefresh(state)) {
            LazyColumn(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()) {
                if (!uiState.isRefreshing) {
                    itemsIndexed(uiState.items) { index, item ->
                        CelestialBodyRow(item, uiState.itemsVisibility[index], onClick)
                    }
                }
            }

            PullRefreshIndicator(uiState.isRefreshing, state,
                Modifier
                    .padding(paddingValues)
                    .align(Alignment.TopCenter))
        }
    }
}

fun displayDegrees (angle: Double, positiveSign: String?=null, negativeSign: String?=null): String {
    val (deg, min, sign) = CelNavUtils.degreesMinutesSign(angle)
    var display = "${deg}° ${min}'"
    if (!positiveSign.isNullOrEmpty() and !negativeSign.isNullOrEmpty()) {
        display += if (sign == 1) "$positiveSign" else "$negativeSign"
    }
    return display
}

fun displayHours (angle: Double, positiveSign: String?=null, negativeSign: String?=null): String {
    val (deg, min, sign) = CelNavUtils.degreesMinutesSign(angle)
    var display = "${deg} hrs ${min} min"
    if (!positiveSign.isNullOrEmpty() and !negativeSign.isNullOrEmpty()) {
        display += if (sign == 1) "$positiveSign" else "$negativeSign"
    }
    return display
}

@Composable
fun CelestialBodyRow(celestialBody: CelestialBody, visibility: CelestialBodyVisibilty, onClick: (celestialBody: CelestialBody) -> Unit) {
    val imageResource = when(celestialBody.objtype) {
        CelestialObjectBodyType.Sun -> R.drawable.sun
        CelestialObjectBodyType.Moon -> R.drawable.moon
        CelestialObjectBodyType.Planet -> {
            when (celestialBody.name) {
                "Venus" -> R.drawable.venus
                "Mars" -> R.drawable.mars
                "Jupiter" -> R.drawable.jupiter
                "Saturn" -> R.drawable.saturn
                else -> R.drawable.saturn // Need some default
            }
        }
        CelestialObjectBodyType.Star -> R.drawable.star
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .padding(8.dp)
        .clickable { onClick(celestialBody) }) {
        Column() {
            Row() {
                Image(painter = painterResource(id = imageResource), contentDescription = "", modifier = Modifier
                    .size(96.dp)
                    .padding(4.dp))
                Column() {
                    Text(text = celestialBody.name, style = Typography.bodyLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "RA: ", style = Typography.bodyLarge)
                        Text(text = displayHours(celestialBody.ra), style = Typography.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (degrees, minutes, sign) = CelNavUtils.degreesMinutesSign(celestialBody.dec)
                        Text(text = "dec:", style = Typography.bodyLarge)
                        Text(text = if (sign > 0) "N" else "S", style = Typography.bodyLarge)
                        Text(text = degrees.toString(), style = Typography.bodyLarge)
                        Text(text = "°", style = Typography.bodyLarge)
                        Text(text = minutes.toString(), style = Typography.bodyLarge)
                        Text(text = "'", style = Typography.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Visibility: ", style = Typography.bodyLarge)
                        val (text, color) = when (visibility) {
                            CelestialBodyVisibilty.AlwaysAboveHorizon -> Pair("Always Above Horizon", Color(0, 200, 0))
                            CelestialBodyVisibilty.AlwaysBelowHorizon -> Pair("Always Below Horizon", Color(255, 0, 0))
                            CelestialBodyVisibilty.AboveHorizon -> Pair("Above Horizon", Color(0, 200, 0))
                            CelestialBodyVisibilty.BelowHorizon -> Pair("Below Horizon", Color(255, 0, 0))
                            CelestialBodyVisibilty.OutOfDate -> Pair("Out Of Date", Color(255, 0, 0))
                        }
                        Text(text = text, style = Typography.bodyLarge, color = color)
                    }
                }
            }
            /*
            Row() {
                Column() {
                    celestialBody.riseset.forEach {
                        Row() {
                            Text(text = if (it.riseset == RiseSetType.Rise) "Rise" else "Set")
                            val datetimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            val offset: Int = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
                            datetimeUTC.timeInMillis = (it.utc * 1000.0).toLong() + offset
                            val date = DateFormat.format("MM-dd-yyyy hh:mm:ss a", datetimeUTC)
                            Text(text = "at: $date")
                        }
                    }
                }
            }
            */
        }
    }
}