package com.ruchitech.carlanuchertab.roomdb.dao

import androidx.room.*
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    @Query("SELECT * FROM dashboard WHERE id = 1")
    suspend fun getDashboard(): Dashboard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDashboard(dashboard: Dashboard)

    // 🔄 Update widget list in Dashboard
    @Update
    suspend fun updateDashboard(dashboard: Dashboard)

    // 🗑 Delete entire dashboard (if ever needed)
    @Delete
    suspend fun deleteDashboard(dashboard: Dashboard)

    // 🧹 Clear all widgets (set empty list)
    @Query("UPDATE dashboard SET widgets = :emptyJson WHERE id = 1")
    suspend fun clearWidgets(emptyJson: String = "[]") // manually provide empty JSON




    // 🔍 Read All (newest first by wall-clock instant, then id)
    @Query("SELECT * FROM fuel_logs ORDER BY loggedAtEpochMs DESC, id DESC")
    suspend fun getAllLogs(): List<FuelLog>

    @Query("SELECT * FROM fuel_logs ORDER BY loggedAtEpochMs DESC, id DESC")
    fun observeFuelLogs(): Flow<List<FuelLog>>

    // 🔍 Read by ID
    @Query("SELECT * FROM fuel_logs WHERE id = :id")
    suspend fun getLogById(id: Int): FuelLog?

    // ➕ Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLog)

    // 📝 Update
    @Update
    suspend fun updateLog(log: FuelLog)

    // 🗑 Delete by object
    @Delete
    suspend fun deleteLog(log: FuelLog)

    // 🗑 Delete by ID
    @Query("DELETE FROM fuel_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    // 🧹 Delete all
    @Query("DELETE FROM fuel_logs")
    suspend fun clearAllLogs()


}
