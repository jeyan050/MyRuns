package com.example.justin_yan_myruns5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import android.content.Intent
import android.util.Log
import android.widget.Spinner

class FragmentStart : Fragment() {
    // Create start fragment view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        val button : Button = view.findViewById<Button>(R.id.btnStartEntry)

        // When start button clicked, checks what input type was chosen and opens activity
        button.setOnClickListener {
            val inputType = view.findViewById<Spinner>(R.id.spinnerInputType).selectedItem.toString()
            val activityType = view.findViewById<Spinner>(R.id.spinnerActivityType).selectedItem.toString()

            var intent = Intent()
            intent = if (inputType == "Manual Entry"){
                Intent (activity, ManualInputActivity::class.java)
            } else {
                Intent (activity, MapDisplayActivity::class.java)
            }

            intent.putExtra("input_type", inputType)
            intent.putExtra("activity_type", activityType)
            Log.d("debug", activityType.toString())
            startActivity(intent)
        }

        return view
    }

}