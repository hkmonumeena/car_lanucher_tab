package com.ruchitech.carlanuchertab.roomdb.action
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog

@Database(entities = [Dashboard::class,FuelLog::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_launcher_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
