package com.ruchitech.carlanuchertab.trip

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object TripStatus {
    const val ONGOING = "ONGOING"
    const val COMPLETED = "COMPLETED"
}

enum class TripExpenseType(val label: String) {
    FUEL("Fuel"),
    TOLL("Toll"),
    FOOD("Food"),
    PARKING("Parking"),
    STAY("Stay"),
    OTHER("Other"),
}

@Entity(
    tableName = "trips",
    indices = [Index(value = ["status"]), Index(value = ["startedAt"])]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val startOdo: Int,
    val endOdo: Int? = null,
    val status: String = TripStatus.ONGOING,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val notes: String? = null,
)

@Entity(
    tableName = "trip_persons",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"]), Index(value = ["tripId", "name"])]
)
data class TripPersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tripId: Long,
    val name: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "trip_expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["paidByPersonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"]), Index(value = ["paidByPersonId"]), Index(value = ["type"])]
)
data class TripExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tripId: Long,
    val type: String,
    val title: String,
    val amount: Int,
    val paidByPersonId: Long,
    val splitEnabled: Boolean = true,
    val manualOdo: Int? = null,
    val fuelLiters: Float? = null,
    val fuelPrice: Float? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "trip_expense_shares",
    primaryKeys = ["expenseId", "personId"],
    foreignKeys = [
        ForeignKey(
            entity = TripExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class TripExpenseShareEntity(
    val expenseId: Long,
    val personId: Long,
    val includedInSplit: Boolean = true,
)

data class TripPersonBalance(
    val person: TripPersonEntity,
    val paid: Int,
    val share: Double,
    val balance: Double,
)

data class TripSettlementHint(
    val fromName: String,
    val toName: String,
    val amount: Int,
)

data class TripSummary(
    val totalExpense: Int = 0,
    val fuelTotal: Int = 0,
    val tollTotal: Int = 0,
    val foodTotal: Int = 0,
    val otherTotal: Int = 0,
    val totalKm: Int? = null,
    val balances: List<TripPersonBalance> = emptyList(),
    val settlements: List<TripSettlementHint> = emptyList(),
)
