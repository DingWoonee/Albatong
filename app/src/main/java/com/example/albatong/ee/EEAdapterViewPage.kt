package com.example.albatong.ee

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EEAdapterViewPage(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> EEFragmentCalendar()
            1 -> EEFragmentAnnouncement()
            2 -> EEFragmentTakeOver()
            else -> EEFragmentCalendar() // default
        }
    }
}