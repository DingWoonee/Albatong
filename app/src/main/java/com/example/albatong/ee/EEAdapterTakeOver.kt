package com.example.albatong.ee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.EeItemTakeoverBinding

class EEAdapterTakeOver(val items:ArrayList<EEMyData>)
    :RecyclerView.Adapter<EEAdapterTakeOver.ViewHolder>(){


    interface OnItemClickListener{
        fun OnItemClick(data: EEMyData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: EeItemTakeoverBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.item.setOnClickListener {
                itemClickListener?.OnItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    fun addItem(a:EEMyData){
        items.add((a))
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        if(position>-1){
            items.removeAt(position)
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EEAdapterTakeOver.ViewHolder {
        val view = EeItemTakeoverBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EEAdapterTakeOver.ViewHolder, position: Int) {
        holder.binding.cmtDateTv.text = items[position].date
        holder.binding.cmtTitleTv.text = items[position].title
        holder.binding.cmtUseridTv.text = items[position].userid
    }

    override fun getItemCount(): Int {
        return items.size
    }


}
