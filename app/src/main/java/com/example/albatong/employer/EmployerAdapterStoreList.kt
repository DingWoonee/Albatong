package com.example.albatong.employer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.data.StoreList
import com.example.albatong.databinding.EmployerItemRowBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class EmployerAdapterStoreList(options: FirebaseRecyclerOptions<StoreList>)
    : FirebaseRecyclerAdapter<StoreList, EmployerAdapterStoreList.ViewHolder>(options) {
    interface OnItemClickListener{
        fun OnItemClick(store_id:String, store_name:String)
    }
    var itemClickListener: OnItemClickListener?=null

    inner class ViewHolder(val binding: EmployerItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        init{
            binding.storeName.setOnClickListener {
                itemClickListener?.OnItemClick(binding.storeId.text.toString(), binding.storeName.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = EmployerItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StoreList) {
        holder.binding.apply {
            storeName.text = model.storeName
            storeId.text = model.store_id
        }
    }
}