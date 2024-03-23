package com.example.justin_yan_myruns5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.ArrayList

class MainActivity: AppCompatActivity() {
    private lateinit var fragmentStart: FragmentStart
    private lateinit var fragmentHistory: FragmentHistory
    private lateinit var fragmentSettings: FragmentSettings
    private lateinit var fragments: ArrayList<Fragment>

    private lateinit var tab: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val tabTitles = arrayOf("Start", "History", "Settings")
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Setting up variables for Image Location and URI
        //  Referred to and based on Camera Implementation to XD's In Class Notes
        Util.checkPermissions(this)

        fragmentStart = FragmentStart()
        fragmentHistory = FragmentHistory()
        fragmentSettings = FragmentSettings()
        fragments = ArrayList()
        fragments.add(fragmentStart)
        fragments.add(fragmentHistory)
        fragments.add(fragmentSettings)

        tab = findViewById(R.id.tab)
        viewPager = findViewById(R.id.viewPager)

        val myFragmentStateAdapter = FragmentStateAdapter(this, fragments)
        viewPager.adapter = myFragmentStateAdapter

        // TabConfigurationStrategy handles the titles of the tab
        tabConfigurationStrategy = TabConfigurationStrategy{
                tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }
        tabLayoutMediator = TabLayoutMediator(tab, viewPager, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    // Make sure to detach it when add closes
    override fun onDestroy(){
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}