package com.jeffrwatts.celestialnavigation.addeditsight

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jeffrwatts.celestialnavigation.CelestialBodyTopAppBar
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.CelestialObjectBodyType
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            LazyColumn(Modifier.padding(paddingValues).fillMaxSize()) {
                if (!uiState.isRefreshing) {
                    items(uiState.items) {
                        CelestialBodyRow(it, onClick)
                    }
                }
            }

            PullRefreshIndicator(uiState.isRefreshing, state, Modifier.align(Alignment.TopCenter))
        }
    }
}

fun displayDec (angle: Double, positiveSign: String?=null, negativeSign: String?=null): String {
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
fun CelestialBodyRow(celestialBody: CelestialBody, onClick: (celestialBody: CelestialBody) -> Unit) {
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
        Image(painter = painterResource(id = imageResource), contentDescription = "", modifier = Modifier
            .size(96.dp)
            .padding(4.dp))
        Column() {
            Text(text = celestialBody.name, style = Typography.bodyLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(4.dp))
            Text(text = "RA: ${displayHours(celestialBody.ra)}", style = Typography.bodyLarge)
            Text(text = "dec: ${displayDec(celestialBody.dec)}", style = Typography.bodyLarge)
        }
    }
}