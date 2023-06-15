package com.example.albatong.er

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.content.Intent
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
import com.example.albatong.data.SignData
import com.example.albatong.databinding.EeFragmentTransferBinding
import com.example.albatong.databinding.ActivityEereBinding
import com.example.albatong.databinding.ActivityDetailBinding
import com.example.albatong.ee.EEMyData
import com.example.albatong.ee.EEAdapterAnnouncement
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.login.LoginActivity
import com.example.albatong.login.SignAcitivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ERFragmentAnnouncement : Fragment() {
    lateinit var binding: EeFragmentTransferBinding
    var data: ArrayList<EEMyData> = ArrayList()
    var data2: ArrayList<EEMyData> = ArrayList()
    var data3: ArrayList<EEMyData> = ArrayList()
    var data5: ArrayList<EEMyData> = ArrayList()
    var SigndataE: ArrayList<SignData> = ArrayList()
    var SigndataR: ArrayList<SignData> = ArrayList()
    lateinit var adapter: EEAdapterAnnouncement
    var storeId:String? = "null"
    var userId:String? = LoginActivity.uId

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addItemBtn1.setOnClickListener{
            showDialog()
        }
    }

    private fun showDialog() {
        val dlgBinding = ActivityEereBinding.inflate(layoutInflater)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val Data = current.format(formatter)

        val dlgBuilder = AlertDialog.Builder(requireContext())
        val dlg = dlgBuilder.setView(dlgBinding.root).show()

        dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setGravity(Gravity.BOTTOM)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            run {
                adapter.addItem(
                    EEMyData(
                        "사장",
                        Data.toString(),
                        dlgBinding.titleEt.text.toString(),
                        dlgBinding.contentEt.text.toString(),
                        "0"
                    )
                )

                data3.add(EEMyData(
                    "사장",
                    Data.toString(),
                    dlgBinding.titleEt.text.toString(),
                    dlgBinding.contentEt.text.toString(),
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


                val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management").child("announcement")
                aDB.setValue(data3)
            }
        }

        dlgBinding.cancelBtn.setOnClickListener {
            dlg.dismiss()
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
                dlgBinding.removeBtn.visibility = View.VISIBLE

                val dlgBuilder = AlertDialog.Builder(requireContext())
                val dlg = dlgBuilder.setView(dlgBinding.root).show()
                dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dlg.window?.setGravity(Gravity.TOP)
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dlgBinding.closeBtn.setOnClickListener {
                    dlg.dismiss()
                }
                dlgBinding.removeBtn.setOnClickListener {
                    run {
                        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management").child("announcement")

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
                            FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management")
                                .child("STARannouncement").setValue(data2)
                        }
                    }
                    dlg.dismiss()
                }
            }

            override fun OnStarClick(data4: EEMyData, position: Int) {
                if(data4.check=="1"){
                    data4.check="0"

                    data2.removeAt(position)

                    FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management")
                        .child("STARannouncement").setValue(data2)

                    var db = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management")
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

                    FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management")
                        .child("STARannouncement").setValue(data2)

                    var db = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("management")
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

                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
                        val Date = current.format(formatter)

                        Toast.makeText(context,"중요공지가 등록되었습니다.",Toast.LENGTH_SHORT).show()

                        FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeInfo").child("employee")
                            .addValueEventListener(object:ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                    for(i in snapshot.children){

                                        FirebaseDatabase.getInstance().getReference("Users").child("employee").child(i.key.toString())
                                            .child("Sign").addListenerForSingleValueEvent(object:ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var count = 0
                                                    for(i1 in snapshot.children){
                                                        count++
                                                    }
                                                    if(count!=0){
                                                        FirebaseDatabase.getInstance().getReference("Users").child("employee").child(i.key.toString())
                                                            .child("Sign").get().addOnSuccessListener {
                                                                for(i in 0..count-1){
                                                                    var title =   it.child(i.toString()).child("title").getValue().toString()
                                                                    var date =   it.child(i.toString()).child("date").getValue().toString()
                                                                    var type =   it.child(i.toString()).child("type").getValue().toString()
                                                                    SigndataE.add(SignData(title,date,type))
                                                                }
                                                                SigndataE.add(SignData("중요공지가 등록되었습니다.",Date, "1"))

                                                                FirebaseDatabase.getInstance().getReference("Users").child("employee").child(i.key.toString())
                                                                    .child("Sign").setValue(SigndataE)

                                                                SigndataE.clear()
                                                            }
                                                    }
                                                    else{
                                                        SigndataE.add(SignData("중요공지가 등록되었습니다.",Date, "1"))

                                                        FirebaseDatabase.getInstance().getReference("Users").child("employee").child(i.key.toString())
                                                            .child("Sign").setValue(SigndataE)

                                                        SigndataE.clear()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }

                                            })

                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                        FirebaseDatabase.getInstance().getReference("Users").child("employer").child(userId.toString())
                            .child("Sign").addListenerForSingleValueEvent(object:ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var count = 0
                                    for(i in snapshot.children){
                                        count++
                                    }
                                    if(count!=0){
                                        FirebaseDatabase.getInstance().getReference("Users").child("employer").child(userId.toString())
                                            .child("Sign").get().addOnSuccessListener {
                                                for(i in 0..count-1){
                                                    var title =   it.child(i.toString()).child("title").getValue().toString()
                                                    var date =   it.child(i.toString()).child("date").getValue().toString()
                                                    var type =   it.child(i.toString()).child("type").getValue().toString()
                                                    SigndataR.add(SignData(title,date,type))
                                                }
                                                SigndataR.add(SignData("중요공지를 등록하였습니다.",Date,"1"))

                                                FirebaseDatabase.getInstance().getReference("Users").child("employer").child(userId.toString())
                                                    .child("Sign").setValue(SigndataR)
                                                SigndataR.clear()
                                            }
                                    }
                                    else{
                                        SigndataR.add(SignData("중요공지를 등록하였습니다.",Date,"1"))

                                        FirebaseDatabase.getInstance().getReference("Users").child("employer").child(userId.toString())
                                            .child("Sign").setValue(SigndataR)
                                        SigndataR.clear()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })

                        adapter.notifyDataSetChanged()

                    }
                }



            }
        }
        binding.recyclerview.adapter = adapter

    }
}