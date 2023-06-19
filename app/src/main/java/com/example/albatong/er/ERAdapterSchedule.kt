package com.example.albatong.er

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.ErItemScheduleListBinding
import com.example.albatong.databinding.ErScheduleDeleteDialogBinding
import com.example.albatong.databinding.ErSettingConfirmuserDialogBinding
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
            val inflater = LayoutInflater.from(context)
            val dlgBinding = ErScheduleDeleteDialogBinding.inflate(inflater)
            val userBuilder = AlertDialog.Builder(context)
            val dlg = userBuilder.setView(dlgBinding.root).show()

            dlg.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlg.window?.setGravity(Gravity.CENTER)
            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dlgBinding.registerBtn.setOnClickListener {
                deleteItem(item)
                dlg.dismiss()
            }

            dlgBinding.cancelBtn.setOnClickListener {
                dlg.dismiss()
            }
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