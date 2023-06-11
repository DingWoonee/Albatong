package com.example.albatong.ee

import android.icu.text.Transliterator.Position
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.EeWriteListBinding

class EEMyDataAdapter(val items:ArrayList<EEMyData>)
    :RecyclerView.Adapter<EEMyDataAdapter.ViewHolder>(){


    interface OnItemClickListener{
        fun OnItemClick(data: EEMyData, position: Int)
        fun OnStarClick(data: EEMyData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding:EeWriteListBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.total2.setOnClickListener {
                itemClickListener?.OnItemClick(items[adapterPosition], adapterPosition)
            }
            binding.star.setOnClickListener{
                itemClickListener?.OnStarClick(items[adapterPosition], adapterPosition)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EEMyDataAdapter.ViewHolder {
        val view = EeWriteListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EEMyDataAdapter.ViewHolder, position: Int) {
        holder.binding.cmtDateTv.text = items[position].date
        holder.binding.cmtTitleTv.text = items[position].title
        holder.binding.cmtUseridTv.text = items[position].userid
        if(items[position].check=="1")
            holder.binding.star.setImageResource(R.drawable.baseline_star_24)
        else
            holder.binding.star.setImageResource(R.drawable.baseline_star_border_24)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}
