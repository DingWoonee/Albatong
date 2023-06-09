package com.example.albatong.employee

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.albatong.data.UserSalary
import com.example.albatong.databinding.EmployeeSalaryRowBinding

class EmployeeAdapterSalaryRecyclerView(var values: ArrayList<UserSalary>)
    : RecyclerView.Adapter<EmployeeAdapterSalaryRecyclerView.ViewHolder>() {

    inner class ViewHolder(val binding: EmployeeSalaryRowBinding) : RecyclerView.ViewHolder(binding.root) {
//        init {
//            binding.item.setOnClickListener {
//                itemClickListener?.OnItemClick(bindingAdapterPosition)
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = EmployeeSalaryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            storeName.text = values[position].storeName

            val totalMinutes = values[position].totalMinutes
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            totalTime.text = "$hours 시간 $minutes 분"
            monthlySalary.text = values[position].monthlySalary.toString()+"원"
        }
    }
}
