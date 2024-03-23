package com.example.justin_yan_myruns5

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrackerViewModel : ViewModel(), ServiceConnection{
    private var messageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())

    // Bundle
    private val _bundle = MutableLiveData<Bundle>()
    val bundle : LiveData<Bundle> = _bundle

    // Activity Type
    private val _activity_type = MutableLiveData<String>()

    val activity_type: LiveData<String>
        get() = _activity_type

    // When Service is established, initialize Tracking Service
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("debug:", "Activity: onServiceConnected() called~~~")
        val trackingBinder = service as TrackingService.MyBinder
        trackingBinder.setmsgHandler(messageHandler)
    }

    // When Service is disconnected
    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("debug:", "Activity: onServiceDisconnected() called~~~")
    }

    // Function to set values to be passed back to Map Activity
    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            if (message.what == TrackingService.MSG_INT_VALUE) {
                _bundle.value = message.data
            } else if (message.what == TrackingService.AUTO_MSG_INT_VALUE) {
                _activity_type.value = message.data.getString(TrackingService.AUTO_ACTIVITY_KEY)
            }
        }
    }
}