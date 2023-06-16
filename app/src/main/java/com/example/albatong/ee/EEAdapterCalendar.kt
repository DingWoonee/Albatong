package com.example.albatong.ee


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.EeItemCalendarListBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EEAdapterCalendar(options: FirebaseRecyclerOptions<Schedule>, private val userID:String?, private val storeID:String?) :
    FirebaseRecyclerAdapter<Schedule, EEAdapterCalendar.ScheduleViewHolder>(options) {

    private lateinit var edb: DatabaseReference

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EEAdapterCalendar.ScheduleViewHolder {
        val view =
            EeItemCalendarListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ScheduleViewHolder, position: Int, model: Schedule
    ) {
        holder.binding.apply {
            nameTextView.text = model.name
            timeTextView.text = "${model.startTime} - ${model.endTime}"


           edb = Firebase.database.getReference("Stores").child(storeID!!).child("storeInfo")
                .child("employee")
            val userRef = edb.child(userID!!)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value?.toString()

                    if (userName == model.name) {
                        changeBtn.isVisible = true
                    } else {
                        changeBtn.isInvisible = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        }
    }
}