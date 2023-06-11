package com.example.albatong.er

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.ErItemEmployeemanageBinding

class ERAdapterEmployeeManage(private val scheduleList: List<Schedule>) :
    RecyclerView.Adapter<ERAdapterEmployeeManage.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ErItemEmployeemanageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    inner class ScheduleViewHolder(private val binding: ErItemEmployeemanageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule) {
            binding.apply {
                textViewName.text = schedule.name
                textViewSalary.text = schedule.salary.toString()
            }
        }
    }
}
