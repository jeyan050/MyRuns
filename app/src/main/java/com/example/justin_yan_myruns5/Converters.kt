package com.example.justin_yan_myruns5

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Calendar

// Reference: https://stackoverflow.com/questions/50313525/room-using-date-field
object Converters {
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }

    @TypeConverter
    fun toArrayList(json: String): ArrayList<LatLng>?
    {
        val listType: Type = object : TypeToken<ArrayList<LatLng>>() {}.type
        return Gson().fromJson(json, listType)


    }

    @TypeConverter
    fun fromArrayList(array: ArrayList<LatLng>?): String
    {
        return Gson().toJson(array)
    }
}