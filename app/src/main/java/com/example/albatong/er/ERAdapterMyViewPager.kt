package com.example.albatong.er

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ERAdapterMyViewPager(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> ERFragmentScheduleManage()
            1-> ERFragmentEmployeeManage()
            2-> ERFragmentTakeOver()
            3-> ERFragmentAnnouncement()
            else-> ERFragmentScheduleManage()
        }
    }

}