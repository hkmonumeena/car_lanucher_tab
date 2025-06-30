package com.ruchitech.carlanuchertab.roomdb.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem



@Entity(tableName = "dashboard")
data class Dashboard(
    @PrimaryKey(autoGenerate = false) val id: Int = 1,
     val wallpaperId: Int = R.drawable.launcher_bg7,
    val isSnowfall: Boolean = false,
   val widgets: List<WidgetItem> = emptyList()
)
