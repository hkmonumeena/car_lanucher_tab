package com.ruchitech.carlanuchertab.roomdb.action

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ruchitech.carlanuchertab.WidgetItem

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWidgetItemList(value: List<WidgetItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWidgetItemList(value: String): List<WidgetItem> {
        val listType = object : TypeToken<List<WidgetItem>>() {}.type
        return gson.fromJson(value, listType)
    }
}
