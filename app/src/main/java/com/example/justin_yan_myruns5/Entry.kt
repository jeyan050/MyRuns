package com.example.justin_yan_myruns5

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

@Entity(tableName = "entry_table")
@TypeConverters(Converters::class)

data class Entry (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var input: Int = 0,

    @ColumnInfo(name = "activity_type")
    var activity: Int = 0,

    @ColumnInfo(name = "dateTime")
    var dateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    @ColumnInfo(name = "avgPace")
    var pace: Double = 0.0,

    @ColumnInfo(name = "avgSpeed")
    var speed: Double = 0.0,

    @ColumnInfo(name = "calorie")
    var calories: Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String = "",

    @ColumnInfo(name = "location")
    var locations: ArrayList<LatLng>? = arrayListOf()
)