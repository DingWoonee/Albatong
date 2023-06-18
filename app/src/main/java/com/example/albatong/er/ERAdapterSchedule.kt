package com.example.albatong.er

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.ErItemScheduleListBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ERAdapterSchedule(options: FirebaseRecyclerOptions<Schedule>) :
    FirebaseRecyclerAdapter<Schedule, ERAdapterSchedule.ScheduleViewHolder>(options) {

    interface OnItemClickListener {
        fun OnItemClick(name: String)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleViewHolder(val binding: ErItemScheduleListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.nameTextView.setOnClickListener {
                itemClickListener?.OnItemClick(binding.nameTextView.text.toString())
            }
            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    showDeleteConfirmationDialog(itemView.context, item)
                }
            }
        }

        private fun showDeleteConfirmationDialog(context: Context, item: Schedule) {
            AlertDialog.Builder(context)
                .setTitle("삭제 확인")
                .setMessage("정말로 삭제하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    // 확인 버튼을 클릭한 경우, Firebase와 RecyclerView에서 해당 아이템을 삭제
                    deleteItem(item)
                    dialog.dismiss()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun deleteItem(item: Schedule) {
            val position = snapshots.indexOf(item)
            if (position != -1) {
                val reference = getRef(position)
                reference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        notifyItemRemoved(position)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ERAdapterSchedule.ScheduleViewHolder {
        val view =
            ErItemScheduleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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