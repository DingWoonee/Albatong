package com.example.albatong.er

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.EeFragmentTransferBinding
import com.example.albatong.ee.EEMyData
import com.example.albatong.ee.EEMyDataAdapter
import com.example.albatong.employer.EmployerActivityStoreList
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ERFragmentAnnouncement : Fragment() {


    lateinit var binding: EeFragmentTransferBinding
    var data: ArrayList<EEMyData> = ArrayList()
    var data2: ArrayList<EEMyData> = ArrayList()
    var data3: ArrayList<EEMyData> = ArrayList()
    var data5: ArrayList<EEMyData> = ArrayList()
    lateinit var adapter: EEMyDataAdapter
    var storeId = EmployerActivityStoreList.storeid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EeFragmentTransferBinding.inflate(inflater,container,false)
        initRecyclerView()
        init()
        return binding.root
    }

    private fun init() {
        val aDB = FirebaseDatabase.getInstance().getReference("Stores")
            .child(storeId).child("storeManager").child("management").child("announcement")
        val aDB2 = FirebaseDatabase.getInstance().getReference("Stores")
            .child(storeId).child("storeManager").child("management").child("STARannouncement")

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
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd\nHH:mm:ss")
        val Data = current.format(formatter)

        builder.setView(dialogView)
        builder.setCancelable(false)

        builder.setPositiveButton("등록"){
                p0,p1->
            run {
                adapter.addItem(
                    EEMyData(
                        "사장",
                        Data.toString(),
                        dialogTitle.text.toString(),
                        dialogContent.text.toString(),
                        "0"
                    )
                )

//                var b: EEMyData = data[0]
//                var c: EEMyData
//
//                if(data.size>1){
//                    data[0] = data[data.size-1]
//                    c = data[1]
//                    data[1] = b
//                    for(i in data.size-2 downTo 1){
//                        if(i==1)
//                            data[2] = c
//                        else
//                            data[i+1] = data[i]
//                    }
//                }

                data3.add(EEMyData(
                    "사장",
                    Data.toString(),
                    dialogTitle.text.toString(),
                    dialogContent.text.toString(),
                    "0"
                ))

                var b1: EEMyData = data3[0]
                var c1: EEMyData

                if(data3.size>1){
                    data3[0] = data3[data3.size-1]
                    c1 = data3[1]
                    data3[1] = b1
                    for(i in data3.size-2 downTo 1){
                        if(i==1)
                            data3[2] = c1
                        else
                            data3[i+1] = data3[i]
                    }
                }

                data5.clear()

                for(i in 0..data3.size-1){
                    if(data3[i].check!="1")
                        data5.add(data3[i])
                }

                data.clear()
                data.addAll(data2)
                data.addAll(data5)

                adapter.notifyDataSetChanged()


                val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management").child("announcement")
                aDB.setValue(data3)
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
                builder.setNegativeButton("삭제"){
                        p0,p1 ->
                    run {
                        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management").child("announcement")

                        adapter.removeItem(position)

                        var test = position
                        var test2 = 0

                        aDB.get().addOnSuccessListener {
                            if(it.exists()){
                                while(true){
                                    if(data2[position].date==it.child(test2.toString()).child("date").value){
                                        data3.removeAt(test2)

                                        while(true){
                                            if(it.child((test2+1).toString()).exists()){
                                                aDB.child(test2.toString()).child("content").setValue( it.child((test2+1).toString()).child("content").getValue().toString())
                                                aDB.child(test2.toString()).child("date").setValue( it.child((test2+1).toString()).child("date").getValue().toString())
                                                aDB.child(test2.toString()).child("title").setValue( it.child((test2+1).toString()).child("title").getValue().toString())
                                                aDB.child(test2.toString()).child("check").setValue( it.child((test2+1).toString()).child("check").getValue().toString())
                                                test2++
                                            }
                                            else{
                                                aDB.child((test2.toString())).removeValue()
                                                break
                                            }
                                        }
                                    }
                                    if(!it.child((test2+1).toString()).exists())
                                        break
                                    else{
                                        test2++
                                    }
                                }
                            }
                            data2.removeAt(position)
                            FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management")
                                .child("STARannouncement").setValue(data2)
                        }
                    }
                }
                val alertDialog = builder.create()
                alertDialog.show()
                alertDialog.window?.setLayout(1000,1800)
            }

            override fun OnStarClick(data4: EEMyData, position: Int) {
                if(data4.check=="1"){
                    data4.check="0"

                    data2.removeAt(position)

                    FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management")
                        .child("STARannouncement").setValue(data2)

                    var db = FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management")
                        .child("announcement")
                    var test4 = 0
                    db.get().addOnSuccessListener {
                        if(it.exists()){
                            while(true){
                                if(it.child(test4.toString()).exists()){
                                    if(it.child(test4.toString()).child("date").value == data4.date){
                                        data3[test4].check = "0"
                                        break
                                    }
                                    else
                                        test4++
                                }
                            }
                        }
                        db.child(test4.toString()).child("check").setValue("0")

                        data5.clear()

                        for(i in 0..data3.size-1){
                            if(data3[i].check!="1"){
                                data5.add(data3[i])
                            }
                        }
                        data.clear()
                        data.addAll(data2)
                        data.addAll(data5)

                        adapter.notifyDataSetChanged()

                    }

                }
                else{
                    data4.check="1"

                    data2.add(data4)

                    var b: EEMyData = data2[0]
                    var c: EEMyData

                    if(data2.size>1){
                        data2[0] = data2[data2.size-1]
                        c = data2[1]
                        data2[1] = b
                        for(i in data2.size-2 downTo 1){
                            if(i==1)
                                data2[2] = c
                            else
                                data2[i+1] = data2[i]
                        }
                    }

                    FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management")
                        .child("STARannouncement").setValue(data2)

                    var db = FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("management")
                        .child("announcement")
                    var test3 = 0
                    db.get().addOnSuccessListener {
                        if(it.exists()){
                            while(true){
                                if(it.child(test3.toString()).exists()){
                                    if(it.child(test3.toString()).child("date").value == data4.date){
                                        data3[test3].check = "1"
                                        break
                                    }
                                    else
                                        test3++
                                }
                            }
                        }
                        db.child(test3.toString()).child("check").setValue("1")

                        data5.clear()

                        for(i in 0..data3.size-1){
                            if(data3[i].check!="1"){
                                data5.add(data3[i])
                            }
                        }
                        data.clear()
                        data.addAll(data2)
                        data.addAll(data5)

                        adapter.notifyDataSetChanged()

                    }
                }



            }
        }
        binding.recyclerview.adapter = adapter

    }
}