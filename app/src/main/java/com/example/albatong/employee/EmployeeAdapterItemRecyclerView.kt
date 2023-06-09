package com.example.albatong.employee

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.albatong.data.StoreList
import com.example.albatong.databinding.EmployeeFragmentItemBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class EmployeeAdapterItemRecyclerView(options: FirebaseRecyclerOptions<StoreList>)
    : FirebaseRecyclerAdapter<StoreList, EmployeeAdapterItemRecyclerView.ViewHolder>(options) {

    interface OnItemClickListener {
        fun OnItemClick(store_id:String, store_name:String)
    }

    var itemClickListener: OnItemClickListener?=null

    inner class ViewHolder(val binding: EmployeeFragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.item.setOnClickListener {
                itemClickListener?.OnItemClick(binding.storeId.text.toString(), binding.storeName.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = EmployeeFragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StoreList) {
        holder.binding.apply {
            storeId.text = model.store_id
            storeName.text = model.storeName
        }
    }
}