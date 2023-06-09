package com.example.albatong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ERAdapterTimeTable
    : RecyclerView.Adapter<ERAdapterTimeTable.TimeTableViewHolder>() {
    private var timeTableData: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeTableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.er_item_time_table, parent, false)
        return TimeTableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeTableViewHolder, position: Int) {
        val time = timeTableData[position]
        holder.bind(time)
    }

    override fun getItemCount(): Int {
        return timeTableData.size
    }

    fun setData(data: List<String>) {
        timeTableData = data
        notifyDataSetChanged()
    }

    inner class TimeTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        fun bind(time: String) {
            timeTextView.text = time
        }
    }
}
