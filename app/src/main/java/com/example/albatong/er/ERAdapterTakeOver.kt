package com.example.albatong.er

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.albatong.databinding.ErFragmentTakeoverBinding

class ERAdapterTakeOver(
    private val values: List<String>
) : RecyclerView.Adapter<ERAdapterTakeOver.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ErFragmentTakeoverBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ErFragmentTakeoverBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.content2
        val btn = binding.firebtn

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}