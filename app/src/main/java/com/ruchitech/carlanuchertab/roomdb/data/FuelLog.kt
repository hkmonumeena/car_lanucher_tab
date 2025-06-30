package com.ruchitech.carlanuchertab.roomdb.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String = getCurrentDate(),       // e.g., 28-06-2025
    val time: String = getCurrentTime(),       // e.g., 22:47
    val location: String? = null,              // optional address/location
    val liters: Float? = null,                 // optional fuel quantity
    val rupee: Int,                            // mandatory amount spent
    val fuelPrice: Float? = null               // optional price per liter
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
