package com.example.justin_yan_myruns5

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//  MyViewModel based on XD's class Notes
class MyViewModel: ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}