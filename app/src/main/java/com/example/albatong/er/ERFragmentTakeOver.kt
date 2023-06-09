package com.example.albatong.er

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
import com.example.albatong.databinding.EeFragmentTransferBinding
import com.example.albatong.ee.EEMyData
import com.example.albatong.ee.EEMyDataAdapter
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

class ERFragmentTakeOver : Fragment() {
    lateinit var binding: EeFragmentTransferBinding
    val data: ArrayList<EEMyData> = ArrayList()
    lateinit var adapter: EEMyDataAdapter
    var storeId: String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EeFragmentTransferBinding.inflate(inflater,container,false)

        var i = requireActivity().intent
        storeId = i.getStringExtra("store_id")

        initRecyclerView()
        init()

        return binding.root
    }

    private fun init() {
        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!)
            .child("storeManager").child("management").child("task")
        var test = 0

        aDB.get().addOnSuccessListener {
            if(it.exists()){
                while(true){
                    if(it.child(test.toString()).exists()){
                        adapter.addItem(EEMyData("1",it.child(test.toString()).child("date").getValue().toString(),
                            it.child(test.toString()).child("title").getValue().toString(),
                            it.child(test.toString()).child("content").getValue().toString()))
                        test++
                    }
                    else
                        break
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addItemBtn1.setOnClickListener{
            showDialog()
        }

    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("글 작성")

        var inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_eere,null)

        val dialogTitle = dialogView.findViewById<EditText>(R.id.title_et)
        val dialogContent = dialogView.findViewById<EditText>(R.id.content_et)
        val Data = LocalDate.now()

        builder.setView(dialogView)
        builder.setCancelable(false)

        builder.setPositiveButton("등록"){
                p0,p1->
            run {
                adapter.addItem(
                    EEMyData(
                        "1",
                        Data.toString(),
                        dialogTitle.text.toString(),
                        dialogContent.text.toString()
                    )
                )
                var b: EEMyData = data[0]
                var c: EEMyData

                if(data.size>1){
                    data[0] = data[data.size-1]
                    c = data[1]
                    data[1] = b
                    for(i in data.size-2 downTo 1){
                        if(i==1)
                            data[2] = c
                        else
                            data[i+1] = data[i]
                    }

                }

                val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!)
                    .child("storeManager").child("management").child("task")
                aDB.setValue(data)
            }

        }
        builder.setNegativeButton("취소"){
                p0,p1 ->{

        }
        }
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.window?.setLayout(1000,1800)
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
                builder.setTitle("인수인계")

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
                builder.setNegativeButton("삭제"){
                        p0,p1 ->
                    run {
                        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!)
                            .child("storeManager").child("management").child("task")
                        adapter.removeItem(position)

                        var test = position

                        aDB.get().addOnSuccessListener {
                            if(it.exists()){
                                while(true){
                                    if(it.child((test+1).toString()).exists()){
                                        aDB.child(test.toString()).child("content").setValue( it.child((test+1).toString()).child("content").getValue().toString())
                                        aDB.child(test.toString()).child("date").setValue( it.child((test+1).toString()).child("date").getValue().toString())
                                        aDB.child(test.toString()).child("title").setValue( it.child((test+1).toString()).child("title").getValue().toString())
                                        test++
                                    }
                                    else{
                                        aDB.child(test.toString()).removeValue()
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
                val alertDialog = builder.create()
                alertDialog.show()
                alertDialog.window?.setLayout(1000,1800)
            }
        }
        binding.recyclerview.adapter = adapter

    }
}