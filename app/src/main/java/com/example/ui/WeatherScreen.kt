package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedCities by viewModel.savedCities.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    var activeTab by remember { mutableStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        showSplash = false
    }

    if (showSplash) {
        val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowScale"
        )
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SunRotation"
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large solar crown with glowing effect
                Box(
                    modifier = Modifier
                        .size(140.dp * glowScale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(SolarGold.copy(alpha = 0.25f), Color.Transparent))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SolarGold, Color(0xFF161616)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "LJS Weather Monitor Logo",
                            tint = SolarGold,
                            modifier = Modifier.size(42.dp).rotate(rotationAngle)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "LJS Weather Monitoring",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarGold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SURVEILLANCE & METEOROLOGICAL FORECASTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedCloud,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Application Branding header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SolarGold, Color(0xFF161616)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "Weather Logo",
                            tint = SolarGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LJS Weather Monitoring",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarGold
                    )
                }

                // Autocomplete Search Bar
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_search_input"),
                        placeholder = { Text("Search city (e.g. Berlin, Paris)", color = MutedCloud) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MutedCloud
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear",
                                        tint = MutedCloud
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = FrostBlue,
                            unfocusedBorderColor = Slate700
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Autocomplete Suggestions Panel
                AnimatedVisibility(
                    visible = searchResults.isNotEmpty() || isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            if (isSearching) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = FrostBlue,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Finding coordinates...", color = MutedCloud, fontSize = 14.sp)
                                }
                            } else {
                                searchResults.forEach { result ->
                                    val subtitle = listOfNotNull(result.admin1, result.country).joinToString(", ")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectLocation(
                                                    name = result.name,
                                                    latitude = result.latitude,
                                                    longitude = result.longitude,
                                                    country = result.country,
                                                    admin1 = result.admin1
                                                )
                                                viewModel.clearSearch()
                                                focusManager.clearFocus()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("search_result_item"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Place,
                                            contentDescription = "Place",
                                            tint = FrostBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = result.name,
                                                color = PureWhite,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (subtitle.isNotEmpty()) {
                                                Text(
                                                    text = subtitle,
                                                    color = MutedCloud,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Slate700, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant premium gold/silver segmented tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .border(width = 1.dp, color = GeoBorder, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple("Forecast", Icons.Outlined.Cloud, 0),
                    Triple("Atmosphere Map", Icons.Outlined.Map, 1),
                    Triple("Global Clocks", Icons.Outlined.Schedule, 2)
                ).forEach { (label, icon, tabIdx) ->
                    val isSelected = activeTab == tabIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SolarGold else GeoBorder.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = tabIdx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) SolarGold else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SolarGold else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (activeTab) {
                0 -> {
                    // FORECAST DASHBOARD
                    when (val state = uiState) {
                        is WeatherUiResult.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = SolarGold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Gathering regional atmosphere details...", color = MutedCloud, fontSize = 14.sp)
                                }
                            }
                        }
                        is WeatherUiResult.Success -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Current Details Card
                                CurrentWeatherView(state = state, onToggleFavorite = {
                                    viewModel.toggleFavorite(
                                        state.cityName,
                                        state.latitude,
                                        state.longitude,
                                        state.country,
                                        state.admin1
                                    )
                                })

                                Spacer(modifier = Modifier.height(16.dp))

                                // AI Meteorologist Insights Card
                                AiInsightsView(state = state)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Hourly Scroll Section
                                HourlyForecastView(state = state)

                                Spacer(modifier = Modifier.height(16.dp))

                                // 7-Day Trend Section
                                DailyTrendView(state = state)

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        is WeatherUiResult.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Unable to load forecast",
                                        color = PureWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.message,
                                        color = MutedCloud,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            viewModel.selectLocation(
                                                "London", 51.5074, -0.1278, "United Kingdom", "England"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolarGold)
                                    ) {
                                        Icon(Icons.Outlined.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reset to default city" , color = Slate900)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // INTERACTIVE ATMOSPHERE MAP
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        AtmosphereMapView(viewModel = viewModel, onSelectBaseTab = { activeTab = 0 })
                    }
                }
                2 -> {
                    // GLOBAL COUNTRY TIME ZONING
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        GlobalTimeZonesView(viewModel = viewModel, onSelectBaseTab = { activeTab = 0 })
                    }
                }
            }

            // Quick Access Saved Cities row (Bottom anchor)
            if (activeTab == 0 && savedCities.isNotEmpty()) {
                Surface(
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = GeoBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Saved Locations",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(savedCities) { city ->
                                val activeCity = (uiState as? WeatherUiResult.Success)?.cityName
                                val isActive = activeCity.equals(city.name, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black)
                                        .border(
                                            width = 1.dp,
                                            color = if (isActive) SolarGold else GeoBorder.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.selectLocation(
                                                city.name,
                                                city.latitude,
                                                city.longitude,
                                                city.country,
                                                city.admin1
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("favorite_city_chip")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Place,
                                            contentDescription = null,
                                            tint = if (isActive) SolarGold else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = city.name,
                                            color = if (isActive) SolarGold else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun CurrentWeatherView(
    state: WeatherUiResult.Success,
    onToggleFavorite: () -> Unit
) {
    val current = state.forecast.current
    val temp = current?.temperature2m ?: 0.0
    val weatherCode = current?.weatherCode ?: 0
    val desc = parseWeatherCode(weatherCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("current_weather_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GeoHeroBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Header with bookmark favorite star
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = "Location",
                        tint = GeoHeroText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = state.cityName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoHeroText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val regionDetails = listOfNotNull(state.admin1, state.country).joinToString(", ")
                        if (regionDetails.isNotEmpty()) {
                            Text(
                                text = regionDetails,
                                fontSize = 12.sp,
                                color = GeoHeroText.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("favorite_toggle_button")
                ) {
                    Icon(
                        imageVector = if (state.isSaved) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (state.isSaved) "Remove Favorite" else "Save Favorite",
                        tint = if (state.isSaved) GeoAccentPurple else GeoHeroText.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main large condition display row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Glow ambient weather graphics symbol
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(GeoSelectedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = desc.icon,
                        contentDescription = desc.description,
                        tint = GeoAccentPurple,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = "${temp.toInt()}°C",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Light,
                        color = GeoHeroText,
                        lineHeight = 54.sp,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = desc.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoHeroText.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Outlined.Thermostat,
                    label = "Feels Like",
                    value = "${current?.apparentTemperature?.toInt() ?: temp.toInt()}°C",
                    tintColor = GeoHeroText,
                    textColor = GeoHeroText,
                    labelColor = GeoHeroText.copy(alpha = 0.7f)
                )
                MetricItem(
                    icon = Icons.Outlined.WaterDrop,
                    label = "Humidity",
                    value = "${current?.relativeHumidity2m?.toInt() ?: 50}%",
                    tintColor = GeoHeroText,
                    textColor = GeoHeroText,
                    labelColor = GeoHeroText.copy(alpha = 0.7f)
                )
                MetricItem(
                    icon = Icons.Outlined.Air,
                    label = "Wind Speed",
                    value = "${current?.windSpeed10m ?: 10.0} km/h",
                    tintColor = GeoHeroText,
                    textColor = GeoHeroText,
                    labelColor = GeoHeroText.copy(alpha = 0.7f)
                )
                MetricItem(
                    icon = Icons.Outlined.WbSunny,
                    label = "UV Index",
                    value = "${state.forecast.daily?.uvIndexMax?.firstOrNull() ?: 2.0}",
                    tintColor = GeoHeroText,
                    textColor = GeoHeroText,
                    labelColor = GeoHeroText.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    tintColor: Color = FrostBlue,
    textColor: Color = PureWhite,
    labelColor: Color = MutedCloud
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = labelColor)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun AiInsightsView(state: WeatherUiResult.Success) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_insights_card")
            .border(width = 1.dp, color = GeoBorder, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GeoCardBg)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GeoCardBg,
                            GeoSelectedBg
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Sparkles",
                        tint = GeoAccentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Meteorologist Insights",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeoAccentPurple)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "GEMINI 3.5",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (state.isAiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = GeoAccentPurple,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Drafting custom recommendations...",
                            color = GeoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = state.aiInsights ?: "",
                        fontSize = 13.sp,
                        color = GeoTextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}@Composable
fun HourlyForecastView(state: WeatherUiResult.Success) {
    val hourly = state.forecast.hourly ?: return
    val currentIsoTime = state.forecast.current?.time ?: ""

    // We filter the next 24 elements from current point
    val startIndex = hourly.time.indexOfFirst { it >= currentIsoTime }.coerceAtLeast(0)
    val next24Times = hourly.time.drop(startIndex).take(24)
    val next24Temps = hourly.temperature2m.drop(startIndex).take(24)
    val next24Codes = hourly.weatherCode?.drop(startIndex)?.take(24) ?: emptyList()
    val next24Pr = hourly.precipitationProbability?.drop(startIndex)?.take(24) ?: emptyList()

    var selectedHourIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HOURLY FORECAST",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SolarGold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(next24Times.indices.toList()) { index ->
                val isActive = index == selectedHourIndex
                val isoTime = next24Times.getOrNull(index) ?: ""
                val rawTime = isoTime.substringAfter("T", "00:00")
                val cleanTime = if (index == 0) "Now" else {
                    if (rawTime.length >= 5) rawTime.substring(0, 5) else rawTime
                }
                val tempVal = next24Temps.getOrNull(index) ?: 0.0
                val code = next24Codes.getOrNull(index) ?: 0
                val prChance = next24Pr.getOrNull(index) ?: 0
                val hourlyDesc = parseWeatherCode(code)

                Card(
                    modifier = Modifier
                        .width(76.dp)
                        .clickable { selectedHourIndex = index }
                        .border(
                            width = 1.dp,
                            color = if (isActive) SolarGold else GeoBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("hourly_forecast_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cleanTime,
                            fontSize = 12.sp,
                            color = if (isActive) SolarGold else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = hourlyDesc.icon,
                            contentDescription = hourlyDesc.description,
                            tint = if (isActive) SolarGold else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${tempVal.toInt()}°",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) SolarGold else Color.White
                        )
                        if (prChance > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💧$prChance%",
                                    fontSize = 9.sp,
                                    color = if (isActive) SolarGold else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyTrendView(state: WeatherUiResult.Success) {
    val daily = state.forecast.daily ?: return
    val times = daily.time
    val maxTemps = daily.temperature2mMax
    val minTemps = daily.temperature2mMin
    val codes = daily.weatherCode

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "7-DAY GENERAL TREND",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GeoTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GeoCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = GeoBorder, shape = RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                times.indices.forEach { index ->
                    val rawDate = times[index]
                    val dayName = parseDayName(rawDate)
                    val maxT = maxTemps.getOrNull(index) ?: 0.0
                    val minT = minTemps.getOrNull(index) ?: 0.0
                    val code = codes.getOrNull(index) ?: 0
                    val desc = parseWeatherCode(code)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoTextPrimary,
                            modifier = Modifier.weight(1.2f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.5f),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = desc.icon,
                                contentDescription = desc.description,
                                tint = GeoAccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = desc.description,
                                fontSize = 12.sp,
                                color = GeoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text(
                                text = "${maxT.toInt()}°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${minT.toInt()}°",
                                fontSize = 14.sp,
                                color = GeoTextSecondary
                            )
                        }
                    }

                    if (index < times.size - 1) {
                        HorizontalDivider(color = GeoBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// Custom parsed status representation holding icon details
data class WeatherConditionDesc(
    val description: String,
    val icon: ImageVector,
    val iconColor: Color
)

fun parseWeatherCode(code: Int): WeatherConditionDesc {
    return when (code) {
        0 -> WeatherConditionDesc("Clear", Icons.Outlined.WbSunny, SolarGold)
        1 -> WeatherConditionDesc("Mostly Clear", Icons.Outlined.WbSunny, SolarGold)
        2 -> WeatherConditionDesc("Partly Cloudy", Icons.Outlined.CloudQueue, MutedCloud)
        3 -> WeatherConditionDesc("Overcast", Icons.Outlined.CloudQueue, MutedCloud)
        45, 48 -> WeatherConditionDesc("Foggy", Icons.Outlined.Air, MutedCloud)
        51, 53, 55 -> WeatherConditionDesc("Light Drizzle", Icons.Outlined.WaterDrop, FrostBlue)
        61, 63, 65, 80, 81, 82 -> WeatherConditionDesc("Rainy", Icons.Outlined.Umbrella, FrostBlue)
        71, 73, 75, 77, 85, 86 -> WeatherConditionDesc("Snowy", Icons.Outlined.AcUnit, FrostBlue)
        95, 96, 99 -> WeatherConditionDesc("Storms", Icons.Outlined.Bolt, SolarGold)
        else -> WeatherConditionDesc("Hazy", Icons.Outlined.CloudQueue, MutedCloud)
    }
}

fun parseDayName(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val cal = GregorianCalendar(year, month - 1, day)
            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val today = Calendar.getInstance()
            if (today.get(Calendar.YEAR) == year && today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                "Today"
            } else {
                "${days[dayOfWeek - 1]}, ${months[month - 1]} $day"
            }
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

// ==========================================
// --- Custom Atmosphere Map & Timezone Views ---
// ==========================================

data class MapLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,  // Heat level
    val precipitation: Int,   // Rain level %
    val aqi: Int,             // Air quality Index
    val timezone: String,     // Time zone name
    val timezoneOffset: Double // GMT Offset in hours
)

val MapLocations = listOf(
    MapLocation("New York", "USA", 40.7128, -74.0060, 24.5, 15, 42, "GMT-4", -4.0),
    MapLocation("London", "UK", 51.5074, -0.1278, 17.0, 65, 31, "GMT+1", 1.0),
    MapLocation("Tokyo", "Japan", 35.6762, 139.6503, 21.0, 10, 55, "GMT+9", 9.0),
    MapLocation("Sydney", "Australia", -33.8688, 151.2093, 16.5, 5, 22, "GMT+10", 10.0),
    MapLocation("Cairo", "Egypt", 30.0444, 31.2357, 34.0, 0, 85, "GMT+3", 3.0),
    MapLocation("Mumbai", "India", 19.0760, 72.8777, 31.0, 80, 110, "GMT+5.5", 5.5),
    MapLocation("Moscow", "Russia", 55.7558, 37.6173, 14.8, 25, 48, "GMT+3", 3.0),
    MapLocation("Johannesburg", "South Africa", -26.2041, 28.0473, 19.0, 0, 35, "GMT+2", 2.0),
    MapLocation("São Paulo", "Brazil", -23.5505, -46.6333, 25.0, 40, 50, "GMT-3", -3.0),
    MapLocation("Reykjavik", "Iceland", 64.1466, -21.9426, 8.2, 75, 12, "GMT+0", 0.0),
    MapLocation("Bangkok", "Thailand", 13.7563, 100.5018, 33.0, 55, 95, "GMT+7", 7.0),
    MapLocation("Anchorage", "USA", 61.2181, -149.9003, 6.0, 30, 18, "GMT-8", -8.0)
)

data class CountryTimeZone(
    val country: String,
    val city: String,
    val flagEmoji: String,
    val gmtLabel: String,
    val offsetHours: Double
)

val CountryTimeZones = listOf(
    CountryTimeZone("United States", "Washington D.C.", "🇺🇸", "GMT-4.0", -4.0),
    CountryTimeZone("United Kingdom", "London", "🇬🇧", "GMT+1.0", 1.0),
    CountryTimeZone("Germany", "Berlin", "🇩🇪", "GMT+2.0", 2.0),
    CountryTimeZone("Japan", "Tokyo", "🇯🇵", "GMT+9.0", 9.0),
    CountryTimeZone("India", "New Delhi", "🇮🇳", "GMT+5.5", 5.5),
    CountryTimeZone("Australia", "Sydney", "🇦🇺", "GMT+10.0", 10.0),
    CountryTimeZone("Brazil", "Brasília", "🇧🇷", "GMT-3.0", -3.0),
    CountryTimeZone("South Africa", "Pretoria", "🇿🇦", "GMT+2.0", 2.0),
    CountryTimeZone("Canada", "Ottawa", "🇨🇦", "GMT-4.0", -4.0),
    CountryTimeZone("Egypt", "Cairo", "🇪🇬", "GMT+3.0", 3.0),
    CountryTimeZone("France", "Paris", "🇫🇷", "GMT+2.0", 2.0),
    CountryTimeZone("China", "Beijing", "🇨🇳", "GMT+8.0", 8.0),
    CountryTimeZone("Russia", "Moscow", "🇷🇺", "GMT+3.0", 3.0),
    CountryTimeZone("Saudi Arabia", "Riyadh", "🇸🇦", "GMT+3.0", 3.0),
    CountryTimeZone("Singapore", "Singapore", "🇸🇬", "GMT+8.0", 8.0),
    CountryTimeZone("Thailand", "Bangkok", "🇹🇭", "GMT+7.0", 7.0),
    CountryTimeZone("Turkey", "Istanbul", "🇹🇷", "GMT+3.0", 3.0),
    CountryTimeZone("United Arab Emirates", "Dubai", "🇦🇪", "GMT+4.0", 4.0),
    CountryTimeZone("Vietnam", "Hanoi", "🇻🇳", "GMT+7.0", 7.0),
    CountryTimeZone("Philippines", "Manila", "🇵🇭", "GMT+8.0", 8.0),
    CountryTimeZone("Spain", "Madrid", "🇪🇸", "GMT+2.0", 2.0),
    CountryTimeZone("Italy", "Rome", "🇮🇹", "GMT+2.0", 2.0),
    CountryTimeZone("Greece", "Athens", "🇬🇷", "GMT+3.0", 3.0),
    CountryTimeZone("Sweden", "Stockholm", "🇸🇪", "GMT+2.0", 2.0),
    CountryTimeZone("Switzerland", "Zurich", "🇨🇭", "GMT+2.0", 2.0),
    CountryTimeZone("Poland", "Warsaw", "🇵🇱", "GMT+2.0", 2.0),
    CountryTimeZone("Netherlands", "Amsterdam", "🇳🇱", "GMT+2.0", 2.0),
    CountryTimeZone("Belgium", "Brussels", "🇧🇪", "GMT+2.0", 2.0),
    CountryTimeZone("Denmark", "Copenhagen", "🇩🇰", "GMT+2.0", 2.0),
    CountryTimeZone("Norway", "Oslo", "🇳🇴", "GMT+2.0", 2.0),
    CountryTimeZone("Finland", "Helsinki", "🇫🇮", "GMT+3.0", 3.0),
    CountryTimeZone("Ireland", "Dublin", "🇮🇪", "GMT+1.0", 1.0),
    CountryTimeZone("Iceland", "Reykjavik", "🇮🇸", "GMT+0.0", 0.0),
    CountryTimeZone("Mexico", "Mexico City", "🇲🇽", "GMT-6.0", -6.0),
    CountryTimeZone("Colombia", "Bogota", "🇨🇴", "GMT-5.0", -5.0),
    CountryTimeZone("Peru", "Lima", "🇵🇪", "GMT-5.0", -5.0),
    CountryTimeZone("Chile", "Santiago", "🇨🇱", "GMT-3.0", -3.0),
    CountryTimeZone("Argentina", "Buenos Aires", "🇦🇷", "GMT-3.0", -3.0),
    CountryTimeZone("Indonesia", "Jakarta", "🇮🇩", "GMT+7.0", 7.0),
    CountryTimeZone("Malaysia", "Kuala Lumpur", "🇲🇾", "GMT+8.0", 8.0),
    CountryTimeZone("Pakistan", "Islamabad", "🇵🇰", "GMT+5.0", 5.0),
    CountryTimeZone("Bangladesh", "Dhaka", "🇧🇩", "GMT+6.0", 6.0),
    CountryTimeZone("Iraq", "Baghdad", "🇮🇶", "GMT+3.0", 3.0),
    CountryTimeZone("Iran", "Tehran", "🇮🇷", "GMT+3.5", 3.5),
    CountryTimeZone("Israel", "Jerusalem", "🇮🇱", "GMT+3.0", 3.0),
    CountryTimeZone("Afghanistan", "Kabul", "🇦🇫", "GMT+4.5", 4.5),
    CountryTimeZone("Kenya", "Nairobi", "🇰🇪", "GMT+3.0", 3.0),
    CountryTimeZone("Nigeria", "Abuja", "🇳🇬", "GMT+1.0", 1.0),
    CountryTimeZone("Ethiopia", "Addis Ababa", "🇪🇹", "GMT+3.0", 3.0),
    CountryTimeZone("Ukraine", "Kyiv", "🇺🇦", "GMT+3.0", 3.0),
    CountryTimeZone("Portugal", "Lisbon", "🇵🇹", "GMT+1.0", 1.0),
    CountryTimeZone("New Zealand", "Wellington", "🇳🇿", "GMT+12.0", 12.0)
)

fun getFormattedLocalTime(offsetHours: Double): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val offsetMillis = (offsetHours * 60 * 60 * 1000).toLong()
    val localMillis = calendar.timeInMillis + offsetMillis
    calendar.timeInMillis = localMillis
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else hour
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}

@Composable
fun AtmosphereMapView(
    viewModel: WeatherViewModel,
    onSelectBaseTab: () -> Unit
) {
    var activeLayer by remember { mutableStateOf(0) } // 0: Heat, 1: Rain, 2: Air Quality, 3: Timezone
    var selectedLocation by remember { mutableStateOf(MapLocations[1]) } // Default to London

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Overlay switcher title
        Text(
            text = "Atmospheric Surveillance Radar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SolarGold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Select layers below to render interactive heatmaps, moisture levels, particulate index, and active timezone boundaries.",
            fontSize = 12.sp,
            color = MutedCloud,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Segment Layer selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(12.dp))
                .border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                Pair("Heat Radar", 0),
                Pair("Rain Radar", 1),
                Pair("AQI Air", 2),
                Pair("Timezones", 3)
            ).forEach { (name, index) ->
                val isSel = activeLayer == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(
                            width = 1.dp,
                            color = if (isSel) SolarGold else GeoBorder.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeLayer = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) SolarGold else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fully Responsive Coordinate Projection Map Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF070707))
                .border(1.dp, GeoBorder, RoundedCornerShape(16.dp))
        ) {
            val mapWidth = maxWidth
            val mapHeight = maxHeight

            // Draw coordinate grids desugared
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Equator reference
                drawLine(
                    color = SolarGold.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(0f, h / 2f),
                    end = androidx.compose.ui.geometry.Offset(w, h / 2f),
                    strokeWidth = 1.dp.toPx()
                )
                // Prime Meridian reference
                drawLine(
                    color = FrostBlue.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(w / 2f, 0f),
                    end = androidx.compose.ui.geometry.Offset(w / 2f, h),
                    strokeWidth = 1.dp.toPx()
                )

                // Subdivision latitude and longitude markers
                for (i in 1..5) {
                    val x = (w / 6f) * i.toFloat()
                    drawLine(
                        color = MutedCloud.copy(alpha = 0.08f),
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, h),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
                for (i in 1..3) {
                    val y = (h / 4f) * i.toFloat()
                    drawLine(
                        color = MutedCloud.copy(alpha = 0.08f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                // Render dynamic overlay halos on top of map locations
                MapLocations.forEach { loc ->
                    val lx = (((loc.longitude + 180.0) / 360.0).toFloat()) * w
                    val ly = (((90.0 - loc.latitude) / 180.0).toFloat()) * h

                    when (activeLayer) {
                        0 -> { // Temperature
                            val normTemp = ((loc.temperature.toFloat() + 5f) / 45f).coerceIn(0.1f, 1f)
                            drawCircle(
                                color = SolarGold.copy(alpha = 0.22f * normTemp),
                                radius = (12.dp + (loc.temperature.toFloat().dp * 0.4f)).toPx(),
                                center = androidx.compose.ui.geometry.Offset(lx, ly)
                            )
                        }
                        1 -> { // Rain moisture density
                            if (loc.precipitation > 5) {
                                drawCircle(
                                    color = FrostBlue.copy(alpha = 0.28f),
                                    radius = (10.dp + (loc.precipitation.toFloat().dp * 0.15f)).toPx(),
                                    center = androidx.compose.ui.geometry.Offset(lx, ly),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f.dp.toPx())
                                )
                            }
                        }
                        2 -> { // AQI dispersion density
                            val opac = (loc.aqi.toFloat() / 150f).coerceIn(0.15f, 0.7f)
                            drawCircle(
                                color = if (loc.aqi > 75) SolarGold.copy(alpha = opac) else FrostBlue.copy(alpha = opac * 0.5f),
                                radius = (12.dp + (loc.aqi.toFloat().dp * 0.12f)).toPx(),
                                center = androidx.compose.ui.geometry.Offset(lx, ly)
                            )
                        }
                        3 -> { // Time Zone Vertical lines
                            drawLine(
                                color = SolarGold.copy(alpha = 0.12f),
                                start = androidx.compose.ui.geometry.Offset(lx, 0f),
                                end = androidx.compose.ui.geometry.Offset(lx, h),
                                strokeWidth = 0.8f.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Clickable tactile node points showing weather and name of every place
            MapLocations.forEach { loc ->
                val lxPercent = ((loc.longitude + 180.0) / 360.0).toFloat()
                val lyPercent = ((90.0 - loc.latitude) / 180.0).toFloat()
                val xDp = (mapWidth.value * lxPercent).dp
                val yDp = (mapHeight.value * lyPercent).dp

                val isSelected = selectedLocation.name == loc.name

                Box(
                    modifier = Modifier
                        .offset(x = xDp - 38.dp, y = yDp - 20.dp)
                        .width(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Color(0xFF1E1602).copy(alpha = 0.95f)
                            else Color.Black.copy(alpha = 0.85f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) SolarGold else GeoBorder.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedLocation = loc }
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = loc.name,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SolarGold else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${loc.temperature.toInt()}°C",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SolarGold else Color.White
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (loc.precipitation >= 50) "🌧️" 
                                       else if (loc.precipitation > 15) "🌦️"
                                       else if (loc.temperature >= 28) "☀️" 
                                       else "☁️",
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Highlight Active Region Analytics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GeoCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GeoBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Place, contentDescription = "", tint = SolarGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedLocation.name}, ${selectedLocation.country}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolarGold
                            )
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "Coords: %.3f°N, %.3f°E", selectedLocation.latitude, selectedLocation.longitude),
                            fontSize = 11.sp,
                            color = MutedCloud
                        )
                    }

                    // Dynamic Clock label
                    Box(
                        modifier = Modifier
                            .background(GeoSelectedBg, RoundedCornerShape(8.dp))
                            .border(1.dp, SolarGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getFormattedLocalTime(selectedLocation.timezoneOffset),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarGold
                        )
                    }
                }

                HorizontalDivider(color = GeoBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                // Plotted layers grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Temperature / Heat level
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.WbSunny, contentDescription = "", tint = SolarGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Heat Level", fontSize = 11.sp, color = MutedCloud)
                        Text("${selectedLocation.temperature}°C", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        val rating = if (selectedLocation.temperature >= 30) "High Thermal" else if (selectedLocation.temperature < 12) "Arctic Chill" else "Mild Zone"
                        Text(rating, fontSize = 9.sp, color = SolarGold)
                    }

                    // Precipitation / Moisture
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.WaterDrop, contentDescription = "", tint = FrostBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rain Moisture", fontSize = 11.sp, color = MutedCloud)
                        Text("${selectedLocation.precipitation}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        val rating = if (selectedLocation.precipitation >= 50) "Heavy Downpour" else if (selectedLocation.precipitation > 10) "Fog/Moist" else "Dry Surface"
                        Text(rating, fontSize = 9.sp, color = FrostBlue)
                    }

                    // Air quality
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Air, contentDescription = "", tint = SolarGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Air Quality", fontSize = 11.sp, color = MutedCloud)
                        Text("${selectedLocation.aqi} AQI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        val rating = if (selectedLocation.aqi > 90) "Heavy Haze" else "Impeccable"
                        Text(rating, fontSize = 9.sp, color = if (selectedLocation.aqi > 90) ErrorRed else SolarGold)
                    }

                    // Time timezone offset
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Schedule, contentDescription = "", tint = MutedCloud, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Surveillance", fontSize = 11.sp, color = MutedCloud)
                        Text(selectedLocation.timezone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        Text("Atomic Sync", fontSize = 9.sp, color = MutedCloud)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Click to focus home weather logic
                Button(
                    onClick = {
                        viewModel.selectLocation(
                            selectedLocation.name,
                            selectedLocation.latitude,
                            selectedLocation.longitude,
                            selectedLocation.country,
                            null
                        )
                        onSelectBaseTab()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = RealDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Selected Spot to Forecast Console", color = RealDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun GlobalTimeZonesView(
    viewModel: WeatherViewModel,
    onSelectBaseTab: () -> Unit
) {
    var timezoneFilter by remember { mutableStateOf("") }

    val filteredCountries = CountryTimeZones.filter {
        it.country.contains(timezoneFilter, ignoreCase = true) ||
        it.city.contains(timezoneFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Atomic World Clock Matrix",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SolarGold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Browse real-time timezone calendars and offsets. Click any country below to automatically target and load its full forecast metrics.",
            fontSize = 12.sp,
            color = MutedCloud,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filtration search input field
        OutlinedTextField(
            value = timezoneFilter,
            onValueChange = { timezoneFilter = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            placeholder = { Text("Filter nations, cities, offsets...", color = MutedCloud) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.TravelExplore,
                    contentDescription = null,
                    tint = SolarGold
                )
            },
            trailingIcon = {
                if (timezoneFilter.isNotEmpty()) {
                    IconButton(onClick = { timezoneFilter = "" }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = null, tint = MutedCloud)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedContainerColor = Color(0xFF0D0D0D),
                unfocusedContainerColor = Color(0xFF0D0D0D),
                focusedBorderColor = SolarGold,
                unfocusedBorderColor = GeoBorder
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Scrollable database table of all country locations
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF030303))
                .border(1.dp, GeoBorder, RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (filteredCountries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded offsets matches your filter",
                            color = MutedCloud,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredCountries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Lookup coordinates to load
                                val matched = MapLocations.find { it.name.equals(entry.city, ignoreCase = true) }
                                val lat = matched?.latitude ?: 51.5
                                val lon = matched?.longitude ?: -0.1
                                viewModel.selectLocation(
                                    entry.city,
                                    lat,
                                    lon,
                                    entry.country,
                                    null
                                )
                                onSelectBaseTab()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1.5f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Native unicode flag
                                Text(
                                    text = entry.flagEmoji,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        text = entry.country,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = entry.city,
                                        fontSize = 12.sp,
                                        color = MutedCloud
                                    )
                                }
                            }

                            // GMT label details slot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = entry.gmtLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SolarGold
                                )
                            }

                            // Precision clock slot
                            Text(
                                text = getFormattedLocalTime(entry.offsetHours),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolarGold,
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    HorizontalDivider(color = GeoBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}

