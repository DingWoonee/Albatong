package com.example.albatong.ee


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.EeItemCalendarListBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions


class EEAdapterCalendar(options: FirebaseRecyclerOptions<Schedule>) :
    FirebaseRecyclerAdapter<Schedule, EEAdapterCalendar.ScheduleViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemNameClick(item: Schedule)
        fun onItemChangeClick(item: Schedule)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleViewHolder(val binding: EeItemCalendarListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.nameTextView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    itemClickListener?.onItemNameClick(item)
                }
            }
            binding.changeBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    itemClickListener?.onItemChangeClick(item)
                }
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