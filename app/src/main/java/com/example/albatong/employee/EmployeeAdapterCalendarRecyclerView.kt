package com.example.albatong.employee

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.EmployeeStoreRowBinding

class EmployeeAdapterCalendarRecyclerView(var values: ArrayList<Schedule>)
    : RecyclerView.Adapter<EmployeeAdapterCalendarRecyclerView.ViewHolder>() {


    inner class ViewHolder(val binding: EmployeeStoreRowBinding) : RecyclerView.ViewHolder(binding.root) {
//        init {
//            binding.item.setOnClickListener {
//                itemClickListener?.OnItemClick(bindingAdapterPosition)
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = EmployeeStoreRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            storename.text = values[position].storeName
            time.text = values[position].startTime + " ~ " + values[position].endTime
        }
    }
}
