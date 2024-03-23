package com.example.justin_yan_myruns5

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

// Shows listview from Database values
// Referenced from XD's in class code
class MyListAdapter(private val context: Context, private var entryList: List<Entry>) : BaseAdapter(){

    override fun getItem(position: Int): Any {
        return entryList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return entryList.size
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter,null)

        // For the 2 lines for each entry in the list (according to Example APK)
        val entryLabel = view.findViewById(R.id.tv_label) as TextView
        val entryDetails = view.findViewById(R.id.tv_details) as TextView

        val inputType =
            when (entryList[position].input) {
                0 -> "Manual Entry"
                1 -> "GPS"
                2 -> "Automatic"
                else -> "Missing"
            }

        val activityType =
            when (entryList[position].activity) {
                0 -> "Running"
                1 -> "Walking"
                2 -> "Standing"
                3 -> "Cycling"
                4 -> "Hiking"
                5 -> "Downhill Skiing"
                6 -> "Cross-Country Skiing"
                7 -> "Snowboarding"
                8 -> "Skating"
                9 -> "Swimming"
                10 -> "Mountain Biking"
                11 -> "Wheelchair"
                12 -> "Elliptical"
                13 -> "Other"
                else -> "Missing"
            }

        // Reference: https://stackoverflow.com/questions/58505052/returning-calendar-with-simpledateformat
        val calendarFormat = SimpleDateFormat("HH:mm:ss yyyy-MM-dd")
        val dateTime: String = calendarFormat.format(entryList[position].dateTime.time)

        // Puts together first line with activity type and date & time values
        entryLabel.text = "$inputType : $activityType , $dateTime"

        // Gets metric type selected
        var sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val unitInUse = sharedPreferences.getString("unit_preference", "0")

        val distance = if (unitInUse == "Metric (Kilometers)") {
            (entryList[position].distance * 1.60934).toString() + " Kilometers"
        } else {
            entryList[position].distance.toString() + " Miles"
        }

        val minutes = entryList[position].duration.toInt()
        val seconds = (minutes.toDouble() - entryList[position].duration).toInt()
        val duration = if (entryList[position].duration.toInt() == 0) {
            "$seconds secs"
        } else {
            "$minutes mins $seconds secs"
        }

        // Forms 2nd line
        entryDetails.text = "$distance , $duration"

        // If list item is clicked, then store position and open details page for that entry
        view.setOnClickListener {
            val intent = if (inputType == "Manual Entry") {
                Intent (context, EntryDetails::class.java)
            } else {
                Intent (context, MapDetails::class.java)
            }

            // Reference: https://stackoverflow.com/questions/45157567/how-to-pass-the-values-from-activity-to-another-activity
            intent.putExtra("entryID", position)
            context.startActivity(intent)
        }

        return view
    }

    fun replace(newCommentList: List<Entry>){
        entryList = newCommentList
    }

}