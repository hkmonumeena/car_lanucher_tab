package com.ruchitech.carlanuchertab.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripDao: TripDao,
    private val dashboardDao: DashboardDao,
) : ViewModel() {
    val activeTrip: StateFlow<TripEntity?> = tripDao.observeActiveTrip()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val completedTrips: StateFlow<List<TripEntity>> = tripDao.observeCompletedTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fuelLogs: StateFlow<List<FuelLog>> = dashboardDao.observeFuelLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    fun observeTrip(tripId: Long): Flow<TripEntity?> = tripDao.observeTrip(tripId)
    fun observePersons(tripId: Long): Flow<List<TripPersonEntity>> = tripDao.observePersons(tripId)
    fun observeExpenses(tripId: Long): Flow<List<TripExpenseWithShares>> = tripDao.observeExpensesWithShares(tripId)

    fun observeSummary(tripId: Long): Flow<TripSummary> {
        return combine(
            tripDao.observeTrip(tripId),
            tripDao.observePersons(tripId),
            tripDao.observeExpensesWithShares(tripId),
        ) { trip, persons, expenses ->
            buildTripSummary(trip, persons, expenses)
        }
    }

    fun createTrip(name: String, startOdo: Int?, personNames: List<String>, notes: String?) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            val cleanPersons = personNames.map { it.trim() }.filter { it.isNotBlank() }
            when {
                activeTrip.value != null -> _messages.emit("End the active trip before starting another.")
                trimmedName.isBlank() -> _messages.emit("Trip name is required.")
                startOdo == null || startOdo < 0 -> _messages.emit("Enter a valid start ODO.")
                cleanPersons.isEmpty() -> _messages.emit("Add at least one person.")
                else -> tripDao.createTrip(trimmedName, startOdo, cleanPersons, notes?.takeIf { it.isNotBlank() })
            }
        }
    }

    fun addPerson(tripId: Long, name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isBlank()) {
                _messages.emit("Person name is required.")
                return@launch
            }
            tripDao.insertPerson(TripPersonEntity(tripId = tripId, name = trimmed))
        }
    }

    fun addExpense(
        tripId: Long,
        type: TripExpenseType,
        title: String,
        amount: Int?,
        paidByPersonId: Long?,
        splitEnabled: Boolean,
        selectedPersonIds: Set<Long>,
        manualOdo: Int?,
        fuelLiters: Float?,
        fuelPrice: Float?,
        note: String?,
    ) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifBlank { type.label }
            when {
                amount == null || amount <= 0 -> _messages.emit("Enter a valid amount.")
                paidByPersonId == null -> _messages.emit("Select who paid.")
                splitEnabled && selectedPersonIds.isEmpty() -> _messages.emit("Select at least one person to split.")
                else -> tripDao.addExpenseWithShares(
                    TripExpenseEntity(
                        tripId = tripId,
                        type = type.name,
                        title = cleanTitle,
                        amount = amount,
                        paidByPersonId = paidByPersonId,
                        splitEnabled = splitEnabled,
                        manualOdo = manualOdo?.takeIf { it >= 0 },
                        fuelLiters = fuelLiters?.takeIf { it > 0f },
                        fuelPrice = fuelPrice?.takeIf { it > 0f },
                        note = note?.trim()?.takeIf { it.isNotBlank() },
                    ),
                    selectedPersonIds
                )
            }
        }
    }

    fun updateExpense(
        expenseId: Long,
        tripId: Long,
        type: TripExpenseType,
        title: String,
        amount: Int?,
        paidByPersonId: Long?,
        splitEnabled: Boolean,
        selectedPersonIds: Set<Long>,
        manualOdo: Int?,
        fuelLiters: Float?,
        fuelPrice: Float?,
        note: String?,
        createdAt: Long,
    ) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifBlank { type.label }
            when {
                amount == null || amount <= 0 -> _messages.emit("Enter a valid amount.")
                paidByPersonId == null -> _messages.emit("Select who paid.")
                splitEnabled && selectedPersonIds.isEmpty() -> _messages.emit("Select at least one person to split.")
                else -> tripDao.replaceExpenseWithShares(
                    TripExpenseEntity(
                        id = expenseId,
                        tripId = tripId,
                        type = type.name,
                        title = cleanTitle,
                        amount = amount,
                        paidByPersonId = paidByPersonId,
                        splitEnabled = splitEnabled,
                        manualOdo = manualOdo?.takeIf { it >= 0 },
                        fuelLiters = fuelLiters?.takeIf { it > 0f },
                        fuelPrice = fuelPrice?.takeIf { it > 0f },
                        note = note?.trim()?.takeIf { it.isNotBlank() },
                        createdAt = createdAt,
                    ),
                    selectedPersonIds
                )
            }
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch { tripDao.deleteExpense(expenseId) }
    }

    fun insertStandaloneFuelLog(log: FuelLog) {
        viewModelScope.launch { dashboardDao.insertLog(log) }
    }

    fun deleteStandaloneFuelLog(log: FuelLog) {
        viewModelScope.launch { dashboardDao.deleteLog(log) }
    }

    fun endTrip(trip: TripEntity, endOdo: Int?) {
        viewModelScope.launch {
            when {
                endOdo == null -> _messages.emit("Enter end ODO.")
                endOdo < trip.startOdo -> _messages.emit("End ODO cannot be lower than start ODO.")
                else -> tripDao.updateTrip(
                    trip.copy(
                        endOdo = endOdo,
                        status = TripStatus.COMPLETED,
                        endedAt = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    private fun buildTripSummary(
        trip: TripEntity?,
        persons: List<TripPersonEntity>,
        expenses: List<TripExpenseWithShares>,
    ): TripSummary {
        if (trip == null) return TripSummary()
        val total = expenses.sumOf { it.expense.amount }
        val fuel = expenses.filter { it.expense.type == TripExpenseType.FUEL.name }.sumOf { it.expense.amount }
        val toll = expenses.filter { it.expense.type == TripExpenseType.TOLL.name }.sumOf { it.expense.amount }
        val food = expenses.filter { it.expense.type == TripExpenseType.FOOD.name }.sumOf { it.expense.amount }
        val other = total - fuel - toll - food
        val paidBy = expenses.groupBy { it.expense.paidByPersonId }.mapValues { entry -> entry.value.sumOf { it.expense.amount } }
        val shares = mutableMapOf<Long, Double>()
        expenses.forEach { item ->
            if (!item.expense.splitEnabled) return@forEach
            val selected = item.shares.filter { it.includedInSplit }.map { it.personId }
            if (selected.isEmpty()) return@forEach
            val perHead = item.expense.amount.toDouble() / selected.size.toDouble()
            selected.forEach { personId -> shares[personId] = (shares[personId] ?: 0.0) + perHead }
        }
        val balances = persons.map { person ->
            val paid = paidBy[person.id] ?: 0
            val share = shares[person.id] ?: 0.0
            TripPersonBalance(person, paid, share, paid - share)
        }
        return TripSummary(
            totalExpense = total,
            fuelTotal = fuel,
            tollTotal = toll,
            foodTotal = food,
            otherTotal = other,
            totalKm = trip.endOdo?.minus(trip.startOdo),
            balances = balances,
            settlements = buildSettlementHints(balances)
        )
    }

    private fun buildSettlementHints(balances: List<TripPersonBalance>): List<TripSettlementHint> {
        val debtors = balances.filter { it.balance < -0.5 }.map { it.person.name to -it.balance }.toMutableList()
        val creditors = balances.filter { it.balance > 0.5 }.map { it.person.name to it.balance }.toMutableList()
        val hints = mutableListOf<TripSettlementHint>()
        var d = 0
        var c = 0
        while (d < debtors.size && c < creditors.size) {
            val debtor = debtors[d]
            val creditor = creditors[c]
            val amount = minOf(debtor.second, creditor.second)
            if (amount >= 1.0) hints += TripSettlementHint(debtor.first, creditor.first, amount.toInt())
            debtors[d] = debtor.first to (debtor.second - amount)
            creditors[c] = creditor.first to (creditor.second - amount)
            if (debtors[d].second <= 0.5) d++
            if (creditors[c].second <= 0.5) c++
        }
        return hints
    }
}
