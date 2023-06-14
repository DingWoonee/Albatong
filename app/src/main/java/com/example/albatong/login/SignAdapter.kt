package com.example.albatong.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.SignData
import com.example.albatong.databinding.SignDetailBinding


class SignAdapter(val items:ArrayList<SignData>)
    :RecyclerView.Adapter<SignAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun OnItemClick(data: SignData, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: SignDetailBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.total3.setOnClickListener {
                itemClickListener?.OnItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    fun addItem(a: SignData){
        items.add((a))
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        if(position>-1){
            items.removeAt(position)
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignAdapter.ViewHolder {
        val view = SignDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SignAdapter.ViewHolder, position: Int) {
        if(items[position].type=="1"){
            holder.binding.signTitle.text = items[position].title
            holder.binding.signDate.text = items[position].date
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}