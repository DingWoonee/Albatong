package com.example.albatong.ee

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.EeItemAnnouncementBinding

class EEAdapterAnnouncement(val items:ArrayList<EEMyData>)
    :RecyclerView.Adapter<EEAdapterAnnouncement.ViewHolder>(){


    interface OnItemClickListener{
        fun OnItemClick(data: EEMyData, position: Int)
        fun OnStarClick(data: EEMyData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: EeItemAnnouncementBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.star.setOnClickListener {
                itemClickListener?.OnStarClick(items[adapterPosition], adapterPosition)
            }
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EEAdapterAnnouncement.ViewHolder {
        val view = EeItemAnnouncementBinding.inflate(
        LayoutInflater.from(parent.context),
        parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EEAdapterAnnouncement.ViewHolder, position: Int) {
        holder.binding.cmtDateTv.text = items[position].date
        holder.binding.cmtTitleTv.text = items[position].title
        holder.binding.cmtUseridTv.text = items[position].userid
        if(items[position].check=="1"){
            holder.binding.star.setImageResource(R.drawable.baseline_star_24)
            holder.binding.star.setColorFilter(Color.parseColor("#FFD400"))
        }
        else{
            holder.binding.star.setImageResource(R.drawable.baseline_star_24)
            holder.binding.star.setColorFilter(Color.parseColor("#DDDDDD"))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


}
