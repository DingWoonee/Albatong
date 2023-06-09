package com.example.albatong.employee

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EmployeeAdapterViewPage(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> EmployeeFragmentCalendar()
            1 -> EmployeeFragmentSalaryCalculate()
            2 -> EmployeeFragmentStoreList()
            else -> EmployeeFragmentCalendar() // default
        }
    }
}