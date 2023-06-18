package com.example.albatong.login

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.SignData
import com.example.albatong.databinding.SignDetailBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class SignAdapter(var options: FirebaseRecyclerOptions<SignData>)
    : FirebaseRecyclerAdapter<SignData, SignAdapter.ViewHolder>(options) {

    interface OnItemClickListener{
        fun OnItemClick(data: SignData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: SignDetailBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.total3.setOnClickListener {
                itemClickListener?.OnItemClick(options.snapshots[bindingAdapterPosition], bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SignDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: SignData) {
        Log.i("check", model.title.toString())
        holder.binding.signTitle.text = model.title
        holder.binding.signDate.text = model.date
    }
}