package com.example.albatong.ee

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.databinding.ActivityDetailBinding
import com.example.albatong.databinding.EeFragmentAnnouncementBinding
import com.google.firebase.database.FirebaseDatabase

class EEFragmentAnnouncement : Fragment() {
    lateinit var binding: EeFragmentAnnouncementBinding
    val data: ArrayList<EEMyData> = ArrayList()
    var data3: ArrayList<EEMyData> = ArrayList()
    var data2: ArrayList<EEMyData> = ArrayList()
    lateinit var adapter: EEAdapterAnnouncement
    var storeId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EeFragmentAnnouncementBinding.inflate(inflater,container,false)

        var i = requireActivity().intent
        storeId = i.getStringExtra("store_id")

        initRecyclerView()
        init()
        return binding.root
    }

    private fun init() {
        val aDB = FirebaseDatabase.getInstance().getReference("Stores")
            .child(storeId!!).child("storeManager").child("management").child("announcement")
        val aDB2 = FirebaseDatabase.getInstance().getReference("Stores")
            .child(storeId!!).child("storeManager").child("management").child("STARannouncement")

        var test = 0
        var test2 = 0

        aDB2.get().addOnSuccessListener {
            if(it.exists()){
                while(true){
                    if(it.child(test.toString()).exists()){
                        adapter.addItem(EEMyData("사장",it.child(test.toString()).child("date").getValue().toString(),it.child(test.toString()).child("title").getValue().toString(),it.child(test.toString()).child("content").getValue().toString(),it.child(test.toString()).child("check").getValue().toString()))
                        data2.add(EEMyData("사장",it.child(test.toString()).child("date").getValue().toString(),it.child(test.toString()).child("title").getValue().toString(),it.child(test.toString()).child("content").getValue().toString(),it.child(test.toString()).child("check").getValue().toString()))
                        test++
                    }
                    else
                        break
                }
            }
        }

        aDB.get().addOnSuccessListener {
            if(it.exists()){
                while(true){
                    if(it.child(test2.toString()).exists()){
                        if(it.child(test2.toString()).child("check").value!="1"){
                            adapter.addItem(EEMyData("사장",it.child(test2.toString()).child("date").getValue()
                                .toString(),it.child(test2.toString()).child("title").getValue().toString(),
                                it.child(test2.toString()).child("content").getValue().toString(),
                                it.child(test2.toString()).child("check").getValue().toString()))

                            data3.add(EEMyData("사장",it.child(test2.toString()).child("date").getValue()
                                .toString(),it.child(test2.toString()).child("title").getValue().toString(),
                                it.child(test2.toString()).child("content").getValue().toString(),
                                it.child(test2.toString()).child("check").getValue().toString()))
                            test2++
                        }
                        else{
                            data3.add(EEMyData("사장",it.child(test2.toString()).child("date").getValue()
                                .toString(),it.child(test2.toString()).child("title").getValue().toString(),
                                it.child(test2.toString()).child("content").getValue().toString(),
                                it.child(test2.toString()).child("check").getValue().toString()))
                            test2++
                        }
                    }
                    else
                        break
                }
            }
        }
    }

    fun initRecyclerView() {
        binding.recyclerview.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        adapter = EEAdapterAnnouncement(data)
        adapter.itemClickListener = object : EEAdapterAnnouncement.OnItemClickListener {
            override fun OnItemClick(data: EEMyData, position: Int) {
                val dlgBinding = ActivityDetailBinding.inflate(layoutInflater)

                dlgBinding.titleTv.text = data.title
                dlgBinding.contentTv.text = data.content
                dlgBinding.dateTv.text = data.date

                val dlgBuilder = AlertDialog.Builder(requireContext())
                val dlg = dlgBuilder.setView(dlgBinding.root).show()
                dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dlg.window?.setGravity(Gravity.TOP)
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dlgBinding.closeBtn.setOnClickListener {
                    dlg.dismiss()
                }
            }

            override fun OnStarClick(data: EEMyData, position: Int) {
            }
        }
        binding.recyclerview.adapter = adapter

    }
}