package com.example.albatong.ee


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.EeItemCalendarListBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions


class EEAdapterCalendar(options: FirebaseRecyclerOptions<Schedule>) :
    FirebaseRecyclerAdapter<Schedule, EEAdapterCalendar.ScheduleViewHolder>(options) {

    interface OnItemClickListener {
        fun OnItemClick(name: String)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleViewHolder(val binding: EeItemCalendarListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.nameTextView.setOnClickListener {
                itemClickListener?.OnItemClick(binding.nameTextView.text.toString())
            }
        }

    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EEAdapterCalendar.ScheduleViewHolder {
        val view =
            EeItemCalendarListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int, model: Schedule
    ) {
        holder.binding.apply {
            nameTextView.text = model.name
            timeTextView.text = "${model.startTime} - ${model.endTime}"
        }
    }
}