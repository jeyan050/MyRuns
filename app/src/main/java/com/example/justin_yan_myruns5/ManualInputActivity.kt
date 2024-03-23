package com.example.justin_yan_myruns5

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ListView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

class ManualInputActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener, MyDialog.DialogListener {

    private val manualOptions = arrayOf(
        "Date", "Time", "Duration", "Distance",
        "Calories", "Heart Rate", "Comment"
    )
    private val calender = Calendar.getInstance()

    private lateinit var listView: ListView
    private lateinit var entry: Entry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_entry)

        entry = Entry()

        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)

        val minute = calender.get(Calendar.MINUTE)
        val second = calender.get(Calendar.SECOND)
        val hour = calender.get(Calendar.HOUR_OF_DAY)

        val btnSave : Button = findViewById<Button>(R.id.save_manual)
        val btnCancel : Button = findViewById<Button>(R.id.cancel_manual)

        // Calling Database
        var database = EntryDatabase.getInstance(this)
        var databaseDao = database.entryDatabaseDao
        var repository = EntryRepository(databaseDao)
        var viewModelFactory = EntryViewModelFactory(repository)
        var entryViewModel = ViewModelProvider(this, viewModelFactory).get(EntryViewModel::class.java)

        // Create list view of all the input options
        listView = findViewById(R.id.list)
        var arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, manualOptions)
        listView.adapter = arrayAdapter

        // Set Input type

        when (intent.getStringExtra("activity_type")) {
            "Running" -> entry.activity = 0
            "Walking" -> entry.activity = 1
            "Standing" -> entry.activity = 2
            "Cycling" -> entry.activity = 3
            "Hiking" -> entry.activity = 4
            "Downhill Skiing" -> entry.activity = 5
            "Cross-Country Skiing" -> entry.activity = 6
            "Snowboarding" -> entry.activity = 7
            "Skating" -> entry.activity = 8
            "Swimming" -> entry.activity = 9
            "Mountain Biking" -> entry.activity = 10
            "Wheelchair" -> entry.activity = 11
            "Elliptical" -> entry.activity = 12
            "Other" -> entry.activity = 13
            else -> entry.activity = -1
        }

        // Checks for which one is clicked and opens dialog
        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String

            // Time and Date is based on XD's lecture code
            if (selectedItem == "Date") {
                val dataPickerDialog = DatePickerDialog(
                    this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        entry.dateTime.set(Calendar.YEAR, year)
                        entry.dateTime.set(Calendar.MONTH, month)
                        entry.dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }, year, month, day
                )
                dataPickerDialog.show()

            } else if (selectedItem == "Time") {
                val timePickerDialog = TimePickerDialog(
                    this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        entry.dateTime.set(Calendar.HOUR_OF_DAY, hour)
                        entry.dateTime.set(Calendar.MINUTE, minute)
                        entry.dateTime.set(Calendar.SECOND, 0)
                    }, hour, minute, false
                )
                timePickerDialog.show()

            // If other dialog clicked, then show and store value
            } else {
                val myDialog = MyDialog()
                val bundle = Bundle()
                bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.MANUAL_DIALOG)
                bundle.putString(MyDialog.DIALOG_TITLE, selectedItem)
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "tag")
            }
        }

        // Buttons to save or cancel entry
        btnSave.setOnClickListener {
            Log.d("Entry:",entry.toString())
            entryViewModel.insert(entry)

            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    // Checks what input type was clicked and set it to entry value
    override fun onInputSet(type: String, input: String) {
        when (type) {
            "Duration" -> {
                entry.duration = input.toDouble()
            }
            "Distance" -> {
                entry.distance = input.toDouble()
            }
            "Calories" -> {
                entry.calories = input.toDouble()
            }
            "Heart Rate" -> {
                entry.heartRate = input.toDouble()
            }
            "Comment" -> {
                entry.comment = input
            }
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
    }

    override fun onTimeSet(view: TimePicker, hour: Int, minute: Int){
    }
}