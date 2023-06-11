package com.example.albatong.ee

import android.icu.text.Transliterator.Position
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.EeWriteList2Binding
import com.example.albatong.databinding.EeWriteListBinding

class EEMyDataAdapter2(val items:ArrayList<EEMyData>)
    :RecyclerView.Adapter<EEMyDataAdapter2.ViewHolder>(){


    interface OnItemClickListener{
        fun OnItemClick(data: EEMyData, position: Int)
        fun OnStarClick(data: EEMyData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: EeWriteList2Binding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.total.setOnClickListener {
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EEMyDataAdapter2.ViewHolder {
        val view = EeWriteList2Binding.inflate(
        LayoutInflater.from(parent.context),
        parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EEMyDataAdapter2.ViewHolder, position: Int) {
        holder.binding.cmtDateTv.text = items[position].date
        holder.binding.cmtTitleTv.text = items[position].title
        holder.binding.cmtUseridTv.text = items[position].userid

    }

    override fun getItemCount(): Int {
        return items.size
    }


}
