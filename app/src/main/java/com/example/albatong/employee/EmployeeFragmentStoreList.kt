package com.example.albatong.employee

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.*
import com.example.albatong.databinding.EmployeeDialogAddBinding
import com.example.albatong.databinding.EmployeeFragmentItemListBinding
import com.example.albatong.ee.EEActivitySpecificMain
import com.example.albatong.ee.EEsettingActivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class EmployeeFragmentStoreList : Fragment() {

    var binding: EmployeeFragmentItemListBinding?= null
    var adapter: EmployeeAdapterItemRecyclerView?= null
    var employeeDB: DatabaseReference?=null
    var user: UserData?=null
    var userID: String?=null

    companion object{
        var settingUserId1:String? = null
        var settingStoreId1:String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EmployeeFragmentItemListBinding.inflate(layoutInflater, container, false)

        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = requireActivity().intent.getStringExtra("user_id")

        employeeDB = Firebase.database.getReference("Users/employee/$userID")

        binding!!.apply {
            list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val query = employeeDB!!.child("store").limitToLast(50)
            val option = FirebaseRecyclerOptions.Builder<StoreList>()
                .setQuery(query, StoreList::class.java)
                .build()
            adapter = EmployeeAdapterItemRecyclerView(option)
            adapter!!.itemClickListener = object :
                EmployeeAdapterItemRecyclerView.OnItemClickListener {
                override fun OnItemClick(store_id:String, store_name:String) {
                    binding.apply {
                        val i = Intent(requireActivity(), EEActivitySpecificMain::class.java)
                        settingStoreId1 = store_id
                        settingUserId1 = userID
                        i.putExtra("store_id", store_id)
                        i.putExtra("store_name", store_name)
                        i.putExtra("user_id", userID)
                        startActivity(i)
                    }
                }
            }
            list.adapter = adapter

            // 알바 추가
            addItemBtn.setOnClickListener {
                addPartTime()
            }
        }
    }

    private fun addPartTime() {
        val dlgBinding = EmployeeDialogAddBinding.inflate(layoutInflater)

        val dlgBuilder = AlertDialog.Builder(context)
        val dlg = dlgBuilder.setView(dlgBinding.root).show()

        dlg.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setGravity(Gravity.CENTER)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            val code = dlgBinding.code.text.toString()

            // 이미 등록된 가게인지 check
            employeeDB!!.child("store/$code").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        Toast.makeText(context, "이미 등록된 알바입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 알바통에 등록된 가게인지 check
                        checkStoreDB(code)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Employee", "Database read error: " + error.message)
                }
            })

            dlg.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener{
            dlg.dismiss()

        }
    }

    private fun checkStoreDB(code: String) {
        val storeDB = Firebase.database.getReference("Stores")
        storeDB.child("$code").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val store = snapshot.getValue(Store::class.java)!!

                    // DB 등록
                    employeeDB!!.child("store").child(code).setValue(StoreList(store.storeInfo.storeName, code, generateColor()))
                    storeDB.child("$code/storeInfo/employee").child("$userID").setValue(Employee("$userID", "${user?.name}"))
                    Toast.makeText(context, "${store.storeInfo.storeName} 알바 등록됨", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "등록되지 않은 알바입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Employee", "Database read error: " + error.message)
            }
        })
    }

    fun generateColor(): Int {
        val random = java.util.Random()
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)
        val color = String.format("%02X%02X%02X", red, green, blue)

        return Color.parseColor("#" + color)
    }

    override fun onResume() {
        super.onResume()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}