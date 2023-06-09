package com.example.albatong.er

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.ErItemEmployeemanageBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ERAdapterEmployeeManage(options: FirebaseRecyclerOptions<Schedule>) :
    FirebaseRecyclerAdapter<Schedule, ERAdapterEmployeeManage.ScheduleViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ERAdapterEmployeeManage.ScheduleViewHolder {
        val view =
            ErItemEmployeemanageBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int, model: Schedule
    ) {
        holder.binding.apply{
            textViewName.text = model.name
            textViewSalary.text = model.salary.toString()
            textViewEmployeeId.text = model.endTime
        }
    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    interface OnItemClickListener{
        fun OnItemClick(name:String)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleViewHolder(val binding: ErItemEmployeemanageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.textViewName.setOnClickListener {
                itemClickListener?.OnItemClick(binding.textViewName.toString())
            }
        }
    }
}