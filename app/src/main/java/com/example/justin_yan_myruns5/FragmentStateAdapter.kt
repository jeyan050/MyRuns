package com.example.justin_yan_myruns5

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentStateAdapter(activity: FragmentActivity, var list: ArrayList<Fragment>): FragmentStateAdapter(activity) {
    // The 2 callback functions you need to implement that are caught automatically by Android
    // Referred to and based on Fragment Implementation of XD's lecture Notes
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
}