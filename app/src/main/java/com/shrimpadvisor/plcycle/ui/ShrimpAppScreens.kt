package com.shrimpadvisor.plcycle.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.data.RegionProfile

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
            // Horizontal scrollable TabRow or custom scrollable tabs for 7 sections
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
                    Pair("Report", Icons.Default.Summarize)
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
            // Global Pond Selector Area
            PondSelectorHeader(
                cycles = cycles,
                activeCycle = activeCycle,
                onSelect = { viewModel.selectCycle(it) },
                onAddClick = { showNewPondDialog = true },
                onDelete = { viewModel.deleteCycle(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (activeCycle != null) {
                // Interactive tabs content
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
                                onLogReading = { viewModel.logDailyReading() }
                            )
                            4 -> CostTrackerTab(
                                cycle = activeCycle!!,
                                result = cost,
                                onUpdate = { viewModel.updateActiveCycle(it) }
                            )
                            5 -> HarvestOptimizerTab(
                                cycle = activeCycle!!,
                                result = harvest,
                                regionProfiles = allRegionProfiles,
                                activeRegionProfile = activeRegionProfile,
                                onUpdate = { viewModel.updateActiveCycle(it) },
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

/**
 * Pond Selection Bar
 */
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

/**
 * Screen 0: Overview Dashboard Card-Deck
 */
@Composable
fun DashboardOverviewTab(
    cycle: PondCycle,
    plResult: AdvisorEngine.QualityResult?,
    stocking: AdvisorEngine.StockingResult?,
    survival: AdvisorEngine.SurvivalTrajectoryResult?,
    cost: AdvisorEngine.CostTrackingResult?,
    harvest: AdvisorEngine.HarvestOptimizerResult?,
    onNavigateToTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("dashboard_overview_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cycle Progress Card (Active Growth Card)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Decorative circular shape representing water flow/ponds
                    val circleColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.size(120.dp).align(Alignment.TopEnd)) {
                        drawCircle(
                            color = circleColor,
                            alpha = 0.05f,
                            radius = size.width / 2,
                            center = Offset(size.width * 1.1f, -size.height * 0.1f)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE GROWTH",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Vannamei • Cycle ${String.format("%02d", cycle.id % 10 + 1)}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Day ${cycle.currentAge} ",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = "Projected Harvest Size: ${cycle.harvestWeightTarget}g",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "EST. BIOMASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = harvest?.currentBiomass?.let { String.format("%,.0f kg", it) } ?: "--- kg",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Small decorative bar equalizers on the right side
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf(12, 24, 16, 32, 20).forEach { hDp ->
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(hDp.dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Module summary block grid
        item {
            Text("Module Health Scorecard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Mod 1: Quality Gate Card
        item {
            SummaryCard(
                title = "1. PL Quality Gate Score",
                statusText = plResult?.statusMessage ?: "Awaiting parameters",
                statusColor = when (plResult?.verdict) {
                    AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen
                    AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold
                    AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed
                    null -> AquaticColors.SoftMutedText
                },
                scoreText = plResult?.score?.let { String.format("%.0f%%", it) } ?: "--",
                icon = Icons.Default.VerifiedUser,
                onClick = { onNavigateToTab(1) }
            )
        }

        // Mod 2: Carrying Capacity Card
        item {
            val exceed = stocking?.carryingCapacityExceeded ?: false
            val capColor = if (exceed) AquaticColors.AlarmRed else AquaticColors.SafeGreen
            val capMsg = if (exceed) "DANGER: Carrying Capacity Exceeded!" else "Bio-load within safe carrying threshold."

            SummaryCard(
                title = "2. Stocking Safe Loading",
                statusText = capMsg,
                statusColor = capColor,
                scoreText = stocking?.proposedTotalQty?.let { String.format("%,.0f PL", it) } ?: "--",
                icon = Icons.Default.WaterDrop,
                onClick = { onNavigateToTab(2) }
            )
        }

        // Mod 3: Survival Trajectory Card
        item {
            SummaryCard(
                title = "3. Survival Trajectory",
                statusText = when (survival?.status) {
                    AdvisorEngine.SurvivalStatus.GREEN -> "GREEN: Excellent survival trajectory."
                    AdvisorEngine.SurvivalStatus.YELLOW -> "YELLOW: Caution. Deviation from expected curve."
                    AdvisorEngine.SurvivalStatus.RED -> "RED: Critical. Immediate remediation REQUIRED."
                    null -> "--"
                },
                statusColor = when (survival?.status) {
                    AdvisorEngine.SurvivalStatus.GREEN -> AquaticColors.SafeGreen
                    AdvisorEngine.SurvivalStatus.YELLOW -> AquaticColors.SandGold
                    AdvisorEngine.SurvivalStatus.RED -> AquaticColors.AlarmRed
                    null -> AquaticColors.SoftMutedText
                },
                scoreText = survival?.actualSurvival?.let { String.format("%.0f%%", it) } ?: "--",
                icon = Icons.Default.Analytics,
                onClick = { onNavigateToTab(3) }
            )
        }

        // Mod 4: Cost & FCR Card
        item {
            SummaryCard(
                title = "4. Food FCR & Debt Accumulation",
                statusText = cost?.fcrStatusMessage ?: "--",
                statusColor = when (cost?.fcrStatusColor) {
                    "success" -> AquaticColors.SafeGreen
                    "warning" -> AquaticColors.SandGold
                    "critical" -> AquaticColors.AlarmRed
                    else -> AquaticColors.SoftMutedText
                },
                scoreText = cost?.fcr?.let { String.format("FCR %.2f", it) } ?: "--",
                icon = Icons.Default.Payments,
                onClick = { onNavigateToTab(4) }
            )
        }

        // Mod 5: Harvest Optimizer Card
        item {
            val rec = if (harvest?.shouldHarvestNow == true) "HARVEST NOW: Reaching plateau." else "HOLD GROWING: Profitable holding window active."
            val optColor = if (harvest?.shouldHarvestNow == true) AquaticColors.SafeGreen else AquaticColors.ElectricTeal

            SummaryCard(
                title = "5. Harvest Window Optimizer",
                statusText = rec,
                statusColor = optColor,
                scoreText = harvest?.bestHoldScenario?.let { "Hold ${it.day}d" } ?: "Now",
                icon = Icons.Default.TrendingUp,
                onClick = { onNavigateToTab(5) }
            )
        }

        item {
            FeedingReminderCard(pondName = cycle.pondName)
        }
    }
}

@Composable
fun FeedingReminderCard(pondName: String) {
    val context = LocalContext.current
    var remindersEnabled by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AquaticColors.ElectricTeal.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AquaticColors.ElectricTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Feeding Reminders", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (remindersEnabled) "Twice-daily reminders active (06:00 & 18:00)"
                    else "Enable to get morning & evening feeding alerts",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )
            }
            Switch(
                checked = remindersEnabled,
                onCheckedChange = { enabled ->
                    remindersEnabled = enabled
                    val workManager = androidx.work.WorkManager.getInstance(context)
                    if (enabled) {
                        val data = androidx.work.Data.Builder()
                            .putString(com.shrimpadvisor.plcycle.FeedingReminderWorker.KEY_POND_NAME, pondName)
                            .build()
                        val morningRequest = androidx.work.PeriodicWorkRequestBuilder<com.shrimpadvisor.plcycle.FeedingReminderWorker>(
                            12, java.util.concurrent.TimeUnit.HOURS
                        ).setInputData(data)
                            .addTag(com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG)
                            .build()
                        workManager.enqueueUniquePeriodicWork(
                            com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG,
                            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                            morningRequest
                        )
                    } else {
                        workManager.cancelAllWorkByTag(com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG)
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    statusText: String,
    statusColor: Color,
    scoreText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(3.dp))
                Text(statusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = scoreText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Screen 1: PL Pre-Stocking Quality Gate
 */
@Composable
fun PlGateTab(
    cycle: PondCycle,
    result: AdvisorEngine.QualityResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("pl_gate_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pre-Stocking Quality Gate", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Grades baby shrimp health parameters prior to release inside ponds.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                // Score 1: Stress Tolerance
                ScoreSliderRow(
                    label = "Stress Tolerance Test",
                    value = cycle.stressToleranceScore,
                    description = "Survival score in high saline / formalin shock tests.",
                    onValueChange = { score -> onUpdate { it.copy(stressToleranceScore = score) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Score 2: Gut Fullness
                ScoreSliderRow(
                    label = "Gut Fullness & Lipid Ratio",
                    value = cycle.gutFullnessScore,
                    description = "Percentage of PLs showing full guts and high lipids under scope.",
                    onValueChange = { score -> onUpdate { it.copy(gutFullnessScore = score) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Score 3: Supplier Integrity
                ScoreSliderRow(
                    label = "Hatchery Historical Score",
                    value = cycle.supplierScore,
                    description = "Historical SPF disease-free verification record of this hatchery.",
                    onValueChange = { score -> onUpdate { it.copy(supplierScore = score) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Verdict
        if (result != null) {
            val panelBg = when (result.verdict) {
                AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen.copy(alpha = 0.08f)
                AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold.copy(alpha = 0.12f)
                AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed.copy(alpha = 0.08f)
            }
            val titleColor = when (result.verdict) {
                AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen
                AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold
                AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = panelBg),
                border = BorderStroke(1.dp, titleColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (result.verdict) {
                                AdvisorEngine.QualityVerdict.STOCK -> Icons.Default.CheckCircle
                                AdvisorEngine.QualityVerdict.HOLD -> Icons.Default.PauseCircle
                                AdvisorEngine.QualityVerdict.REJECT -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = titleColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = result.statusMessage,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Cycle Recommendations:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    result.recommendations.forEach { recommendation ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
                            Text(recommendation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSliderRow(
    label: String,
    value: Int,
    description: String,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
            Text(
                text = "$value%",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

/**
 * Screen 2: Stocking Decision Engine
 */
@Composable
fun StockingEngineTab(
    cycle: PondCycle,
    result: AdvisorEngine.StockingResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("stocking_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stocking Densities & Water Indices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Enforces water security envelopes and computes safe stocking bio-loads.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                // Physical metrics fields
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.pondSize.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(pondSize = parsed) }
                        },
                        label = { Text("Pond Size (m²)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_size"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.proposedDensity.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(proposedDensity = parsed) }
                        },
                        label = { Text("Density (PL/m²)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_density"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cycle.harvestWeightTarget.let { if (it == 0.0) "" else it.toString() },
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onUpdate { current -> current.copy(harvestWeightTarget = parsed) }
                    },
                    label = { Text("Target Harvest Body Weight (grams)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_pond_weight_target"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Active Water Metrics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                // Oxygen Slider
                WaterMetricSlider(
                    label = "Dissolved Oxygen (DO)",
                    value = cycle.doLevel,
                    range = 1.0f..10.0f,
                    unit = " ppm",
                    onValueChange = { onUpdate { c -> c.copy(doLevel = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // pH Slider
                WaterMetricSlider(
                    label = "pH Index",
                    value = cycle.ph,
                    range = 5.0f..10.0f,
                    unit = "",
                    onValueChange = { onUpdate { c -> c.copy(ph = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Salinity Slider
                WaterMetricSlider(
                    label = "Salinity",
                    value = cycle.salinity,
                    range = 5.0f..45.0f,
                    unit = " ppt",
                    onValueChange = { onUpdate { c -> c.copy(salinity = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature Slider
                WaterMetricSlider(
                    label = "Temperature",
                    value = cycle.temp,
                    range = 20.0f..38.0f,
                    unit = " °C",
                    onValueChange = { onUpdate { c -> c.copy(temp = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // TAN Level
                WaterMetricSlider(
                    label = "Ammonia (TAN)",
                    value = cycle.tanLevel,
                    range = 0.0f..2.5f,
                    unit = " ppm",
                    onValueChange = { onUpdate { c -> c.copy(tanLevel = it) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculations & Results Visuals
        if (result != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bio-Load Carrying Capacity Check", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    BiomassCapacityBar(
                        proposedBiomassKg = result.proposedBiomass,
                        maxSafeBiomassKg = result.maxSafeBiomass
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Optimal Density recommendation: ${result.optimalDensity.toInt()} PL/m² (total: ${String.format("%,.0f", result.totalOptimalQty)} PL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Water Quality Stability Checklist:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    result.parameterChecks.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (check.isOptimal) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (check.isOptimal) AquaticColors.SafeGreen else AquaticColors.AlarmRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(check.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(check.valueString, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                Text(check.rangeMessage, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterMetricSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = String.format("%.1f", value) + unit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
            modifier = Modifier.height(28.dp)
        )
    }
}

/**
 * Screen 3: Survival Trajectory Monitor
 */
@Composable
fun SurvivalMonitorTab(
    cycle: PondCycle,
    result: AdvisorEngine.SurvivalTrajectoryResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit,
    dailyReadings: List<DailyReading> = emptyList(),
    onLogReading: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("survival_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Survival Trajectory & Diagnostics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Compares sampling count parameters and isolates environmental/pathogenic factors.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.currentAge.let { if (it == 0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 1
                            onUpdate { current -> current.copy(currentAge = parsed) }
                        },
                        label = { Text("Pond Age (Days)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_age"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.estimatedSurvival.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(estimatedSurvival = parsed) }
                        },
                        label = { Text("Est. Survival %", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_survival"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log today's reading button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${dailyReadings.size} reading(s) logged",
                fontSize = 12.sp,
                color = AquaticColors.SoftMutedText
            )
            OutlinedButton(onClick = onLogReading) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Today", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Curve Graph
        if (result != null) {
            Text("Survival Deviation Graphic", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            SurvivalCurveChart(
                currentDay = cycle.currentAge,
                estimatedSurvival = cycle.estimatedSurvival,
                expectedSurvival = result.expectedSurvival,
                historicalReadings = dailyReadings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Water quality trend chart — only rendered when ≥2 readings are available
            if (dailyReadings.size >= 2) {
                WaterQualityTrendChart(readings = dailyReadings)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Diagnostic outputs
            val titleColor = when (result.status) {
                AdvisorEngine.SurvivalStatus.GREEN -> AquaticColors.SafeGreen
                AdvisorEngine.SurvivalStatus.YELLOW -> AquaticColors.SandGold
                AdvisorEngine.SurvivalStatus.RED -> AquaticColors.AlarmRed
            }
            val borderBg = titleColor.copy(alpha = 0.08f)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = borderBg),
                border = BorderStroke(1.dp, titleColor.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Classification: " + result.classification,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deviation: ${String.format("%+.1f%%", result.deviation)} against reference limit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Symptoms Diagnostic:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    result.diagnostics.forEach { diag ->
                        Text("• $diag", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Directed Interventions Required:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    result.actionSteps.forEach { step ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("⚡ ", fontSize = 11.sp)
                            Text(step, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 4: Cost & FCR Tracker
 */
@Composable
fun CostTrackerTab(
    cycle: PondCycle,
    result: AdvisorEngine.CostTrackingResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("cost_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feed & Operational Cost Logbook", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Tracks ongoing physical feed inputs, electricity, probiotics, and labor expenses.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.currentAbw.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 1.0
                            onUpdate { current -> current.copy(currentAbw = parsed) }
                        },
                        label = { Text("Average Weight (g)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_abw"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.totalFeedConsumed.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(totalFeedConsumed = parsed) }
                        },
                        label = { Text("Total Feed (kg)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_feed"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Fixed Cost Multipliers (USD)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cycle.feedCostPerKg.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onUpdate { current -> current.copy(feedCostPerKg = parsed) }
                    },
                    label = { Text("Feed Cost per kg ($)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.plUnitCost.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(plUnitCost = parsed) }
                        },
                        label = { Text("PL Unit cost ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cycle.aerationCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(aerationCostPerDay = parsed) }
                        },
                        label = { Text("Aeration/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.probioticCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(probioticCostPerDay = parsed) }
                        },
                        label = { Text("Probiotic/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cycle.laborCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(laborCostPerDay = parsed) }
                        },
                        label = { Text("Labor/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Running Calculations & FCR Readout
        if (result != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dial gauge card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.width(160.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Feeding Efficiency", fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        FcrGauge(fcr = result.fcr)
                    }
                }

                // Costs list card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Expense Breakdown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        CostRowItem("Stock PL Cost", result.totalPlCost)
                        CostRowItem("Feed Expenses", result.totalFeedCost)
                        CostRowItem("Operational Costs", result.totalOperationalCost)

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Accumulated: ${String.format("$%,.2f", result.totalAccumulatedCost)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cost per kg: ${String.format("$%,.2f/kg", result.costPerKg)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CostRowItem(label: String, valUSD: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
        Text(String.format("$%,.2f", valUSD), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Screen 5: Harvest Window Optimizer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestOptimizerTab(
    cycle: PondCycle,
    result: AdvisorEngine.HarvestOptimizerResult?,
    regionProfiles: List<RegionProfile> = emptyList(),
    activeRegionProfile: RegionProfile? = null,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit,
    onSaveRegionProfile: (RegionProfile) -> Unit = {},
    onDeleteRegionProfile: (RegionProfile) -> Unit = {}
) {
    var showDiseaseScenario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("optimizer_tab")
    ) {
        // ── Region Selector Card ──────────────────────────────────────────────
        RegionSelectorCard(
            regionProfiles = regionProfiles,
            activeRegionProfile = activeRegionProfile,
            onSelectProfile = { selected ->
                onUpdate { it.copy(regionProfileId = selected?.id) }
            },
            onSaveProfile = onSaveRegionProfile,
            onDeleteProfile = onDeleteRegionProfile
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Harvest Window Opt. Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Simulates 30-day future feeding schedules and evaluates size-market premiums to find highest peak profits.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.averageDailyGain.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(averageDailyGain = parsed) }
                        },
                        label = { Text("Avg Daily Gain (g/day)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_adg"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.marketPriceTarget.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(marketPriceTarget = parsed) }
                        },
                        label = { Text("Market Price Target ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_market_price"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Mortality Settings Card ──────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.testTag("mortality_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Mortality Settings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Configure baseline daily mortality and weekly acceleration for the harvest optimizer.",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mortality rate slider: 0.001 (0.1%) – 0.020 (2.0%), step 0.001
                val mortalitySteps = 19 // steps between 0.1% and 2.0% at 0.1% increments
                val mortalitySliderValue = ((cycle.customMortalityRate.coerceIn(0.001, 0.020) - 0.001) / 0.001).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Mortality Rate", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.1f%%/day", cycle.customMortalityRate * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = mortalitySliderValue,
                    onValueChange = { raw ->
                        val stepped = raw.toInt().coerceIn(0, mortalitySteps)
                        val newRate = 0.001 + stepped * 0.001
                        onUpdate { current -> current.copy(customMortalityRate = newRate) }
                    },
                    valueRange = 0f..mortalitySteps.toFloat(),
                    steps = mortalitySteps - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_mortality_rate"),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.1%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                    Text("2.0%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Acceleration slider: 0.0 – 0.002 (0.0 – 0.2%/week), step 0.0001
                val accelSteps = 20 // steps 0.0 to 0.002 at 0.0001 increments
                val accelSliderValue = ((cycle.mortalityAcceleration.coerceIn(0.0, 0.002) / 0.0001)).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mortality Acceleration", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        String.format("+%.2f%%/wk", cycle.mortalityAcceleration * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Slider(
                    value = accelSliderValue,
                    onValueChange = { raw ->
                        val stepped = raw.toInt().coerceIn(0, accelSteps)
                        val newAccel = stepped * 0.0001
                        onUpdate { current -> current.copy(mortalityAcceleration = newAccel) }
                    },
                    valueRange = 0f..accelSteps.toFloat(),
                    steps = accelSteps - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_mortality_acceleration"),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.00%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                    Text("0.20%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Disease Scenario toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Disease Scenario", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "If disease hits (×2.5 mortality), compares projected best harvest day and revenue delta.",
                            fontSize = 10.sp,
                            color = AquaticColors.SoftMutedText
                        )
                    }
                    Switch(
                        checked = showDiseaseScenario,
                        onCheckedChange = { showDiseaseScenario = it },
                        modifier = Modifier.testTag("toggle_disease_scenario")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result != null) {
            Text("30-Day Profit Projection Trend", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            ProfitScenarioBarChart(
                scenarios = result.holdScenariosList,
                bestHoldDay = result.bestHoldScenario?.day
            )

            Spacer(modifier = Modifier.height(16.dp))

            val optimizerBg = if (result.shouldHarvestNow) AquaticColors.SafeGreen.copy(alpha = 0.1f) else AquaticColors.ElectricTeal.copy(alpha = 0.08f)
            val borderClr = if (result.shouldHarvestNow) AquaticColors.SafeGreen else AquaticColors.ElectricTeal

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = optimizerBg),
                border = BorderStroke(1.dp, borderClr.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (result.shouldHarvestNow) Icons.Default.DoneOutline else Icons.Default.TrendingFlat,
                            contentDescription = null,
                            tint = borderClr
                        )
                        Text(
                            text = if (result.shouldHarvestNow) "RECOMMENDATION: HARVEST NOW" else "RECOMMENDATION: HOLD FOR MORE DAYS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = borderClr
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (result.shouldHarvestNow) {
                            "Shrimp biological mass curves indicate growth deceleration. Daily feed maintenance overhead exceeds size-based market price premiums. Promptly harvest and reseed."
                        } else {
                            "Hold stocking with expected ADG (${cycle.averageDailyGain}g/day) for ${result.bestHoldScenario?.day} more days to maximize cash outcomes. Projected net premium: ${String.format("$%,.2f", result.profitDifferential)}"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }

            // ── Disease Scenario Card ─────────────────────────────────────────
            if (showDiseaseScenario) {
                val diseaseResult = result.scenarioResult
                if (diseaseResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val diseaseBg = Color(0xFFFF5722).copy(alpha = 0.08f)
                    val diseaseBorder = Color(0xFFFF5722)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = diseaseBg),
                        border = BorderStroke(1.dp, diseaseBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("disease_scenario_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = diseaseBorder
                                )
                                Text(
                                    "If Disease Hits (×2.5 Mortality)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = diseaseBorder
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val diseaseBestDay = diseaseResult.bestHoldScenario?.day
                            val normalBestDay = result.bestHoldScenario?.day
                            val revenueDelta = diseaseResult.profitDifferential - result.profitDifferential

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Best Harvest Day", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                                    Text(
                                        if (diseaseResult.shouldHarvestNow) "Harvest Now" else "Day +${diseaseBestDay}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = diseaseBorder
                                    )
                                    if (normalBestDay != null && diseaseBestDay != null && diseaseBestDay != normalBestDay) {
                                        Text(
                                            "(normal: Day +${normalBestDay})",
                                            fontSize = 10.sp,
                                            color = AquaticColors.SoftMutedText
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Revenue Delta vs. Normal", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                                    Text(
                                        (if (revenueDelta >= 0) "+" else "") + String.format("$%,.2f", revenueDelta),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (revenueDelta >= 0) AquaticColors.SafeGreen else diseaseBorder
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Applied mortality rate: ${String.format("%.1f%%/day", diseaseResult.mortalityRatePerDay * 100)} (baseline ×2.5). Harvest earlier to minimize disease-driven crop losses.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Region price profile selector with read-only details panel for built-in profiles
 * and an editable expansion panel for custom profiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectorCard(
    regionProfiles: List<RegionProfile>,
    activeRegionProfile: RegionProfile?,
    onSelectProfile: (RegionProfile?) -> Unit,
    onSaveProfile: (RegionProfile) -> Unit,
    onDeleteProfile: (RegionProfile) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showEditPanel by remember { mutableStateOf(false) }

    // Edit state for a custom profile being edited/created
    var editingProfile by remember { mutableStateOf<RegionProfile?>(null) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.testTag("region_selector_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Regional Price Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Select a market region to apply locale-specific price brackets and default input costs.",
                fontSize = 11.sp,
                color = AquaticColors.SoftMutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown selector
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = activeRegionProfile?.regionName ?: "Generic (no region selected)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Market Region", fontSize = 11.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("region_dropdown"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    // "None" option to clear the profile
                    DropdownMenuItem(
                        text = {
                            Text(
                                "None (use manual costs)",
                                fontSize = 13.sp,
                                fontWeight = if (activeRegionProfile == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectProfile(null)
                            dropdownExpanded = false
                        },
                        modifier = Modifier.testTag("region_option_none")
                    )
                    regionProfiles.forEach { profile ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            profile.regionName,
                                            fontSize = 13.sp,
                                            fontWeight = if (profile.id == activeRegionProfile?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            "${profile.currency} • 20g: ${String.format("%.1f", profile.priceAt20g)} – 40g: ${String.format("%.1f", profile.priceAt40g)}",
                                            fontSize = 10.sp,
                                            color = AquaticColors.SoftMutedText
                                        )
                                    }
                                    if (!profile.isBuiltIn) {
                                        IconButton(
                                            onClick = {
                                                onDeleteProfile(profile)
                                                dropdownExpanded = false
                                            },
                                            modifier = Modifier.size(24.dp)
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
                                onSelectProfile(profile)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("region_option_${profile.id}")
                        )
                    }
                }
            }

            // Show price brackets and action buttons when a profile is active
            if (activeRegionProfile != null) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Price Brackets & Default Costs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!activeRegionProfile.isBuiltIn) {
                            IconButton(
                                onClick = {
                                    editingProfile = activeRegionProfile
                                    showEditPanel = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { showEditPanel = !showEditPanel },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (showEditPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showEditPanel) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (showEditPanel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RegionProfileDetailPanel(
                        profile = activeRegionProfile,
                        editable = !activeRegionProfile.isBuiltIn,
                        onSave = onSaveProfile
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button to create a new custom profile
            OutlinedButton(
                onClick = {
                    editingProfile = RegionProfile(
                        regionName = "",
                        isBuiltIn = false
                    )
                    showEditPanel = true
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("add_region_profile_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Custom Region", fontSize = 12.sp)
            }
        }
    }

    // Dialog for editing / creating a custom profile
    val ep = editingProfile
    if (ep != null && showEditPanel && (ep.id == 0 || !ep.isBuiltIn)) {
        RegionProfileEditDialog(
            profile = ep,
            onDismiss = {
                editingProfile = null
                showEditPanel = false
            },
            onConfirm = { saved ->
                onSaveProfile(saved)
                editingProfile = null
                showEditPanel = false
            }
        )
    }
}

/**
 * Inline read-only (or editable) detail panel showing the 5 price brackets
 * and the 4 default cost fields of a RegionProfile.
 */
@Composable
fun RegionProfileDetailPanel(
    profile: RegionProfile,
    editable: Boolean,
    onSave: (RegionProfile) -> Unit
) {
    var draft by remember(profile.id) { mutableStateOf(profile) }

    val brackets = listOf(
        "20g" to draft.priceAt20g,
        "25g" to draft.priceAt25g,
        "30g" to draft.priceAt30g,
        "35g" to draft.priceAt35g,
        "40g" to draft.priceAt40g
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Price per kg (${draft.currency})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                brackets.forEach { (label, price) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                        Text(
                            String.format("%.1f", price),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            Text("Default Input Costs (${draft.currency}/day or /kg)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            listOf(
                "Feed ($/kg)" to String.format("%.2f", draft.feedCostDefault),
                "Labor ($/day)" to String.format("%.2f", draft.laborCostDefault),
                "Aeration ($/day)" to String.format("%.2f", draft.aerationCostDefault),
                "Probiotic ($/day)" to String.format("%.2f", draft.probioticCostDefault)
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 11.sp, color = AquaticColors.SoftMutedText)
                    Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (profile.isBuiltIn) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Built-in region — read only. Create a custom region to edit.",
                    fontSize = 10.sp,
                    color = AquaticColors.SoftMutedText
                )
            }
        }
    }
}

/**
 * Dialog for creating or editing a custom RegionProfile.
 */
@Composable
fun RegionProfileEditDialog(
    profile: RegionProfile,
    onDismiss: () -> Unit,
    onConfirm: (RegionProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.regionName) }
    var currency by remember { mutableStateOf(profile.currency) }
    var p20 by remember { mutableStateOf(profile.priceAt20g.toString()) }
    var p25 by remember { mutableStateOf(profile.priceAt25g.toString()) }
    var p30 by remember { mutableStateOf(profile.priceAt30g.toString()) }
    var p35 by remember { mutableStateOf(profile.priceAt35g.toString()) }
    var p40 by remember { mutableStateOf(profile.priceAt40g.toString()) }
    var feed by remember { mutableStateOf(profile.feedCostDefault.toString()) }
    var labor by remember { mutableStateOf(profile.laborCostDefault.toString()) }
    var aeration by remember { mutableStateOf(profile.aerationCostDefault.toString()) }
    var probiotic by remember { mutableStateOf(profile.probioticCostDefault.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (profile.id == 0) "New Region Profile" else "Edit Region Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Region Name", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency Code (e.g. USD)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Price per kg by size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = p20,
                        onValueChange = { p20 = it },
                        label = { Text("@20g", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = p25,
                        onValueChange = { p25 = it },
                        label = { Text("@25g", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = p30,
                        onValueChange = { p30 = it },
                        label = { Text("@30g", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = p35,
                        onValueChange = { p35 = it },
                        label = { Text("@35g", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = p40,
                        onValueChange = { p40 = it },
                        label = { Text("@40g", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Default Input Costs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = feed,
                        onValueChange = { feed = it },
                        label = { Text("Feed $/kg", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = labor,
                        onValueChange = { labor = it },
                        label = { Text("Labor $/day", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = aeration,
                        onValueChange = { aeration = it },
                        label = { Text("Aeration $/day", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = probiotic,
                        onValueChange = { probiotic = it },
                        label = { Text("Probiotic $/day", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            profile.copy(
                                regionName = name.trim(),
                                currency = currency.trim().ifBlank { "USD" },
                                priceAt20g = p20.toDoubleOrNull() ?: profile.priceAt20g,
                                priceAt25g = p25.toDoubleOrNull() ?: profile.priceAt25g,
                                priceAt30g = p30.toDoubleOrNull() ?: profile.priceAt30g,
                                priceAt35g = p35.toDoubleOrNull() ?: profile.priceAt35g,
                                priceAt40g = p40.toDoubleOrNull() ?: profile.priceAt40g,
                                feedCostDefault = feed.toDoubleOrNull() ?: profile.feedCostDefault,
                                laborCostDefault = labor.toDoubleOrNull() ?: profile.laborCostDefault,
                                aerationCostDefault = aeration.toDoubleOrNull() ?: profile.aerationCostDefault,
                                probioticCostDefault = probiotic.toDoubleOrNull() ?: profile.probioticCostDefault
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Screen 6: Complete Cycle Report
 */
@Composable
fun ReportSummaryTab(
    cycle: PondCycle,
    plResult: AdvisorEngine.QualityResult?,
    stocking: AdvisorEngine.StockingResult?,
    survival: AdvisorEngine.SurvivalTrajectoryResult?,
    cost: AdvisorEngine.CostTrackingResult?,
    harvest: AdvisorEngine.HarvestOptimizerResult?,
    dailyReadings: List<DailyReading> = emptyList()
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showCopiedToast by remember { mutableStateOf(false) }

    val reportText = """
=== SHRIMP PL CYCLE ADVISOR REPORT ===
Pond Name: ${cycle.pondName}
Evaluation Date: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(cycle.stockingDate))}
Pond Specs: ${cycle.pondSize} m² | Current Age: ${cycle.currentAge} Days

[1] PL PRE-STOCKING QUALITY
Verdict: ${plResult?.statusMessage ?: "--"}
Avg Quality rating: ${plResult?.score?.let { String.format("%.1f%%", it) } ?: "--"}

[2] STOCKING & CAPACITIES
Proposed density: ${cycle.proposedDensity} PL/m²
Total optimal recommended quantity: ${stocking?.totalOptimalQty?.toInt() ?: "--"} PL
Safe carrying limit capacity exceeded: ${stocking?.carryingCapacityExceeded ?: false}

[3] SURVIVAL TRAJECTORY
Current estimated survival: ${cycle.estimatedSurvival}%
Expected survival trend: ${survival?.expectedSurvival?.let { String.format("%.1f%%", it) } ?: "--"}
Diagnostics isolates: ${survival?.classification ?: "--"}

[4] MONEY TRACKING
FCR Index: ${cost?.fcr?.let { String.format("%.2f", it) } ?: "--"}
Accumulated Expenses: ${cost?.totalAccumulatedCost?.let { String.format("$%,.2f", it) } ?: "--"}
Cost per kg: ${cost?.costPerKg?.let { String.format("$%,.2f/kg", it) } ?: "--"}

[5] HARVEST OPTIMIZATION
Best Action: ${if (harvest?.shouldHarvestNow == true) "HARVEST NOW" else "HOLD GROWING"}
Best holding scenario peak: ${harvest?.bestHoldScenario?.let { "Hold ${it.day} more Days" } ?: "Harvest right now"}
Projected profitability uplift: ${harvest?.profitDifferential?.let { String.format("$%,.2f", it) } ?: "--"}
======================================
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("report_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 Comprehensive Cycle Report",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Consolidated summaries across all 5 operational vectors. Share or export copy to files or team chat.",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030D1A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA1B3C6),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(reportText))
                            showCopiedToast = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_report_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                putExtra(Intent.EXTRA_SUBJECT, "Shrimp Cycle Report — ${cycle.pondName}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_report_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }

                if (dailyReadings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val pdfDoc = android.graphics.pdf.PdfDocument()
                            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                            val page = pdfDoc.startPage(pageInfo)
                            val canvas = page.canvas
                            val paint = android.graphics.Paint().apply { isAntiAlias = true }
                            var y = 60f

                            fun drawLine(text: String, size: Float = 12f, bold: Boolean = false) {
                                paint.textSize = size
                                paint.typeface = if (bold) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                                canvas.drawText(text, 40f, y, paint)
                                y += size + 6f
                            }

                            drawLine("SHRIMP PL CYCLE ADVISOR — FULL REPORT", 16f, true)
                            drawLine("Pond: ${cycle.pondName}  |  Area: ${cycle.pondSize} m²  |  Age: ${cycle.currentAge} days", 11f)
                            drawLine("Generated: ${java.text.DateFormat.getDateInstance().format(java.util.Date())}", 10f)
                            y += 8f
                            drawLine("─────────────────────────────────────────────────", 10f)
                            y += 4f
                            drawLine("PL Quality: ${plResult?.statusMessage ?: "N/A"}  (${plResult?.score?.let { String.format("%.1f%%", it) } ?: "--"})", 11f)
                            drawLine("Stocking Density: ${cycle.proposedDensity} PL/m²  |  Capacity exceeded: ${stocking?.carryingCapacityExceeded ?: false}", 11f)
                            drawLine("Survival: ${cycle.estimatedSurvival}%  |  Expected: ${survival?.expectedSurvival?.let { String.format("%.1f%%", it) } ?: "--"}", 11f)
                            drawLine("FCR: ${cost?.fcr?.let { String.format("%.2f", it) } ?: "--"}  |  Total cost: ${cost?.totalAccumulatedCost?.let { String.format("$%,.2f", it) } ?: "--"}", 11f)
                            drawLine("Harvest: ${if (harvest?.shouldHarvestNow == true) "HARVEST NOW" else "HOLD"}  |  Uplift: ${harvest?.profitDifferential?.let { String.format("$%,.2f", it) } ?: "--"}", 11f)

                            if (dailyReadings.isNotEmpty()) {
                                y += 8f
                                drawLine("─────────────────────────────────────────────────", 10f)
                                drawLine("DAILY READINGS", 12f, true)
                                drawLine("Day  Survival%  DO    TAN   pH    Temp  ABW", 9f, true)
                                dailyReadings.sortedBy { it.pondAge }.forEach { r ->
                                    if (y < 800f) {
                                        drawLine(
                                            String.format(
                                                "%-4d %-9.1f  %-5.2f %-5.2f %-5.1f %-5.1f %-5.1f",
                                                r.pondAge, r.survivalPct, r.doLevel, r.tanLevel, r.ph, r.temp, r.abw
                                            ), 9f
                                        )
                                    }
                                }
                            }

                            pdfDoc.finishPage(page)
                            val pdfFileName = "${cycle.pondName.replace(" ", "_")}_report.pdf"
                            val pdfFile = java.io.File(context.cacheDir, pdfFileName)
                            pdfFile.outputStream().use { pdfDoc.writeTo(it) }
                            pdfDoc.close()
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Shrimp Report — ${cycle.pondName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export PDF"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Full Report (PDF)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val csv = buildString {
                                appendLine("Day,Survival %,DO (mg/L),TAN (mg/L),pH,Temp (°C),ABW (g),Notes")
                                dailyReadings.sortedBy { it.pondAge }.forEach { r ->
                                    val notes = r.notes.replace(",", ";").replace("\n", " ")
                                    appendLine("${r.pondAge},${r.survivalPct},${r.doLevel},${r.tanLevel},${r.ph},${r.temp},${r.abw},$notes")
                                }
                            }
                            val fileName = "${cycle.pondName.replace(" ", "_")}_readings.csv"
                            val file = java.io.File(context.cacheDir, fileName)
                            file.writeText(csv)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Daily Readings — ${cycle.pondName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export CSV"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Daily Readings (CSV)", fontWeight = FontWeight.Bold)
                    }
                }

                if (showCopiedToast) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AquaticColors.SafeGreen.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Copied report successfully!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticColors.SafeGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add new Pond Cycle
 */
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
