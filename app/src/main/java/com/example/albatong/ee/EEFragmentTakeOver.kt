package com.example.albatong.ee

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.databinding.ActivityDetailBinding
import com.example.albatong.databinding.ActivityEereBinding
import com.example.albatong.databinding.EeFragmentTransferBinding
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class EEFragmentTakeOver : Fragment() {

    lateinit var binding: EeFragmentTransferBinding
    val data: ArrayList<EEMyData> = ArrayList()
    lateinit var adapter: EEAdapterTakeOver
    var userID: String?=null
    var userName:String = ""
    var storeId: String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EeFragmentTransferBinding.inflate(inflater,container,false)

        var i = requireActivity().intent
        userID = i.getStringExtra("user_id")
        storeId = i.getStringExtra("store_id")

        initRecyclerView()
        init()

        return binding.root
    }

    private fun init() {
        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!)
            .child("storeManager").child("management").child("task")
        val use = FirebaseDatabase.getInstance().getReference("Users").child("employee")
        var test = 0

        use.get().addOnSuccessListener {
            if(it.exists()){
                userName = it.child(userID!!).child("name").getValue().toString()
            }
        }

        aDB.get().addOnSuccessListener {
            if(it.exists()){
                while(true){
                    if(it.child(test.toString()).exists()){
                        adapter.addItem(EEMyData(it.child(test.toString()).child("userid").getValue().toString(),
                            it.child(test.toString()).child("date").getValue().toString(),
                            it.child(test.toString()).child("title").getValue().toString(),
                            it.child(test.toString()).child("content").getValue().toString(),"0"))
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
                        "$userName($userID)",
                        Data.toString(),
                        dlgBinding.titleEt.text.toString(),
                        dlgBinding.contentEt.text.toString(),
                        "0"
                    )
                )
                var b:EEMyData = data[0]
                var c:EEMyData

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
            dlg.dismiss()
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
        adapter = EEAdapterTakeOver(data)
        adapter.itemClickListener = object : EEAdapterTakeOver.OnItemClickListener {
            override fun OnItemClick(data: EEMyData, position: Int) {
                val dlgBinding = ActivityDetailBinding.inflate(layoutInflater)

                dlgBinding.titleTv.text = data.title
                dlgBinding.contentTv.text = data.content
                dlgBinding.dateTv.text = data.date
                dlgBinding.type.text = "인수인계"

                if("$userName($userID)"==data.userid)
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
                        val aDB = FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!)
                            .child("storeManager").child("management").child("task")
                        adapter.removeItem(position)

                        var test = position

                        aDB.get().addOnSuccessListener {
                            if(it.exists()){
                                while(true){
                                    if(it.child((test+1).toString()).exists()){
                                        aDB.child(test.toString()).child("userid").setValue(it.child((test+1).toString()).child("userid").getValue().toString())
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
                    dlg.dismiss()
                }
            }
        }
        binding.recyclerview.adapter = adapter

    }

}

