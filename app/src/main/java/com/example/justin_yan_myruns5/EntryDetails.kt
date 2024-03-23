package com.example.justin_yan_myruns5

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import kotlin.math.round

class EntryDetails : AppCompatActivity() {

    private lateinit var database: EntryDatabase
    private lateinit var databaseDao: EntryDatabaseDao
    private lateinit var repository: EntryRepository
    private lateinit var viewModelFactory: EntryViewModelFactory
    private lateinit var entryViewModel: EntryViewModel

    private var isDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entry_details)

        // Get Database Entry values
        database = EntryDatabase.getInstance(this)
        databaseDao = database.entryDatabaseDao
        repository = EntryRepository(databaseDao)
        viewModelFactory = EntryViewModelFactory(repository)
        entryViewModel = ViewModelProvider(this, viewModelFactory).get(EntryViewModel::class.java)

        // Shared Preferences for what metrics was used
        var sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val unitInUse = sharedPreferences.getString("unit_preference", "0")

        // Get position value from intent
        // Reference: https://stackoverflow.com/questions/45157567/how-to-pass-the-values-from-activity-to-another-activity
        val position = intent.getIntExtra("entryID", -1)

        // Go through each column in entry
        // Reference: First line is based off of XD's in class code, rest is copied from my ListAdapter
        entryViewModel.allEntriesLiveData.observe(this, Observer { entryList ->

            if (isDone) {
                finish()
            }

            val inputType =
                when (entryList[position].input) {
                    0 -> "Manual Entry"
                    1 -> "GPS"
                    2 -> "Automatic"
                    else -> "Other"
                }

            findViewById<EditText>(R.id.inputVal).setText(inputType)

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

            findViewById<EditText>(R.id.activityVal).setText(activityType)

            // Reference: https://stackoverflow.com/questions/58505052/returning-calendar-with-simpledateformat
            val calendarFormat = SimpleDateFormat("HH:mm:ss yyyy-MM-dd")
            val dateTime: String = calendarFormat.format(entryList[position].dateTime.time)

            findViewById<EditText>(R.id.dateTimeVal).setText(dateTime)

            Log.d("debug", unitInUse.toString())
            val distance = if (unitInUse == "Metric (Kilometers)") {
                (round((entryList[position].distance * 1.60934) * 1000) / 1000).toString() + " Kilometers"
            } else {
                entryList[position].distance.toString() + " Miles"
            }

            findViewById<EditText>(R.id.distanceVal).setText(distance)

            val minutes = entryList[position].duration.toInt()
            val seconds = (minutes.toDouble() - entryList[position].duration).toInt()
            val duration = if (entryList[position].duration.toInt() == 0) {
                "$seconds secs"
            } else {
                "$minutes mins $seconds secs"
            }

            findViewById<EditText>(R.id.durationVal).setText(duration)

            val calories = entryList[position].calories.toString() + " cals"

            findViewById<EditText>(R.id.caloriesVal).setText(calories)

            val heartRate = entryList[position].heartRate.toString() + " bpm"

            findViewById<EditText>(R.id.heartRateVal).setText(heartRate)
        })
    }

    // Menu Button on Toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.entry_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Checks if Toolbar button clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_button) {
            val position = intent.getIntExtra("entryID", -1)

            entryViewModel.delete(position)

            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}