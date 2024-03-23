package com.example.justin_yan_myruns5

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.example.justin_yan_myruns5.databinding.MapDetailsBinding
import com.google.android.gms.maps.CameraUpdate
import kotlin.math.round

class MapDetails : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private var lastMarker : Marker? = null
    private lateinit var binding: MapDetailsBinding

    private lateinit var database: EntryDatabase
    private lateinit var databaseDao: EntryDatabaseDao
    private lateinit var repository: EntryRepository
    private lateinit var viewModelFactory: EntryViewModelFactory
    private lateinit var entryViewModel: EntryViewModel

    private lateinit var inputType : String
    private lateinit var activityType : String
    private lateinit var distance : String
    private lateinit var avgSpeed : String
    private lateinit var calories : String
    private lateinit var curSpeed : String
    private lateinit var climb : String
    private lateinit var locationList : ArrayList<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creates a binding on current view, which allows the map to display
        // Reference: https://stackoverflow.com/questions/71877641/inflating-view-using-data-binding
        binding = MapDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap) {
        // Map variables
        mMap = p0
        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.RED)
        polylines = ArrayList()

        // Database
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

            inputType =
                when (entryList[position].input) {
                    0 -> "Manual Entry"
                    1 -> "GPS"
                    2 -> "Automatic"
                    else -> "Missing"
                }

            activityType = "Type: " + when (entryList[position].activity) {
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

            findViewById<TextView>(R.id.activity).setText(activityType)

            distance = if (unitInUse == "Metric (Kilometers)") {
                "Distance: " + (entryList[position].distance * 1.60934).toString() + " Kilometers"
            } else {
                "Distance: " + entryList[position].distance.toString() + " Miles"
            }

            findViewById<TextView>(R.id.distance).setText(distance)

            calories = "Calorie: " + round(entryList[position].calories).toString()

            findViewById<TextView>(R.id.calorie).setText(calories)

            avgSpeed = if (unitInUse == "Metric (Kilometers)") {
                "Cur Speed: " + ((round(entryList[position].pace * 1.60934) * 1000) / 1000).toString() + " m/h"
            } else {
                "Avg Speed: " + entryList[position].pace.toString() + " m/h"
            }

            findViewById<TextView>(R.id.average_speed).setText(avgSpeed)

            curSpeed = if (unitInUse == "Metric (Kilometers)") {
                "Cur Speed: " + ((round(entryList[position].speed * 1.60934) * 1000) / 1000).toString() + " m/h"
            } else {
                "Cur Speed: " + entryList[position].speed.toString() + " m/h"
            }

            findViewById<TextView>(R.id.current_speed).setText(curSpeed)

            climb = if (unitInUse == "Metric (Kilometers)") {
                "Climb: " + ((round(entryList[position].climb * 1.60934) * 1000) / 1000).toString() + " Kilometers"
            } else {
                "Climb: " + entryList[position].climb.toString() + " Miles"
            }

            findViewById<TextView>(R.id.climb).setText(climb)

            // Location List
            locationList = entryList[position].locations!!

            // Get start and end point
            val start = LatLng(locationList[0].latitude,locationList[0].longitude )
            val end = LatLng(locationList[locationList.size -1].latitude,locationList[locationList.size-1].longitude )

            // Get path lines
            // Referenced to XD's notes
            val polylineOptions = PolylineOptions()
            polylineOptions.addAll(locationList)
            mMap.addPolyline(polylineOptions)

            // Set markers
            markerOptions.position(start)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            mMap.addMarker(markerOptions)

            markerOptions.position(end)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mMap.addMarker(markerOptions)

            // Set camera to location
            // Referenced to XD's notes
            val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(end, 17f)
            mMap.animateCamera(cameraUpdate)

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
            database = EntryDatabase.getInstance(this)
            databaseDao = database.entryDatabaseDao
            repository = EntryRepository(databaseDao)
            viewModelFactory = EntryViewModelFactory(repository)
            entryViewModel = ViewModelProvider(this, viewModelFactory).get(EntryViewModel::class.java)

            val position = intent.getIntExtra("entryID", -1)

            entryViewModel.delete(position)

            onDestroy()
        }

        return super.onOptionsItemSelected(item)
    }
}