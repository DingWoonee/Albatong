package com.example.albatong.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.EmployeeCalendarScheduleRowBinding
import com.example.albatong.employee.EmployeeAdapterMainCalendar.EmployeeScheduleViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class EmployeeAdapterMainCalendar(var items:MutableList<Schedule>)
    : RecyclerView.Adapter<EmployeeAdapterMainCalendar.EmployeeScheduleViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }
    interface OnItemClickListener{
        fun OnItemClick(schedule:Schedule)
    }
    var itemClickListener:OnItemClickListener?=null

    var date = "THU\nJUN 30"
    var store_id = ""
    var storeBackColorMap: MutableMap<String, Int> = mutableMapOf()

    inner class EmployeeScheduleViewHolder(val binding: EmployeeCalendarScheduleRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.scheduleTouchView.setOnClickListener {
                itemClickListener?.OnItemClick(items[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeScheduleViewHolder {
        val view =
            EmployeeCalendarScheduleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmployeeScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeScheduleViewHolder, position: Int) {
        var tempSchedule = items[position]
        holder.binding.apply {
            scheduleDateText.setBackgroundColor(storeBackColorMap[tempSchedule.store_id]!!)
            scheduleDateText.text = date
            scheduleNameText.text = tempSchedule.storeName
            scheduleStartTimeText.text = tempSchedule.startTime
            scheduleEndText.text = tempSchedule.endTime
        }
    }
}