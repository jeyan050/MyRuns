package com.example.justin_yan_myruns5

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import java.util.Calendar
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0

    private var StartInitalised = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private var lastMarker : Marker? = null

    private lateinit var trackerViewModel : TrackerViewModel
    private lateinit var serviceIntent : Intent

    private lateinit var locationList: ArrayList<LatLng>

    private var isBind: Boolean = false

    private var metricUnits: String = ""
    private var inputTypeVal: String = ""
    private var activityTypeVal: String = ""
    private var dateTimeVal : Calendar = Calendar.getInstance()
    private var durationVal: Double = 0.0
    private var distanceVal: Double = 0.0
    private var avgPacVal: Double = 0.0
    private var avgSpeedVal: Double = 0.0
    private var caloriesVal: Double = 0.0
    private var climbVal: Double = 0.0

    private lateinit var activityTypeTV: TextView
    private lateinit var averageSpeedTV: TextView
    private lateinit var currentSpeedTV: TextView
    private lateinit var climbTV: TextView
    private lateinit var caloriesTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var units: String

    private var isStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_entry)

        val btnSave : Button = findViewById<Button>(R.id.save_manual)
        val btnCancel : Button = findViewById<Button>(R.id.cancel_manual)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Intent for Service and create View Model for Service
        serviceIntent = Intent(this, TrackingService::class.java)
        trackerViewModel = ViewModelProvider(this)[TrackerViewModel::class.java]

        locationList = ArrayList()

        inputTypeVal = intent.getStringExtra("input_type").toString()

        activityTypeVal = if (inputTypeVal == "Automatic") {
            "Standing"
        } else {
            intent.getStringExtra("activity_type").toString()
        }

        activityTypeTV = findViewById(R.id.map_activity_type)
        averageSpeedTV = findViewById(R.id.map_average_speed)
        currentSpeedTV = findViewById(R.id.map_current_speed)
        climbTV = findViewById(R.id.map_climb)
        caloriesTV = findViewById(R.id.map_calorie)
        distanceTV = findViewById(R.id.map_distance)

        activityTypeTV.text = activityTypeVal

        var sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        metricUnits = sharedPreferences.getString("unit_preference", "0").toString()

        // Set Current Date and Time for Entry
        val calender = Calendar.getInstance()

        dateTimeVal.set(Calendar.YEAR, calender.get(Calendar.YEAR))
        dateTimeVal.set(Calendar.MONTH, calender.get(Calendar.MONTH))
        dateTimeVal.set(Calendar.DAY_OF_MONTH, calender.get(Calendar.DAY_OF_MONTH))
        dateTimeVal.set(Calendar.HOUR_OF_DAY, calender.get(Calendar.HOUR_OF_DAY))
        dateTimeVal.set(Calendar.MINUTE, calender.get(Calendar.MINUTE))
        dateTimeVal.set(Calendar.SECOND, calender.get(Calendar.SECOND))

        // Puts variables for service to use later on
        serviceIntent.putExtra("units", metricUnits)
        serviceIntent.putExtra("activity_type", activityTypeVal)
        serviceIntent.putExtra("input_type", inputTypeVal)

        // Calling Database
        var database = EntryDatabase.getInstance(this)
        var databaseDao = database.entryDatabaseDao
        var repository = EntryRepository(databaseDao)
        var viewModelFactory = EntryViewModelFactory(repository)
        var entryViewModel = ViewModelProvider(this, viewModelFactory).get(EntryViewModel::class.java)

        // If Button saved, then save values
        btnSave.setOnClickListener {
            var entry = Entry()
            when (inputTypeVal) {
                "GPS" -> entry.input = 1
                "Automatic" -> entry.input = 2
                else -> entry.input = 3
            }
            when (activityTypeVal) {
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
                else -> entry.activity = 14
            }

            entry.dateTime = dateTimeVal
            entry.duration = durationVal
            entry.distance = distanceVal
            entry.pace = avgPacVal
            entry.speed = avgSpeedVal
            entry.heartRate = 0.0
            entry.calories = caloriesVal
            entry.climb = climbVal
            entry.comment = ""
            entry.locations = locationList

            // Save entry to database
            entryViewModel.insert(entry)

            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    // Initalize Map
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        // Check users permission
        checkPermission()

        // Communicates with View Model to get Updated Values
        trackerViewModel.bundle.observe(this) {
            getMapVals(it)
        }
    }

    // Start Tracker service, and binds intent to it
    private fun startService() {
        applicationContext.startService(serviceIntent)
        if (!isBind)
        {
            applicationContext.bindService(serviceIntent, trackerViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
        }
    }

    // Gets values from map entry
    private fun getMapVals(bundle : Bundle) {
        // Location list
        locationList = toArrayList(bundle.getString("locations")!!)!!

        updateMapScoreboard()

        // If empty, then just add a marker in center
        if (locationList.isEmpty()) {
            mMap.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))
        } else {
            durationVal = bundle.getDouble("duration")
            distanceVal = bundle.getDouble("distance")
            avgSpeedVal = if (bundle.getDouble("current_speed") == null) {
                0.0
            } else {
                bundle.getDouble("current_speed")
            }
            avgPacVal = bundle.getDouble("average_speed")
            caloriesVal = bundle.getDouble("calories")
            climbVal = bundle.getDouble("climb")

            // Updates polylines on map
            polylineOptions = PolylineOptions()
            polylineOptions.addAll(locationList)
            mMap.addPolyline(polylineOptions)

            // Centers Map when initally opened
            if (!StartInitalised)
            {
                // Sets start marker
                val latLng = locationList.first()
                markerOptions.position(latLng).title("Start location")
                mMap.addMarker(markerOptions)
                StartInitalised = true
            }

            // Updates last marker to current location
            val latLng = locationList.last()
            lastMarker?.remove()
            markerOptions.position(latLng)
            markerOptions.title("End location")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            lastMarker = mMap.addMarker(markerOptions)

            // Change camera angle to follow updated point
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
        }
    }

    // Convert json to Arraylist (This is for locations to be stored in database)
    private fun toArrayList(json: String): ArrayList<LatLng> {
        return Gson().fromJson(json, object : TypeToken<ArrayList<LatLng>>() {}.type)
    }

    @SuppressLint("SetTextI18n")
    // Updates map data
    private fun updateMapScoreboard() {
        // If in Kilometers

        if (inputTypeVal == "Automatic"){
            trackerViewModel.activity_type.observe(this) {
                Log.d("test", it.toString())
                when(it.toString())
                {
                    "Running" -> activityTypeTV.text = "Running"
                    "Walking" -> activityTypeTV.text = "Walking"
                    "Standing" -> activityTypeTV.text = "Standing"
                    "Other" -> activityTypeTV.text = "Other"
                }
            }
        }

        if (metricUnits == "Metric (Kilometers)") {
            averageSpeedTV.text = "Avg speed: ${String.format("%.1f", (avgSpeedVal * 1.60934))} km/h"
            currentSpeedTV.text = "Cur speed: ${String.format("%.1f", avgPacVal * 1.60934)} km/h"
            climbTV.text = "Climb: ${String.format("%.1f", climbVal * 1.60934)} Kilometers"
            caloriesTV.text = "Calorie: ${String.format("%.1f", caloriesVal)}"
            distanceTV.text = "Distance: ${String.format("%.3f", distanceVal * 1.60934)} Kilometers"
        } else {
            averageSpeedTV.text = "Avg speed: ${String.format("%.1f" ,avgSpeedVal)} m/h"
            currentSpeedTV.text = "Cur speed: ${String.format("%.1f" ,avgPacVal)} m/h"
            climbTV.text = "Climb: ${String.format("%.1f" ,climbVal)} Miles"
            caloriesTV.text = "Calorie: ${String.format("%.1f" ,caloriesVal)}"
            distanceTV.text = "Distance: ${String.format("%.3f" ,distanceVal)} Miles"
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            startService()
    }

    override fun onMapClick(p0: LatLng) {
    }

    override fun onMapLongClick(p0: LatLng) {
    }

    override fun onLocationChanged(location: Location) {
    }
}