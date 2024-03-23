package com.example.justin_yan_myruns5

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class FragmentSettings : PreferenceFragmentCompat() {
    // Create settings fragment view
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_preference)
    }
}