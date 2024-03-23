package com.example.justin_yan_myruns5

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.Job
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.round
import kotlin.math.sqrt

class TrackingService : Service(), LocationListener, SensorEventListener {

    private lateinit var  myBinder: MyBinder
    private lateinit var notificationManager: NotificationManager
    private var msgHandler: Handler? = null
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "MyRuns Notification"

    private lateinit var locationManager : LocationManager
    private lateinit var locationList : ArrayList<LatLng>
    private lateinit var prevLocation : Location

    private lateinit var activityType : String
    private lateinit var inputType : String
    private lateinit var units : String

    private var avgSpeed = 0.0
    private var curSpeed = 0.0
    private var climb = 0.0
    private var calories = 0.0
    private var distance = 0.0
    private var duration = 0.0

    private var startTime = 0.0
    private var prevTime = 0.0
    private var endTime = 0.0

    private var stopThread = false

    private lateinit var mAccBuffer : ArrayBlockingQueue<Double>
    private lateinit var sensorManager : SensorManager

    companion object{
        val MSG_INT_VALUE = 0
        val AUTO_MSG_INT_VALUE = 1
        val AUTO_ACTIVITY_KEY = "activity_key"
    }

    override fun onCreate() {
        super.onCreate()

        // Show notification
        showNotification()

        // Initalize Variables
        myBinder = MyBinder()
        locationList = ArrayList()

        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)

        initLocationManager()

        // Executes onSensorChangedTask function, since execute is deprecated so had to use threads
        // Reference: https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
        object: Thread() {
            override fun run() {
                super.run()
                OnSensorChangedTask()
            }
        }.start()
    }

    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // Get updated location details
            val provider = LocationManager.GPS_PROVIDER
            if (provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                locationManager.requestLocationUpdates(provider, 0, 0f, this)

                // Location updated
                if (location != null) {
                    onLocationChanged(location)
                }
            }
        } catch (e: SecurityException) {
        }
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        return true
    }

    // Start service
    // Reference: Based on XD in Class Notes
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Get intent values
        if (intent != null) {
            activityType = intent.getStringExtra("activity_type").toString()
            inputType = intent.getStringExtra("input_type").toString()
            units = intent.getStringExtra("units").toString()

        }

        return START_NOT_STICKY
    }

    // If location changed
    override fun onLocationChanged(location: Location) {

        val latLng = LatLng(location.latitude, location.longitude)

        // Add to list
        locationList.add(latLng)

        // If location was starting position
        if (locationList.size == 1) {
            startTime = System.currentTimeMillis().toDouble()
        }

        // If 2nd or xth location, update values
        if (this::prevLocation.isInitialized) {
            endTime = System.currentTimeMillis().toDouble()

            duration = ((round(((endTime - startTime) * 1000).toDouble()) / 1000 ) / 1000)
            distance += (round(prevLocation.distanceTo(location) * 1000) / 1000) / 1609.34
            if (duration != 0.0) {
                avgSpeed = distance / ( duration / 3600)
                curSpeed = (prevLocation.distanceTo(location).toDouble()) / ((endTime - prevTime) / 3600)
            }

            climb = (prevLocation.altitude - location.altitude) / 1000

            var met = 0.0
            when (activityType) {
                "Running" -> met = 7.0
                "Walking" -> met = 8.3
                "Standing" -> met = 2.3
                "Cycling" -> met = 7.5
                "Hiking" -> met = 6.0
                "Downhill Skiing" -> met = 5.4
                "Cross-Country Skiing" -> met = 9.0
                "Snowboarding" -> met = 5.3
                "Skating" -> met = 7.0
                "Swimming" -> met = 7.0
                "Mountain Biking" -> met = 8.5
                "Wheelchair" -> met = 3.5
                "Elliptical" -> met = 5.0
                else -> met = 1.0
            }
            calories = (met / 60) * (duration / 60000) * distance
        }

        prevLocation = location
        prevTime = System.currentTimeMillis().toDouble()

        // Send details back to display
        if (msgHandler != null) {
            val bundle = Bundle()
            bundle.putDouble("duration", round(duration * 100) / 100)
            bundle.putDouble("distance", round(distance * 100) / 100)
            bundle.putDouble("current_speed", round(curSpeed * 100) / 100)
            bundle.putDouble("average_speed", round(avgSpeed * 100) / 100)
            bundle.putDouble("calories", round(calories))
            bundle.putDouble("climb", round(climb * 100) / 100)
            bundle.putString("locations", Gson().toJson(locationList))

            val message = msgHandler!!.obtainMessage()
            message.data = bundle
            message.what = MSG_INT_VALUE
            msgHandler!!.sendMessage(message)
        }
    }

    // Referenced: Based off from XD's in class code
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                        * event.values!![2])).toDouble())
                try {
                    mAccBuffer.add(m)
                } catch (e: IllegalStateException) {
                    val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                    mAccBuffer.drainTo(newBuf)
                    mAccBuffer = newBuf
                    mAccBuffer.add(m)
                }
            }
        }
    }

    // Referenced: Based off from XD's in class code
    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = sqrt(accBlock[i] * accBlock[i] + im[i] * im[i])
                            im[i] = .0 // Clear the field
                        }

                        // Append max after frequency component
                        val automaticCurrentActivity =
                            when (WekaClassifier.classify(accBlock.toTypedArray()).toInt()) {
                                0 -> "Standing"
                                1 -> "Walking"
                                2 -> "Running"
                                else -> "Other"
                            }

                        // Send message back to ViewModel
                        if(msgHandler != null) {
                            val bundle = Bundle()
                            bundle.putString("AUTO_ACTIVITY_KEY", automaticCurrentActivity)
                            val message = msgHandler!!.obtainMessage()
                            message.data = bundle
                            msgHandler!!.sendMessage(message)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onTaskRemoved(rootIntent: Intent?) {}

    // Stops service
    override fun onDestroy() {
        super.onDestroy()

        msgHandler = null

        locationList.clear()

        locationManager.removeUpdates(this)

        stopThread = true
    }

    // Shows notification
    // Reference: Based on XD's notes
    private fun showNotification() {
        val intent = Intent(this, MapDisplayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)

        notificationBuilder.setSmallIcon(R.drawable.rocket)
        notificationBuilder.setContentTitle("MyRuns5")
        notificationBuilder.setContentText("Activity is recording location now.")
        notificationBuilder.setContentIntent(pendingIntent)

        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "MyRuns'Tracker", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}