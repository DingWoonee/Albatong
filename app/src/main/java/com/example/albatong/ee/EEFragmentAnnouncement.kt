package com.example.albatong.ee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.R
import com.example.albatong.databinding.EeFragmentAnnouncementBinding
import com.example.albatong.databinding.EeFragmentStoreListBinding
import com.example.albatong.databinding.EeFragmentTransferBinding
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

class EEFragmentAnnouncement : Fragment() {
    lateinit var binding: EeFragmentAnnouncementBinding
    val data: ArrayList<EEMyData> = ArrayList()
    var data3: ArrayList<EEMyData> = ArrayList()
    var data2: ArrayList<EEMyData> = ArrayList()
    lateinit var adapter: EEMyDataAdapter
    var storeId:String? = "null"

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun initRecyclerView() {
        binding.recyclerview.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        adapter = EEMyDataAdapter(data)
        adapter.itemClickListener = object : EEMyDataAdapter.OnItemClickListener {
            override fun OnItemClick(data: EEMyData, position: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("공지사항")

                var inflater = layoutInflater
                val dialogView = inflater.inflate(R.layout.activity_detail,null)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.title_tv)
                val dialogContent = dialogView.findViewById<TextView>(R.id.content_tv)
                val dialogDate = dialogView.findViewById<TextView>(R.id.date_tv)

                dialogTitle.text = data.title
                dialogContent.text = data.content
                dialogDate.text = data.date

                builder.setView(dialogView)
                builder.setCancelable(false)

                builder.setPositiveButton("확인"){
                        p0,p1->{


                }

                }
                val alertDialog = builder.create()
                alertDialog.show()
                alertDialog.window?.setLayout(1000,1800)
            }

            override fun OnStarClick(data: EEMyData, position: Int) {
                TODO("Not yet implemented")
            }
        }
        binding.recyclerview.adapter = adapter

    }
}