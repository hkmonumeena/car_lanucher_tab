package com.ruchitech.carlanuchertab.roomdb.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String = getCurrentDate(),       // e.g., 28-06-2025
    val time: String = getCurrentTime(),       // e.g., 22:47
    val loggedAtEpochMs: Long = 0L,            // canonical sort / month stats; migration backfills
    val location: String? = null,              // optional address/location
    val liters: Float? = null,                 // optional fuel quantity
    val rupee: Int,                            // mandatory amount spent
    val fuelPrice: Float? = null               // optional price per liter
)

/** Last saved fill for quick pre-fill in [com.ruchitech.carlanuchertab.ui.composables.FuelLogDialog]. */
data class FuelQuickFillHints(
    val lastPricePerLiter: Float?,
    val lastLiters: Float?,
)

// 🔧 Utility functions
fun getCurrentDate(): String {
    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return format.format(Date())
}

fun getCurrentTime(): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date())
}

/** Count and total ₹ for logs whose [FuelLog.loggedAtEpochMs] falls in the current calendar month (device zone). */
fun fuelMonthSummaryNow(logs: List<FuelLog>): Pair<Int, Int> {
    val zone = ZoneId.systemDefault()
    val ym = YearMonth.now(zone)
    val monthStart = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val monthEnd = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val inMonth = logs.filter { it.loggedAtEpochMs >= monthStart && it.loggedAtEpochMs < monthEnd }
    return inMonth.size to inMonth.sumOf { it.rupee }
}
