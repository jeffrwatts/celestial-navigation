package com.jeffrwatts.celestialnavigation.addsight

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import com.jeffrwatts.celestialnavigation.utils.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AddSightScreen(
    onSightUpdate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddSightViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
){
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AddSightTopAppBar(R.string.add_sight, onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveSight) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.save_sight))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ConstraintLayoutContent(uiState,
                viewModel::getGeoPosition,
                viewModel::setHs,
                viewModel::setIc,
                viewModel::setEyeHeight,
                viewModel::setLimb,
                paddingValues)
        }

        // Check if the task is saved and call onTaskUpdate event
        LaunchedEffect(uiState.isSightSaved) {
            if (uiState.isSightSaved) {
                onSightUpdate()
            }
        }

        uiState.userMessage?.let{ userMessage->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSightTopAppBar(@StringRes title: Int, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

val rowSpacer = 8.dp
val inputRowSpacer = 32.dp

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

fun limbToDisplayName(limb: CelNavUtils.Limb): String {
    return when (limb) {
        CelNavUtils.Limb.Upper -> "Upper Limb"
        CelNavUtils.Limb.Lower -> "Lower Limb"
        CelNavUtils.Limb.Center -> "Center"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstraintLayoutContent(uiState: AddEditSightUiState,
                            onGetGeographicalPosition: ()->Unit,
                            onHsValueChanged: (newValue: Double)->Unit,
                            onICValueChanged: (minutes: Double)->Unit,
                            onEyeHeightValueChanged: (eyeHeight: Int)->Unit,
                            onLimbValueChanged: (limb: CelNavUtils.Limb) -> Unit,
                            paddingValues: PaddingValues, ) {
    ConstraintLayout(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()) {
        // Create references for the composables to constrain
        val (sightLabelField, sightField, getGPButton) = createRefs()
        val (timeLabelField, timeField) = createRefs()
        val (ghaLabelField, ghaDegreesField, ghaMinutesField)= createRefs()
        val (decLabelField, decDecField, decDegreesField, decMinutesField) = createRefs()
        val (HsLabelField, HsDegrees, HsDegreesDeg, HsMinutes, HsMinutesMin) = createRefs()
        val (ICLabelField, ICMinutes, ICMinutesMin, ICDirectionButton) = createRefs()
        val (eyeHeightLabelField, eyeHeightField, DIPLabelField, DIPField) = createRefs()
        val (limbLabelField, limbDropDownField) = createRefs()
        val (refrLabelField, refrField) = createRefs()
        val (SDLabelField, SDField) = createRefs()
        val (HPLabelField, HPField) = createRefs()
        val (HoLabelField, HoDegreesField, HoMinutesField) = createRefs()
        val (latLabelField, latDegreesField, latMinutesField, latSignField) = createRefs()
        val (lonLabelField, lonDegreesField, lonMinutesField, lonSignField) = createRefs()
        val (HcLabelField, HcDegreesField, HcMinutesField)= createRefs()
        val (ZnLabelField, ZnDegreesField, ZnMinutesField)= createRefs()
        val (interceptLabelField, interceptField)= createRefs()
        val (divider1, divider2, divider3) = createRefs()

        val labelGuide = createGuidelineFromAbsoluteLeft(8.dp)
        val degreesGuide = createGuidelineFromAbsoluteLeft(180.dp)
        val minutesGuide = createGuidelineFromAbsoluteLeft(300.dp)

        // Celestial Sight and GP
        Text("Sight:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(sightLabelField){
            top.linkTo(parent.top, margin = rowSpacer)
            start.linkTo(labelGuide)
        })

        Text(uiState.celestialBody, style = Typography.bodyLarge, modifier = Modifier.constrainAs(sightField){
            start.linkTo(sightLabelField.end, 24.dp)
            centerVerticallyTo(sightLabelField)
        })

        Button(onClick = { onGetGeographicalPosition() }, modifier = Modifier.constrainAs(getGPButton){
            start.linkTo(sightField.end, 24.dp)
            centerVerticallyTo(sightLabelField)
        }) {
            Text(text = "Get Geo Position")
        }

        // Time
        Text("Time:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(timeLabelField){
            top.linkTo(getGPButton.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })

        Text(uiState.utc, style = Typography.bodyLarge, modifier = Modifier.constrainAs(timeField){
            start.linkTo(timeLabelField.end, margin = 16.dp)
            centerVerticallyTo(timeLabelField)
        })

        // GHA
        val (ghaDegrees, ghaMinutes, _) = CelNavUtils.degreesMinutesSign(uiState.gha)
        Text("GHA:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(ghaLabelField){
            top.linkTo(timeLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$ghaDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(ghaDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(ghaLabelField)
        })
        Text("$ghaMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(ghaMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(ghaLabelField)
        })

        // dec
        val (decDegrees, decMinutes, decSign) = CelNavUtils.degreesMinutesSign(uiState.dec)
        Text("dec:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(decLabelField){
            top.linkTo(ghaLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$decDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(decDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(decLabelField)
        })
        Text("$decMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(decMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(decLabelField)
        })
        Text(text = if (decSign>=0) "N" else "S", style = Typography.bodyLarge, modifier = Modifier.constrainAs(decDecField){
            end.linkTo(decDegreesField.start, margin = 24.dp)
            centerVerticallyTo(decLabelField)
        })

        Divider(thickness = 2.dp, modifier = Modifier.constrainAs(divider1){
            top.linkTo(decLabelField.bottom, margin = rowSpacer)
        })

        // Hs
        var inputDegreesHs by remember { mutableStateOf("") }
        var inputMinutesHs by remember { mutableStateOf("") }

        Text("Hs:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(HsLabelField){
            top.linkTo(divider1.bottom, margin = inputRowSpacer)
            start.linkTo(labelGuide)
        })
        TextField(
            value = inputDegreesHs,
            textStyle = Typography.bodyLarge,
            modifier = Modifier
                .width(70.dp)
                .constrainAs(HsDegrees) {
                    end.linkTo(degreesGuide)
                    centerVerticallyTo(HsLabelField)
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (intValid(it, 90)) {
                    inputDegreesHs = it
                    val degrees = if (it.isEmpty()) 0 else it.toInt()
                    val minutes = if (inputMinutesHs.isEmpty()) 0.0 else inputMinutesHs.toDouble()
                    onHsValueChanged(CelNavUtils.angle(degrees, minutes))
                }
            })
        Text(text = "°", style = Typography.titleLarge, modifier = Modifier.constrainAs(HsDegreesDeg){
            start.linkTo(HsDegrees.end, 2.dp)
            centerVerticallyTo(HsLabelField)
        })
        TextField(
            value = inputMinutesHs,
            textStyle = Typography.bodyLarge,
            modifier = Modifier
                .width(70.dp)
                .constrainAs(HsMinutes) {
                    end.linkTo(minutesGuide)
                    centerVerticallyTo(HsLabelField)
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (minutesValid(it)) {
                    inputMinutesHs = it
                    val degrees = if (inputDegreesHs.isEmpty()) 0 else inputDegreesHs.toInt()
                    val minutes = if (it.isEmpty()) 0.0 else it.toDouble()
                    onHsValueChanged(CelNavUtils.angle(degrees, minutes))
                }
            })
        Text(text = "'", style = Typography.titleLarge, modifier = Modifier.constrainAs(HsMinutesMin){
            start.linkTo(HsMinutes.end, 2.dp)
            centerVerticallyTo(HsLabelField)
        })

        // Index Correction
        var initIC by remember { mutableStateOf(true) }
        var inputICMinutes by remember { mutableStateOf("") }
        var icDirection by remember { mutableStateOf(CelNavUtils.ICDirection.Off) }

        if (initIC) {
            var initialIc = uiState.ic
            if (initialIc < 0.0) {
                icDirection = CelNavUtils.ICDirection.On
                initialIc *= -1.0
            }
            inputICMinutes = initialIc.toString()
            initIC = false
        }

        Text("IC:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(ICLabelField){
            top.linkTo(HsLabelField.bottom, margin = inputRowSpacer)
            start.linkTo(labelGuide)
        })
        TextField(
            value = inputICMinutes,
            textStyle = Typography.bodyLarge,
            modifier = Modifier
                .width(70.dp)
                .constrainAs(ICMinutes) {
                    end.linkTo(minutesGuide)
                    centerVerticallyTo(ICLabelField)
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (minutesValid(it)) {
                    inputICMinutes = it
                    onICValueChanged(computeIcMinutes(it, icDirection))
                }
            })
        Text(text = "'", style = Typography.titleLarge, modifier = Modifier.constrainAs(ICMinutesMin){
            start.linkTo(ICMinutes.end, 2.dp)
            centerVerticallyTo(ICLabelField)
        })
        Button(onClick = {
            icDirection = if (icDirection == CelNavUtils.ICDirection.On) CelNavUtils.ICDirection.Off else CelNavUtils.ICDirection.On
            onICValueChanged(computeIcMinutes(inputICMinutes, icDirection))
        },
            modifier = Modifier
                .width(70.dp)
                .constrainAs(ICDirectionButton) {
                    start.linkTo(ICMinutesMin.end, 16.dp)
                    centerVerticallyTo(ICLabelField)
                })
        {
            Text(if (icDirection == CelNavUtils.ICDirection.On) "On" else "Off")
        }

        // DIP
        var initEyeHeight by remember { mutableStateOf(true) }
        var inputEyeHeight by remember { mutableStateOf("") }

        if (initEyeHeight) {
            inputEyeHeight = uiState.eyeHeight.toString()
            initEyeHeight = false
        }

        Text("EH:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(eyeHeightLabelField){
            top.linkTo(ICLabelField.bottom, margin = inputRowSpacer)
            start.linkTo(labelGuide)
        })
        TextField(value = inputEyeHeight,
            textStyle = Typography.bodyLarge,
            modifier = Modifier
                .width(70.dp)
                .constrainAs(eyeHeightField) {
                    end.linkTo(degreesGuide)
                    centerVerticallyTo(eyeHeightLabelField)
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                if (intValid(it)) {
                    inputEyeHeight = it
                    val eyeHeight = if (it.isEmpty()) 0 else it.toInt()
                    onEyeHeightValueChanged(eyeHeight)
                }
            })
        Text("DIP:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(DIPLabelField){
            start.linkTo(eyeHeightField.end, 16.dp)
            centerVerticallyTo(eyeHeightLabelField)
        })
        Text("${uiState.dip} '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(DIPField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(eyeHeightLabelField)
        })

        // Limb
        var limbSelected by remember { mutableStateOf(CelNavUtils.Limb.Center) }
        var limbExpanded by remember { mutableStateOf(false) }

        val listItems = listOf(CelNavUtils.Limb.Upper, CelNavUtils.Limb.Lower, CelNavUtils.Limb.Center)
        Text("Limb:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(limbLabelField){
            top.linkTo(eyeHeightLabelField.bottom, margin = inputRowSpacer)
            start.linkTo(labelGuide)
        })
        Box(modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
            .constrainAs(limbDropDownField) {
                start.linkTo(limbLabelField.end, 16.dp)
                centerVerticallyTo(limbLabelField)
            }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = limbToDisplayName(limbSelected), style = Typography.titleLarge)
                IconButton(onClick = { limbExpanded = !limbExpanded }) {
                    Icon(Icons.Filled.ArrowDropDown, stringResource(id = R.string.app_name))
                }
            }

            DropdownMenu(
                expanded = limbExpanded,
                onDismissRequest = { limbExpanded = false },
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                listItems.forEach { itemValue ->
                    DropdownMenuItem(text = { Text(text = limbToDisplayName(itemValue), style = Typography.bodyLarge) },
                        onClick = {
                            limbSelected = itemValue
                            onLimbValueChanged(itemValue)
                            limbExpanded = false
                        })
                    Divider()
                }
            }
        }

        // Refraction
        Text("Refr:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(refrLabelField){
            top.linkTo(limbLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("${uiState.refraction} '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(refrField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(refrLabelField)
        })

        // SD
        Text("SD:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(SDLabelField){
            top.linkTo(refrLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("${uiState.SD} '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(SDField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(SDLabelField)
        })

        // HP
        Text("HP:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(HPLabelField){
            top.linkTo(SDLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("${uiState.HP} '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(HPField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(HPLabelField)
        })

        // Ho
        val (HoDegrees, HoMinutes, _) = CelNavUtils.degreesMinutesSign(uiState.Ho)
        Text("Ho:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(HoLabelField){
            top.linkTo(HPLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$HoDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(HoDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(HoLabelField)
        })
        Text("$HoMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(HoMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(HoLabelField)
        })

        Divider(thickness = 2.dp, modifier = Modifier.constrainAs(divider2){
            top.linkTo(HoLabelField.bottom, margin = rowSpacer)
        })

        // Lat
        val (latDegrees, latMinutes, latSign) = CelNavUtils.degreesMinutesSign(uiState.lat)
        Text("Lat:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(latLabelField){
            top.linkTo(divider2.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$latDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(latDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(latLabelField)
        })
        Text("$latMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(latMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(latLabelField)
        })
        Text(if (latSign == 1) "N" else "S", style = Typography.bodyLarge, modifier = Modifier.constrainAs(latSignField){
            start.linkTo(latMinutesField.end, 8.dp)
            centerVerticallyTo(latLabelField)
        })

        // Lon
        val (lonDegrees, lonMinutes, lonSign) = CelNavUtils.degreesMinutesSign(uiState.lon)
        Text("Lat:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(lonLabelField){
            top.linkTo(latLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$lonDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(lonDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(lonLabelField)
        })
        Text("$lonMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(lonMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(lonLabelField)
        })
        Text(if (lonSign == 1) "E" else "W", style = Typography.bodyLarge, modifier = Modifier.constrainAs(lonSignField){
            start.linkTo(latMinutesField.end, 8.dp)
            centerVerticallyTo(lonLabelField)
        })

        Divider(thickness = 2.dp, modifier = Modifier.constrainAs(divider3){
            top.linkTo(lonLabelField.bottom, margin = rowSpacer)
        })

        // Hc
        val (HcDegrees, HcMinutes, _) = CelNavUtils.degreesMinutesSign(uiState.Hc)
        Text("Hc:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(HcLabelField){
            top.linkTo(divider3.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$HcDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(HcDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(HcLabelField)
        })
        Text("$HcMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(HcMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(HcLabelField)
        })

        // Zn
        val (ZnDegrees, ZnMinutes, _) = CelNavUtils.degreesMinutesSign(uiState.Zn)
        Text("Zn:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(ZnLabelField){
            top.linkTo(HcLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("$ZnDegrees °", style = Typography.bodyLarge, modifier = Modifier.constrainAs(ZnDegreesField){
            end.linkTo(degreesGuide)
            centerVerticallyTo(ZnLabelField)
        })
        Text("$ZnMinutes '", style = Typography.bodyLarge, modifier = Modifier.constrainAs(ZnMinutesField){
            end.linkTo(minutesGuide)
            centerVerticallyTo(ZnLabelField)
        })

        // Intercept
        Text("a:", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(interceptLabelField){
            top.linkTo(ZnLabelField.bottom, margin = rowSpacer)
            start.linkTo(labelGuide)
        })
        Text("${uiState.intercept} nm; ${if (uiState.lopDirection == CelNavUtils.LOPDirection.Away) "Away" else "Towards"}"
            , style = Typography.bodyLarge, modifier = Modifier.constrainAs(interceptField){
            start.linkTo(interceptLabelField.end, 60.dp)
            centerVerticallyTo(interceptLabelField)
        })
    }
}

