package com.shrimpadvisor.plcycle.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.screens.*

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val steps = listOf(
        Triple(Icons.Default.VerifiedUser, "PL Gate", "Score your post-larvae batch before stocking — get a STOCK, HOLD, or REJECT verdict."),
        Triple(Icons.Default.WaterDrop, "Stocking", "Set pond size and density; the engine checks carrying capacity and all 5 water parameters."),
        Triple(Icons.Default.Analytics, "Survival", "Log daily readings to track survival trajectory against the industry reference curve."),
        Triple(Icons.Default.Payments, "FCR & Cost", "Monitor your real-time cost per kg and feed conversion ratio as the cycle progresses."),
        Triple(Icons.Default.TrendingUp, "Optimizer", "Run a 30-day hold-vs-harvest simulation to find the most profitable harvest window."),
        Triple(Icons.Default.SmartToy, "AI Advisor", "Ask any question in plain language — the AI has your full pond data and trend history loaded.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🦐",
                fontSize = 56.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "PL Cycle Advisor",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Decision support for intensive L. vannamei farming",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "HOW IT WORKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            steps.forEach { (icon, title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 17.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "For the AI Advisor to work, add your Gemini API key to the .env file and rebuild. All other modules work offline.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShrimpAppMainContainer(
    viewModel: PondCycleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pl_advisor_prefs", android.content.Context.MODE_PRIVATE) }
    var showWelcome by remember { mutableStateOf(!prefs.getBoolean("hasSeenWelcome", false)) }

    if (showWelcome) {
        WelcomeScreen(onGetStarted = {
            prefs.edit().putBoolean("hasSeenWelcome", true).apply()
            showWelcome = false
        })
        return
    }

    val cycles by viewModel.allCycles.collectAsStateWithLifecycle()
    val activeCycle by viewModel.activeCycle.collectAsStateWithLifecycle()

    val plQuality by viewModel.plQualityResult.collectAsStateWithLifecycle()
    val stocking by viewModel.stockingResult.collectAsStateWithLifecycle()
    val survival by viewModel.survivalResult.collectAsStateWithLifecycle()
    val cost by viewModel.costResult.collectAsStateWithLifecycle()
    val harvest by viewModel.harvestResult.collectAsStateWithLifecycle()

    val activeReadings by viewModel.activeReadings.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val allRegionProfiles by viewModel.allRegionProfiles.collectAsStateWithLifecycle()
    val activeRegionProfile by viewModel.activeRegionProfile.collectAsStateWithLifecycle()
    val feedRecommendation by viewModel.feedRecommendation.collectAsStateWithLifecycle()
    val survivalForecast by viewModel.survivalForecast.collectAsStateWithLifecycle()
    val diseaseRisk by viewModel.diseaseRisk.collectAsStateWithLifecycle()
    val tempAdjustedFcr by viewModel.tempAdjustedFcr.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    var showNewPondDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🦐 PL Cycle Advisor",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { activeTab = 6 },
                        modifier = Modifier.testTag("ai_advisor_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Advisor",
                            tint = if (activeTab == 6) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { activeTab = 7 },
                        modifier = Modifier.testTag("report_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Report",
                            tint = if (activeTab == 7) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        },
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                listOf(
                    Pair("Overview", Icons.Default.Dashboard),
                    Pair("PL Gate", Icons.Default.VerifiedUser),
                    Pair("Stocking", Icons.Default.WaterDrop),
                    Pair("Survival", Icons.Default.Analytics),
                    Pair("FCR & Cost", Icons.Default.Payments),
                    Pair("Optimizer", Icons.Default.TrendingUp),
                    Pair("AI Advisor", Icons.Default.SmartToy),
                    Pair("Report", Icons.Default.Summarize),
                    Pair("History", Icons.Default.History),
                    Pair("Compare", Icons.Default.CompareArrows),
    Pair("Stats", Icons.Default.BarChart),
    Pair("Settings", Icons.Default.Settings)
                ).forEachIndexed { index, (label, icon) ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PondSelectorHeader(
                cycles = cycles,
                activeCycle = activeCycle,
                onSelect = { viewModel.selectCycle(it) },
                onAddClick = { showNewPondDialog = true },
                onDelete = { viewModel.deleteCycle(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (activeCycle != null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                        },
                        label = "tabChange"
                    ) { currentTab ->
                        when (currentTab) {
                            0 -> DashboardOverviewTab(
                                cycle = activeCycle!!,
                                plResult = plQuality,
                                stocking = stocking,
                                survival = survival,
                                cost = cost,
                                harvest = harvest,
                                feedRecommendation = feedRecommendation,
                                survivalForecast = survivalForecast,
                                diseaseRisk = diseaseRisk,
                                onNavigateToTab = { activeTab = it }
                            )
                            1 -> PlGateTab(
                                cycle = activeCycle!!,
                                result = plQuality,
                                onUpdate = { viewModel.updateActiveCycle(it) }
                            )
                            2 -> StockingEngineTab(
                                cycle = activeCycle!!,
                                result = stocking,
                                onUpdate = { viewModel.updateActiveCycle(it) }
                            )
                            3 -> SurvivalMonitorTab(
                                cycle = activeCycle!!,
                                result = survival,
                                onUpdate = { viewModel.updateActiveCycle(it) },
                                dailyReadings = activeReadings,
                                onLogReading = { feedGiven -> viewModel.logDailyReading(feedGiven) }
                            )
                            4 -> CostTrackerTab(
                                cycle = activeCycle!!,
                                result = cost,
                                tempAdjustedFcr = tempAdjustedFcr,
                                onUpdate = { viewModel.updateActiveCycle(it) }
                            )
                            5 -> HarvestOptimizerTab(
                                cycle = activeCycle!!,
                                result = harvest,
                                regionProfiles = allRegionProfiles,
                                activeRegionProfile = activeRegionProfile,
                                onUpdate = { viewModel.updateActiveCycle(it) },
                                onLinkRegionProfile = { profileId -> viewModel.linkRegionProfile(profileId) },
                                onSaveRegionProfile = { viewModel.saveRegionProfile(it) },
                                onDeleteRegionProfile = { viewModel.deleteRegionProfile(it) }
                            )
                            6 -> AiAdvisorTab(
                                cycle = activeCycle!!,
                                chatMessages = chatMessages,
                                isAiLoading = isAiLoading,
                                onSendMessage = { viewModel.sendChatMessage(it) }
                            )
                            7 -> ReportSummaryTab(
                                cycle = activeCycle!!,
                                plResult = plQuality,
                                stocking = stocking,
                                survival = survival,
                                cost = cost,
                                harvest = harvest,
                                dailyReadings = activeReadings
                            )
                            8 -> ReadingHistoryTab(
                                readings = activeReadings,
                                onDelete = { viewModel.deleteDailyReading(it) }
                            )
                            9 -> PondComparisonTab(
                                cycles = cycles,
                                allRegionProfiles = allRegionProfiles
                            )
                            10 -> PerformanceBenchmarkTab(
                                cycles = cycles
                            )
                            11 -> SettingsTab(
                                cycle = activeCycle!!,
                                onUpdate = { viewModel.updateActiveCycle(it) }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showNewPondDialog) {
        NewPondDialog(
            onDismiss = { showNewPondDialog = false },
            onConfirm = { name, size, density ->
                viewModel.createNewPond(name, size, density)
                showNewPondDialog = false
            }
        )
    }
}

@Composable
fun PondSelectorHeader(
    cycles: List<PondCycle>,
    activeCycle: PondCycle?,
    onSelect: (PondCycle) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (PondCycle) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ACTIVE POND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expandedMenu = true }
                    .testTag("pond_dropdown_trigger")
            ) {
                Text(
                    text = activeCycle?.pondName ?: "Select Pond...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Pond",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                cycles.forEach { cycle ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cycle.pondName, fontWeight = FontWeight.SemiBold)
                                if (cycles.size > 1) {
                                    IconButton(
                                        onClick = { onDelete(cycle) },
                                        modifier = Modifier.size(24.dp).testTag("delete_pond_${cycle.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = AquaticColors.AlarmRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            onSelect(cycle)
                            expandedMenu = false
                        },
                        modifier = Modifier.testTag("pond_option_${cycle.id}")
                    )
                }
            }
        }

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("add_pond_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Pond", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("New Pond", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewPondDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("1000") }
    var density by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setup New Pond Cycle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pond Identifier Name") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_name")
                )

                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Pond Surface Area (m²)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_size")
                )

                OutlinedTextField(
                    value = density,
                    onValueChange = { density = it },
                    label = { Text("Planned Density (PL/m²)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_density")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedSize = size.toDoubleOrNull() ?: 1000.0
                        val parsedDensity = density.toDoubleOrNull() ?: 60.0
                        onConfirm(name, parsedSize, parsedDensity)
                    }
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("Stock Pond")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_dismiss_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
