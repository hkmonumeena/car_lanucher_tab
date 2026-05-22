package com.ruchitech.carlanuchertab.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.carlanuchertab.roomdb.data.FuelQuickFillHints
import com.ruchitech.carlanuchertab.trip.TripEntity
import com.ruchitech.carlanuchertab.trip.TripExpenseType
import com.ruchitech.carlanuchertab.trip.TripExpenseWithShares
import com.ruchitech.carlanuchertab.trip.TripPersonEntity
import com.ruchitech.carlanuchertab.trip.TripSettlementHint
import com.ruchitech.carlanuchertab.trip.TripStatus
import com.ruchitech.carlanuchertab.trip.TripSummary
import com.ruchitech.carlanuchertab.trip.TripViewModel
import com.ruchitech.carlanuchertab.ui.composables.CockpitControlChip
import com.ruchitech.carlanuchertab.ui.composables.CockpitPalette
import com.ruchitech.carlanuchertab.ui.composables.CockpitSectionHeader
import com.ruchitech.carlanuchertab.ui.composables.FuelLogDialog
import com.ruchitech.carlanuchertab.ui.composables.FuelLogsList
import com.ruchitech.carlanuchertab.ui.composables.cockpitBackgroundBrush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class TripsPage { Home, Start, Detail, AddExpense, AddFuel, End }

@Composable
fun TripsScreen(
    onBack: () -> Unit,
    viewModel: TripViewModel = hiltViewModel(),
) {
    val activeTrip by viewModel.activeTrip.collectAsState()
    val completedTrips by viewModel.completedTrips.collectAsState()
    val fuelLogs by viewModel.fuelLogs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var page by remember { mutableStateOf(TripsPage.Home) }
    var selectedTripId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingExpense by remember { mutableStateOf<TripExpenseWithShares?>(null) }
    var showStandaloneFuelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    val currentTripId = when (page) {
        TripsPage.Detail, TripsPage.AddExpense, TripsPage.AddFuel, TripsPage.End -> selectedTripId ?: activeTrip?.id
        else -> null
    }
    val currentTrip by remember(currentTripId) {
        currentTripId?.let(viewModel::observeTrip) ?: kotlinx.coroutines.flow.flowOf(null)
    }.collectAsState(initial = null)
    val persons by remember(currentTripId) {
        currentTripId?.let(viewModel::observePersons) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val expenses by remember(currentTripId) {
        currentTripId?.let(viewModel::observeExpenses) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val summary by remember(currentTripId) {
        currentTripId?.let(viewModel::observeSummary) ?: kotlinx.coroutines.flow.flowOf(TripSummary())
    }.collectAsState(initial = TripSummary())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CockpitPalette.BackgroundBottom,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cockpitBackgroundBrush())
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TripsTopBar(
                title = when (page) {
                    TripsPage.Home -> "Trips & Fuel"
                    TripsPage.Start -> "Start Trip"
                    TripsPage.Detail -> currentTrip?.name ?: "Trip Detail"
                    TripsPage.AddExpense -> if (editingExpense == null) "Add Expense" else "Edit Expense"
                    TripsPage.AddFuel -> if (editingExpense == null) "Add Fuel" else "Edit Fuel"
                    TripsPage.End -> "End Trip"
                },
                onBack = {
                    if (page == TripsPage.Home) onBack() else {
                        editingExpense = null
                        page = if (currentTripId != null && page != TripsPage.Detail) TripsPage.Detail else TripsPage.Home
                    }
                }
            )

            when (page) {
                TripsPage.Home -> TripsHomeScreen(
                    activeTrip = activeTrip,
                    completedTrips = completedTrips,
                    fuelLogsCount = fuelLogs.size,
                    onStartTrip = { page = TripsPage.Start },
                    onOpenTrip = {
                        selectedTripId = it.id
                        page = TripsPage.Detail
                    },
                    onAddStandaloneFuel = { showStandaloneFuelDialog = true },
                    fuelLogsContent = {
                        val quickHints = remember(fuelLogs) {
                            val last = fuelLogs.firstOrNull()
                            FuelQuickFillHints(last?.fuelPrice, last?.liters)
                        }
                        FuelLogsList(
                            fuelLogs = fuelLogs,
                            onClose = {},
                            onAddNew = { showStandaloneFuelDialog = true },
                            onDelete = viewModel::deleteStandaloneFuelLog,
                            embeddedInParentScroll = true,
                        )
                        if (showStandaloneFuelDialog) {
                            FuelLogDialog(
                                onDismiss = { showStandaloneFuelDialog = false },
                                quickFillHints = quickHints,
                                onSubmit = { log ->
                                    viewModel.insertStandaloneFuelLog(log)
                                    showStandaloneFuelDialog = false
                                }
                            )
                        }
                    }
                )

                TripsPage.Start -> StartTripScreen(
                    onCreate = { name, odo, names, notes ->
                        viewModel.createTrip(name, odo, names, notes)
                        page = TripsPage.Home
                    }
                )

                TripsPage.Detail -> currentTrip?.let { trip ->
                    TripDetailScreen(
                        trip = trip,
                        persons = persons,
                        expenses = expenses,
                        summary = summary,
                        onAddFuel = {
                            editingExpense = null
                            page = TripsPage.AddFuel
                        },
                        onAddExpense = {
                            editingExpense = null
                            page = TripsPage.AddExpense
                        },
                        onAddToll = {
                            editingExpense = null
                            page = TripsPage.AddExpense
                        },
                        onEndTrip = { page = TripsPage.End },
                        onDeleteExpense = viewModel::deleteExpense,
                        onEditExpense = {
                            editingExpense = it
                            page = if (it.expense.type == TripExpenseType.FUEL.name) TripsPage.AddFuel else TripsPage.AddExpense
                        },
                        onAddPerson = viewModel::addPerson,
                    )
                } ?: EmptyPanel("Trip not found", "Open a trip from history or start a new one.")

                TripsPage.AddExpense -> currentTrip?.let { trip ->
                    AddTripExpenseScreen(
                        trip = trip,
                        persons = persons,
                        initialType = TripExpenseType.OTHER,
                        editing = editingExpense,
                        fuelMode = false,
                        onSave = { input ->
                            if (editingExpense == null) {
                                viewModel.addExpense(trip.id, input.type, input.title, input.amount, input.paidBy, input.splitEnabled, input.selectedPersons, input.odo, null, null, input.note)
                            } else {
                                viewModel.updateExpense(editingExpense!!.expense.id, trip.id, input.type, input.title, input.amount, input.paidBy, input.splitEnabled, input.selectedPersons, input.odo, null, null, input.note, editingExpense!!.expense.createdAt)
                            }
                            editingExpense = null
                            page = TripsPage.Detail
                        }
                    )
                }

                TripsPage.AddFuel -> currentTrip?.let { trip ->
                    AddTripExpenseScreen(
                        trip = trip,
                        persons = persons,
                        initialType = TripExpenseType.FUEL,
                        editing = editingExpense,
                        fuelMode = true,
                        onSave = { input ->
                            if (editingExpense == null) {
                                viewModel.addExpense(trip.id, TripExpenseType.FUEL, input.title.ifBlank { "Fuel" }, input.amount, input.paidBy, input.splitEnabled, input.selectedPersons, input.odo, input.liters, input.price, input.note)
                            } else {
                                viewModel.updateExpense(editingExpense!!.expense.id, trip.id, TripExpenseType.FUEL, input.title.ifBlank { "Fuel" }, input.amount, input.paidBy, input.splitEnabled, input.selectedPersons, input.odo, input.liters, input.price, input.note, editingExpense!!.expense.createdAt)
                            }
                            editingExpense = null
                            page = TripsPage.Detail
                        }
                    )
                }

                TripsPage.End -> currentTrip?.let { trip ->
                    EndTripScreen(
                        trip = trip,
                        summary = summary,
                        onEnd = { endOdo ->
                            viewModel.endTrip(trip, endOdo)
                            page = TripsPage.Home
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CockpitPalette.TextPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Drive Console • Trip | Fuel | Settlement", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TripsHomeScreen(
    activeTrip: TripEntity?,
    completedTrips: List<TripEntity>,
    fuelLogsCount: Int,
    onStartTrip: () -> Unit,
    onOpenTrip: (TripEntity) -> Unit,
    onAddStandaloneFuel: () -> Unit,
    fuelLogsContent: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tabletLandscape = maxWidth >= 900.dp
        if (!tabletLandscape) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    PremiumPanel {
                        if (activeTrip == null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(36.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("No Active Trip", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Start with ODO and crew", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                PrimaryTripButton("Start", onStartTrip)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(36.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(activeTrip.name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("ODO ${activeTrip.startOdo} • ${formatTripTime(activeTrip.startedAt)}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                PrimaryTripButton("Manage", { onOpenTrip(activeTrip) })
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Trips", completedTrips.size.toString(), Modifier.weight(1f))
                        MetricCard("Fuel", fuelLogsCount.toString(), Modifier.weight(1f))
                        MetricCard("Mode", "Manual", Modifier.weight(1f))
                    }
                }
                item {
                    CockpitSectionHeader("Recent Trips")
                    if (completedTrips.isEmpty()) EmptyPanel("No completed trips", "Completed trips will appear here.")
                }
                itemsIndexed(completedTrips, key = { index, trip -> "completed_${trip.id}_$index" }) { _, trip ->
                    TripHistoryRow(trip = trip, onClick = { onOpenTrip(trip) })
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        CockpitSectionHeader("Fuel Logs")
                        TextButton(onClick = onAddStandaloneFuel) { Text("Add", color = CockpitPalette.Accent) }
                    }
                    PremiumPanel { fuelLogsContent() }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AutomotivePanel(modifier = Modifier.width(104.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(26.dp))
                        Text("TRIP", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        CompactCommandButton("START", onStartTrip, enabled = activeTrip == null)
                        CompactCommandButton("FUEL", onAddStandaloneFuel)
                        if (activeTrip != null) CompactCommandButton("OPEN", { onOpenTrip(activeTrip) })
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AutomotivePanel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CockpitPalette.Accent.copy(alpha = 0.14f))
                                    .border(1.dp, CockpitPalette.Accent.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(30.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    activeTrip?.name ?: "Ready For New Trip",
                                    color = CockpitPalette.TextPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    activeTrip?.let { "ODO ${it.startOdo}  |  ${formatTripTime(it.startedAt)}" } ?: "No active drive session",
                                    color = CockpitPalette.TextMuted,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            PrimaryTripButton(if (activeTrip == null) "Start" else "Manage", { activeTrip?.let(onOpenTrip) ?: onStartTrip() })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatStripItem("TRIPS", completedTrips.size.toString(), Modifier.weight(1f))
                        StatStripItem("FUEL LOGS", fuelLogsCount.toString(), Modifier.weight(1f))
                        StatStripItem("SYSTEM", "MANUAL", Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AutomotivePanel(modifier = Modifier.weight(1f)) {
                            CompactPanelHeader("Recent Trips")
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (completedTrips.isEmpty()) {
                                    EmptyInlineState("No trips recorded")
                                } else {
                                    completedTrips.take(8).forEach { trip ->
                                        CompactTripRow(trip = trip, onClick = { onOpenTrip(trip) })
                                    }
                                }
                            }
                        }
                        AutomotivePanel(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                CompactPanelHeader("Fuel Ledger", Modifier.weight(1f))
                                TextButton(onClick = onAddStandaloneFuel) { Text("ADD", color = CockpitPalette.Accent, fontWeight = FontWeight.Bold) }
                            }
                            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                fuelLogsContent()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartTripScreen(onCreate: (String, Int?, List<String>, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var odo by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(4) }
    var notes by rememberSaveable { mutableStateOf("") }
    var persons by remember { mutableStateOf(List(count) { "Person ${it + 1}" }) }

    LaunchedEffect(count) {
        persons = List(count) { idx -> persons.getOrNull(idx) ?: "Person ${idx + 1}" }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tabletLandscape = maxWidth >= 780.dp
        if (!tabletLandscape) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { TripTextField(name, { name = it }, "Trip name", false) }
                item { TripTextField(odo, { odo = it.filter(Char::isDigit) }, "Start ODO", true) }
                item {
                    PremiumPanel {
                        Text("Number of persons", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                            (1..8).forEach { n -> CockpitControlChip(n.toString(), count == n) { count = n } }
                        }
                    }
                }
                items(persons.indices.toList(), key = { it }) { index ->
                    TripTextField(persons[index], { value -> persons = persons.toMutableList().also { it[index] = value } }, "Person ${index + 1}", false)
                }
                item { TripTextField(notes, { notes = it }, "Notes (optional)", false) }
                item { PrimaryTripButton("Create Trip", { onCreate(name, odo.toIntOrNull(), persons, notes) }, Modifier.fillMaxWidth()) }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AutomotivePanel(modifier = Modifier.width(112.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(26.dp))
                        Text("START", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        CompactCommandButton("CREATE", { onCreate(name, odo.toIntOrNull(), persons, notes) })
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AutomotivePanel {
                        CompactPanelHeader("Trip Setup")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1.45f)) { TripTextField(name, { name = it }, "Trip name", false) }
                            Box(modifier = Modifier.weight(1f)) { TripTextField(odo, { odo = it.filter(Char::isDigit) }, "Start ODO", true) }
                        }
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AutomotivePanel(modifier = Modifier.weight(0.82f)) {
                            CompactPanelHeader("Crew Size")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..8).chunked(4).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        row.forEach { n ->
                                            CompactSelectButton(n.toString(), selected = count == n, modifier = Modifier.weight(1f)) { count = n }
                                        }
                                    }
                                }
                                TripTextField(notes, { notes = it }, "Notes", false)
                            }
                        }
                        AutomotivePanel(modifier = Modifier.weight(1.18f)) {
                            CompactPanelHeader("Passengers")
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                persons.indices.forEach { index ->
                                    CompactIndexedInput(
                                        index = index + 1,
                                        value = persons[index],
                                        onValueChange = { value -> persons = persons.toMutableList().also { it[index] = value } },
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

@Composable
private fun TripDetailScreen(
    trip: TripEntity,
    persons: List<TripPersonEntity>,
    expenses: List<TripExpenseWithShares>,
    summary: TripSummary,
    onAddFuel: () -> Unit,
    onAddExpense: () -> Unit,
    onAddToll: () -> Unit,
    onEndTrip: () -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onEditExpense: (TripExpenseWithShares) -> Unit,
    onAddPerson: (Long, String) -> Unit,
) {
    var newPerson by rememberSaveable { mutableStateOf("") }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tabletLandscape = maxWidth >= 900.dp
        if (!tabletLandscape) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Total", "₹${summary.totalExpense}", Modifier.weight(1f))
                        MetricCard("Fuel", "₹${summary.fuelTotal}", Modifier.weight(1f))
                        MetricCard("Tolls", "₹${summary.tollTotal}", Modifier.weight(1f))
                        MetricCard("KM", summary.totalKm?.toString() ?: "--", Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryTripButton("Add Fuel", onAddFuel)
                        PrimaryTripButton("Add Toll", onAddToll)
                        PrimaryTripButton("Add Expense", onAddExpense)
                        if (trip.status == TripStatus.ONGOING) PrimaryTripButton("End Trip", onEndTrip)
                    }
                }
                item {
                    PremiumPanel {
                        Text("Add Person", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) { TripTextField(newPerson, { newPerson = it }, "Name", false) }
                            PrimaryTripButton("Add", { onAddPerson(trip.id, newPerson); newPerson = "" })
                        }
                    }
                }
                item {
                    CockpitSectionHeader("Contributions")
                    Text(
                        "Net balance per person. Positive = should receive, Negative = should pay.",
                        color = CockpitPalette.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                itemsIndexed(summary.balances, key = { index, balance -> "balance_${balance.person.id}_$index" }) { _, balance ->
                    BalanceRow(balance.person.name, balance.paid, balance.share, balance.balance)
                }
                item { SettlementPanel(summary.settlements) }
                item { CockpitSectionHeader("Expenses") }
                if (expenses.isEmpty()) item { EmptyPanel("No expenses", "Add fuel, tolls, food or other expenses.") }
                itemsIndexed(expenses, key = { index, item -> "expense_${item.expense.id}_$index" }) { _, item ->
                    ExpenseRow(item, persons, onEditExpense, onDeleteExpense)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AutomotivePanel(modifier = Modifier.width(112.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, null, tint = CockpitPalette.Accent, modifier = Modifier.size(26.dp))
                        Text("DRIVE", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        CompactCommandButton("FUEL", onAddFuel)
                        CompactCommandButton("TOLL", onAddToll)
                        CompactCommandButton("COST", onAddExpense)
                        if (trip.status == TripStatus.ONGOING) CompactCommandButton("END", onEndTrip)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AutomotivePanel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trip.name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("ODO ${trip.startOdo}  |  ${formatTripTime(trip.startedAt)}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
                            }
                            StatStripItem("TOTAL", "₹${summary.totalExpense}", Modifier.weight(0.72f))
                            StatStripItem("FUEL", "₹${summary.fuelTotal}", Modifier.weight(0.72f))
                            StatStripItem("KM", summary.totalKm?.toString() ?: "--", Modifier.weight(0.55f))
                        }
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AutomotivePanel(modifier = Modifier.weight(0.82f)) {
                            CompactPanelHeader("Settlement")
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettlementCompactList(summary.settlements)
                                CompactPanelHeader("Passengers")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.weight(1f)) { TripTextField(newPerson, { newPerson = it }, "Name", false) }
                                    PrimaryTripButton("Add", { onAddPerson(trip.id, newPerson); newPerson = "" })
                                }
                                summary.balances.forEach { balance ->
                                    CompactBalanceRow(balance.person.name, balance.paid, balance.share, balance.balance)
                                }
                            }
                        }
                        AutomotivePanel(modifier = Modifier.weight(1.18f)) {
                            CompactPanelHeader("Expense Ledger")
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (expenses.isEmpty()) {
                                    EmptyInlineState("No expenses yet")
                                } else {
                                    expenses.forEach { item ->
                                        CompactExpenseRow(item, persons, onEditExpense, onDeleteExpense)
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

private data class ExpenseInput(
    val type: TripExpenseType,
    val title: String,
    val amount: Int?,
    val paidBy: Long?,
    val splitEnabled: Boolean,
    val selectedPersons: Set<Long>,
    val odo: Int?,
    val liters: Float?,
    val price: Float?,
    val note: String?,
)

@Composable
private fun AddTripExpenseScreen(
    trip: TripEntity,
    persons: List<TripPersonEntity>,
    initialType: TripExpenseType,
    editing: TripExpenseWithShares?,
    fuelMode: Boolean,
    onSave: (ExpenseInput) -> Unit,
) {
    var type by remember(editing) { mutableStateOf(editing?.expense?.type?.let { runCatching { TripExpenseType.valueOf(it) }.getOrNull() } ?: initialType) }
    var title by remember(editing) { mutableStateOf(editing?.expense?.title ?: if (fuelMode) "Fuel" else "") }
    var amount by remember(editing) { mutableStateOf(editing?.expense?.amount?.toString() ?: "") }
    var odo by remember(editing) { mutableStateOf(editing?.expense?.manualOdo?.toString() ?: "") }
    var liters by remember(editing) { mutableStateOf(editing?.expense?.fuelLiters?.toString() ?: "") }
    var price by remember(editing) { mutableStateOf(editing?.expense?.fuelPrice?.toString() ?: "") }
    var note by remember(editing) { mutableStateOf(editing?.expense?.note ?: "") }
    var paidBy by rememberSaveable(editing?.expense?.id, trip.id) { mutableStateOf(editing?.expense?.paidByPersonId) }
    var splitEnabled by rememberSaveable(editing?.expense?.id, trip.id) { mutableStateOf(editing?.expense?.splitEnabled ?: true) }
    var selectedPersons by rememberSaveable(editing?.expense?.id, trip.id) { mutableStateOf(emptySet<Long>()) }

    LaunchedEffect(editing?.expense?.id, trip.id, persons.map { it.id }) {
        if (paidBy == null) paidBy = persons.firstOrNull()?.id
        if (selectedPersons.isEmpty()) {
            selectedPersons = editing?.shares
                ?.filter { it.includedInSplit }
                ?.map { it.personId }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
                ?: persons.filter { it.active }.map { it.id }.toSet()
        } else {
            selectedPersons = selectedPersons.intersect(persons.map { it.id }.toSet())
        }
    }

    fun syncFuel(changed: String) {
        val amountVal = amount.toFloatOrNull()
        val priceVal = price.toFloatOrNull()
        val litersVal = liters.toFloatOrNull()
        when (changed) {
            "amount", "price" -> if (amountVal != null && priceVal != null && priceVal > 0f) liters = formatDecimal(amountVal / priceVal)
            "liters" -> if (litersVal != null && priceVal != null && priceVal > 0f) amount = (litersVal * priceVal).roundToInt().toString()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tabletLandscape = maxWidth >= 780.dp
        if (!tabletLandscape) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (!fuelMode) {
                    item {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TripExpenseType.values().forEach { item -> CockpitControlChip(item.label, type == item) { type = item; if (title.isBlank()) title = item.label } }
                        }
                    }
                }
                item { TripTextField(title, { title = it }, "Title", false) }
                item {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(40, 100, 200, 500, 1000, 2000).forEach { preset -> CockpitControlChip("₹$preset", amount == preset.toString()) { amount = preset.toString(); syncFuel("amount") } }
                    }
                }
                item { TripTextField(amount, { amount = it.filter(Char::isDigit); syncFuel("amount") }, "Amount ₹", true) }
                if (fuelMode) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) { TripTextField(price, { price = decimalOnly(it); syncFuel("price") }, "Price/L", true) }
                            Box(Modifier.weight(1f)) { TripTextField(liters, { liters = decimalOnly(it); syncFuel("liters") }, "Liters", true) }
                        }
                    }
                }
                item { TripTextField(odo, { odo = it.filter(Char::isDigit) }, "ODO (optional)", true) }
                item {
                    PremiumPanel {
                        Text("Paid by", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            persons.forEach { person -> CockpitControlChip(person.name, paidBy == person.id) { paidBy = person.id } }
                        }
                    }
                }
                item {
                    PremiumPanel {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("Split this expense", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.SemiBold); Text("Turn off for personal/non-shared cost", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall) }
                            Switch(splitEnabled, { splitEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = CockpitPalette.Accent))
                        }
                        if (splitEnabled) {
                            persons.forEach { person ->
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = person.id in selectedPersons,
                                        role = Role.Checkbox
                                    ) {
                                    selectedPersons = if (person.id in selectedPersons) selectedPersons - person.id else selectedPersons + person.id
                                }, verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = person.id in selectedPersons,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = CockpitPalette.Accent)
                                    )
                                    Text(person.name, color = CockpitPalette.TextPrimary)
                                }
                            }
                        }
                    }
                }
                item { TripTextField(note, { note = it }, "Note (optional)", false) }
                item {
                    PrimaryTripButton(
                        label = if (editing == null) "Save" else "Update",
                        onClick = { onSave(ExpenseInput(type, title, amount.toIntOrNull(), paidBy, splitEnabled, selectedPersons, odo.toIntOrNull(), liters.toFloatOrNull(), price.toFloatOrNull(), note)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AutomotivePanel(modifier = Modifier.width(112.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (fuelMode) Icons.Default.LocalGasStation else Icons.Default.Payments, null, tint = CockpitPalette.Accent, modifier = Modifier.size(26.dp))
                        Text(if (fuelMode) "FUEL" else "COST", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        CompactCommandButton(if (editing == null) "SAVE" else "UPDATE", onClick = {
                            onSave(ExpenseInput(type, title, amount.toIntOrNull(), paidBy, splitEnabled, selectedPersons, odo.toIntOrNull(), liters.toFloatOrNull(), price.toFloatOrNull(), note))
                        })
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AutomotivePanel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (editing == null) "New ${if (fuelMode) "Fuel" else "Expense"}" else "Edit ${if (fuelMode) "Fuel" else "Expense"}", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                Text(trip.name, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            StatStripItem("AMOUNT", amount.takeIf { it.isNotBlank() }?.let { "₹$it" } ?: "--", Modifier.weight(0.7f))
                            StatStripItem("SPLIT", if (splitEnabled) selectedPersons.size.toString() else "OFF", Modifier.weight(0.5f))
                        }
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AutomotivePanel(modifier = Modifier.weight(1f)) {
                            CompactPanelHeader("Expense Data")
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!fuelMode) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        TripExpenseType.values().take(3).forEach { item ->
                                            CompactSelectButton(item.label, selected = type == item, modifier = Modifier.weight(1f)) {
                                                type = item
                                                if (title.isBlank()) title = item.label
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        TripExpenseType.values().drop(3).forEach { item ->
                                            CompactSelectButton(item.label, selected = type == item, modifier = Modifier.weight(1f)) {
                                                type = item
                                                if (title.isBlank()) title = item.label
                                            }
                                        }
                                    }
                                }
                                TripTextField(title, { title = it }, "Title", false)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf(100, 200, 500).forEach { preset ->
                                        CompactSelectButton("₹$preset", selected = amount == preset.toString(), modifier = Modifier.weight(1f)) {
                                            amount = preset.toString()
                                            syncFuel("amount")
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf(1000, 2000, 5000).forEach { preset ->
                                        CompactSelectButton("₹$preset", selected = amount == preset.toString(), modifier = Modifier.weight(1f)) {
                                            amount = preset.toString()
                                            syncFuel("amount")
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Box(Modifier.weight(1f)) { TripTextField(amount, { amount = it.filter(Char::isDigit); syncFuel("amount") }, "Amount", true) }
                                    Box(Modifier.weight(1f)) { TripTextField(odo, { odo = it.filter(Char::isDigit) }, "ODO", true) }
                                }
                                if (fuelMode) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Box(Modifier.weight(1f)) { TripTextField(price, { price = decimalOnly(it); syncFuel("price") }, "Price/L", true) }
                                        Box(Modifier.weight(1f)) { TripTextField(liters, { liters = decimalOnly(it); syncFuel("liters") }, "Liters", true) }
                                    }
                                }
                                TripTextField(note, { note = it }, "Note", false)
                            }
                        }
                        AutomotivePanel(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                CompactPanelHeader("Share Control", Modifier.weight(1f))
                                Switch(splitEnabled, { splitEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = CockpitPalette.Accent))
                            }
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                CompactPanelHeader("Paid By")
                                persons.forEach { person ->
                                    CompactPersonChoice(
                                        name = person.name,
                                        selected = paidBy == person.id,
                                        trailing = if (paidBy == person.id) "PAYER" else "",
                                        onClick = { paidBy = person.id }
                                    )
                                }
                                if (splitEnabled) {
                                    CompactPanelHeader("Included In Split")
                                    persons.forEach { person ->
                                        CompactPersonChoice(
                                            name = person.name,
                                            selected = person.id in selectedPersons,
                                            trailing = if (person.id in selectedPersons) "IN" else "OUT",
                                            onClick = { selectedPersons = if (person.id in selectedPersons) selectedPersons - person.id else selectedPersons + person.id }
                                        )
                                    }
                                } else {
                                    EmptyInlineState("Personal cost, no split")
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
private fun EndTripScreen(trip: TripEntity, summary: TripSummary, onEnd: (Int?) -> Unit) {
    var endOdo by rememberSaveable { mutableStateOf(trip.endOdo?.toString() ?: "") }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            PremiumPanel {
                Text(trip.name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Start ODO ${trip.startOdo}", color = CockpitPalette.TextMuted)
                Text("Trip spend ₹${summary.totalExpense}", color = CockpitPalette.TextMuted)
            }
        }
        item { TripTextField(endOdo, { endOdo = it.filter(Char::isDigit) }, "End ODO", true) }
        item {
            val km = endOdo.toIntOrNull()?.minus(trip.startOdo)
            MetricCard("Total KM", if (km != null && km >= 0) km.toString() else "--", Modifier.fillMaxWidth())
        }
        item { PrimaryTripButton("Complete Trip", { onEnd(endOdo.toIntOrNull()) }, Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun AutomotivePanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1D2428).copy(alpha = 0.98f),
                        Color(0xFF101417).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.14f), CockpitPalette.Border.copy(alpha = 0.28f))
                ),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) { content() }
}

@Composable
private fun CompactCommandButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(38.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CockpitPalette.Accent.copy(alpha = 0.90f),
            contentColor = CockpitPalette.OnAccent,
            disabledContainerColor = CockpitPalette.SurfaceRaised.copy(alpha = 0.45f),
            disabledContentColor = CockpitPalette.TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun CompactSelectButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) CockpitPalette.Accent.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.055f),
            contentColor = if (selected) CockpitPalette.OnAccent else CockpitPalette.TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CompactIndexedInput(
    index: Int,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, CockpitPalette.Border.copy(alpha = 0.22f), RoundedCornerShape(7.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(index.toString().padStart(2, '0'), color = CockpitPalette.Accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
        Box(modifier = Modifier.weight(1f)) {
            TripTextField(value, onValueChange, "Passenger", false)
        }
    }
}

@Composable
private fun CompactPersonChoice(
    name: String,
    selected: Boolean,
    trailing: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .background(if (selected) CockpitPalette.Accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                if (selected) CockpitPalette.Accent.copy(alpha = 0.34f) else CockpitPalette.Border.copy(alpha = 0.22f),
                RoundedCornerShape(7.dp)
            )
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) CockpitPalette.Accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), color = if (selected) CockpitPalette.Accent else CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
        }
        Text(name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailing.isNotBlank()) {
            Text(trailing, color = if (selected) CockpitPalette.Accent else CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun StatStripItem(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF0F1416).copy(alpha = 0.86f))
            .border(1.dp, CockpitPalette.Border.copy(alpha = 0.45f), RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(label, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(value, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun CompactPanelHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(18.dp).background(CockpitPalette.Accent, RoundedCornerShape(2.dp)))
        Text(title.uppercase(), color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyInlineState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.035f)),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompactTripRow(trip: TripEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, CockpitPalette.Border.copy(alpha = 0.28f), RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(trip.name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatTripTime(trip.startedAt)}  |  ${trip.startOdo}-${trip.endOdo ?: "--"} ODO", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Text("${trip.endOdo?.minus(trip.startOdo) ?: 0} km", color = CockpitPalette.Accent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SettlementCompactList(settlements: List<TripSettlementHint>) {
    if (settlements.isEmpty()) {
        EmptyInlineState("All balances settled")
    } else {
        settlements.forEach { hint ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(CockpitPalette.Accent.copy(alpha = 0.10f))
                    .border(1.dp, CockpitPalette.Accent.copy(alpha = 0.26f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${hint.fromName} -> ${hint.toName}", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("₹${hint.amount}", color = CockpitPalette.Accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun CompactBalanceRow(name: String, paid: Int, share: Double, balance: Double) {
    val amount = balance.let { if (it < 0) -it else it }.roundToInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("P ₹$paid", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
        Text("S ₹${share.roundToInt()}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
        Text(
            if (balance >= 0) "+₹$amount" else "-₹$amount",
            color = if (balance >= 0) CockpitPalette.Success else CockpitPalette.Danger,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun CompactExpenseRow(
    item: TripExpenseWithShares,
    persons: List<TripPersonEntity>,
    onEdit: (TripExpenseWithShares) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val expense = item.expense
    val paidBy = persons.firstOrNull { it.id == expense.paidByPersonId }?.name ?: "Unknown"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, CockpitPalette.Border.copy(alpha = 0.24f), RoundedCornerShape(7.dp))
            .padding(start = 9.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(CockpitPalette.Accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (expense.type == TripExpenseType.FUEL.name) Icons.Default.LocalGasStation else Icons.Default.Payments, null, tint = CockpitPalette.Accent, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.title, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${typeLabel(expense.type)}  |  $paidBy  |  ${if (expense.splitEnabled) "Split ${item.shares.size}" else "Solo"}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("₹${expense.amount}", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, null, tint = CockpitPalette.TextSecondary, modifier = Modifier.size(18.dp)) }
        IconButton(onClick = { onDelete(expense.id) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, null, tint = CockpitPalette.Danger, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun PremiumPanel(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = CockpitPalette.Accent.copy(alpha = 0.10f), spotColor = CockpitPalette.Accent.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CockpitPalette.SurfaceTop.copy(alpha = 0.96f),
                        CockpitPalette.SurfaceBottom.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        CockpitPalette.Border.copy(alpha = 0.90f),
                        CockpitPalette.Border.copy(alpha = 0.35f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) { content() }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CockpitPalette.SurfaceRaised.copy(alpha = 0.96f),
                        CockpitPalette.SurfaceBottom.copy(alpha = 0.90f)
                    )
                )
            )
            .border(1.dp, CockpitPalette.Border.copy(alpha = 0.78f), RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column {
            Text(label.uppercase(), color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Text(value, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun PrimaryTripButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(11.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CockpitPalette.Accent, contentColor = CockpitPalette.OnAccent)
    ) { Text(label.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
}

@Composable
private fun TripTextField(value: String, onValueChange: (String) -> Unit, label: String, number: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = CockpitPalette.TextPrimary,
            unfocusedTextColor = CockpitPalette.TextPrimary,
            focusedBorderColor = CockpitPalette.Accent,
            unfocusedBorderColor = CockpitPalette.Border,
            focusedLabelColor = CockpitPalette.Accent,
            unfocusedLabelColor = CockpitPalette.TextMuted,
            cursorColor = CockpitPalette.Accent,
        )
    )
}

@Composable
private fun EmptyPanel(title: String, message: String) {
    PremiumPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = CockpitPalette.TextPrimary, fontWeight = FontWeight.Bold)
            Text(message, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TripHistoryRow(trip: TripEntity, onClick: () -> Unit) {
    PremiumPanel {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.name, color = CockpitPalette.TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatTripTime(trip.startedAt)} • ${trip.startOdo} to ${trip.endOdo ?: "--"} ODO", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
            }
            Text("${trip.endOdo?.minus(trip.startOdo) ?: 0} km", color = CockpitPalette.Accent, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun BalanceRow(name: String, paid: Int, share: Double, balance: Double) {
    val absoluteBalance = balance.let { if (it < 0) -it else it }.roundToInt()
    val statusText = if (balance >= 0) "Should receive ₹$absoluteBalance" else "Should pay ₹$absoluteBalance"
    PremiumPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CockpitPalette.Accent.copy(alpha = 0.20f))
                    .border(1.dp, CockpitPalette.Accent.copy(alpha = 0.38f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = CockpitPalette.Accent, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = CockpitPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Paid ₹$paid • Share ₹${share.roundToInt()}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
                Text(statusText, color = if (balance >= 0) CockpitPalette.Success else CockpitPalette.Danger, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Text(if (balance >= 0) "+₹${balance.roundToInt()}" else "-₹${(-balance).roundToInt()}", color = if (balance >= 0) CockpitPalette.Success else CockpitPalette.Danger, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettlementPanel(settlements: List<TripSettlementHint>) {
    PremiumPanel {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Who Pays Whom", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.Bold)
            if (settlements.isEmpty()) Text("All balanced or no shared expenses yet.", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
            settlements.forEachIndexed { index, hint ->
                Text("${hint.fromName} gives ₹${hint.amount} to ${hint.toName}", color = CockpitPalette.TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (index != settlements.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CockpitPalette.Border.copy(alpha = 0.45f)))
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    item: TripExpenseWithShares,
    persons: List<TripPersonEntity>,
    onEdit: (TripExpenseWithShares) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val expense = item.expense
    val paidBy = persons.firstOrNull { it.id == expense.paidByPersonId }?.name ?: "Unknown"
    PremiumPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CockpitPalette.Accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (expense.type == TripExpenseType.FUEL.name) Icons.Default.LocalGasStation else Icons.Default.Payments, null, tint = CockpitPalette.Accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, color = CockpitPalette.TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${typeLabel(expense.type)} • Paid by $paidBy • ${if (expense.splitEnabled) "Split ${item.shares.size}" else "No split"}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
                expense.manualOdo?.let { Text("ODO $it", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall) }
            }
            Text("₹${expense.amount}", color = CockpitPalette.TextPrimary, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = { onEdit(item) }) { Icon(Icons.Default.Edit, null, tint = CockpitPalette.TextSecondary) }
            IconButton(onClick = { onDelete(expense.id) }) { Icon(Icons.Default.Delete, null, tint = CockpitPalette.Danger) }
        }
    }
}

private fun formatTripTime(ms: Long): String = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(ms))
private fun typeLabel(raw: String): String = runCatching { TripExpenseType.valueOf(raw).label }.getOrDefault(raw)
private fun decimalOnly(raw: String): String = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && raw.indexOf('.') == index) }
private fun formatDecimal(value: Float): String = "%.2f".format(Locale.US, value).trimEnd('0').trimEnd('.')
