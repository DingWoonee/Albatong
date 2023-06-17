package com.example.albatong.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.Schedule
import com.example.albatong.data.SignData
import com.example.albatong.databinding.ActivitySignAcitivityBinding
import com.example.albatong.databinding.SignDialogBinding
import com.example.albatong.employer.EmployerActivityStoreList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignAcitivity : AppCompatActivity() {
    lateinit var binding: ActivitySignAcitivityBinding
    lateinit var adapter: SignAdapter
    var notificationList: ArrayList<SignData> = ArrayList()

    var storeId = EmployerActivityStoreList.settingStoreId2
    var userId = LoginActivity.uId
    var userID: String? = null
    var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var i = intent
        userID = i.getStringExtra("user_id")

        initRecyclerView()
        init()
    }

    private fun init() {
        adapter.items.clear()

        val userDB = FirebaseDatabase.getInstance().getReference("Users")

        userDB.child("employee/${userID!!}/name").get().addOnSuccessListener {
            if(it.exists())
                userName = it.getValue(String::class.java)
        }

        // employee
        userDB.child("employee").child(userID!!).child("Sign").addListenerForSingleValueEvent (
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(n in snapshot.children)
                        notificationList.add(0, n.getValue(SignData::class.java)!!)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )

        // employer
        userDB.child("employer/${userID!!}/name").get().addOnSuccessListener {
            if(it.exists())
                userName = it.getValue(String::class.java)
        }

        userDB.child("employer").child(userID!!).child("Sign").addListenerForSingleValueEvent (
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(n in snapshot.children)
                        notificationList.add(0, n.getValue(SignData::class.java)!!)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )


    }

    private fun initRecyclerView() {
        binding.recyclerView2.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        adapter = SignAdapter(notificationList)
        adapter.itemClickListener = object : SignAdapter.OnItemClickListener {
            override fun OnItemClick(data: SignData, position: Int) {
                if(data.type == "2") {
                    val dlgBinding = SignDialogBinding.inflate(layoutInflater)

                    dlgBinding.storeName.text = data.schedule?.storeName
                    dlgBinding.date.text = "${data.selectedDate}"
                    dlgBinding.time.text = "${data.schedule?.startTime} - ${data.schedule?.endTime}"
                    dlgBinding.dayOfWeek.text = data.dayOfWeek

                    val dlgBuilder = AlertDialog.Builder(this@SignAcitivity)
                    val dlg = dlgBuilder.setView(dlgBinding.root).show()
                    dlg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    dlg.window?.setGravity(Gravity.BOTTOM)
                    dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    dlgBinding.closeBtn.setOnClickListener {
                        dlg.dismiss()
                    }
                    dlgBinding.acceptBtn.setOnClickListener {
                        acceptExchange(data)
                        dlg.dismiss()
                    }
                    dlgBinding.rejectBtn.setOnClickListener {
                        rejectExchange(data)
                        dlg.dismiss()
                    }
                }
            }
        }
        binding.recyclerView2.adapter = adapter
    }

    private fun acceptExchange(data: SignData) {
        // 일정 교환
        val requestDB = Firebase.database.getReference("Stores/${data.schedule?.store_id}/storeManager")
        requestDB.child("request").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()) {
                        Toast.makeText(this@SignAcitivity, "이미 변경된 일정입니다.", Toast.LENGTH_SHORT).show()
                    }
                    for(request in snapshot.children) {
                        var senderName = request.child("senderName").getValue(String::class.java)
                        var receiver = request.child("receiver").getValue(String::class.java)
                        var storedSchedule = request.child("schedule").getValue(Schedule::class.java)
                        if(senderName == data.schedule?.name && (receiver == userID || receiver == "all")
                            && storedSchedule?.startTime == data.schedule?.startTime && storedSchedule?.endTime == data.schedule?.endTime) {

                            var year = data.selectedDate?.split("-")?.get(0)
                            var month = data.selectedDate?.split("-")?.get(1)
                            var day = data.selectedDate?.split("-")?.get(2)

                            // 교환
                            var DB = Firebase.database.getReference("Stores/${data.schedule?.store_id}/storeManager/calendar")
                                .child(year+"년/"+month+"월/"+day+"일")
                            DB.addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(n in snapshot.children) {
                                            var s = n.getValue(Schedule::class.java)
                                            if(s?.name == senderName && s?.startTime == storedSchedule?.startTime && s?.endTime == storedSchedule?.endTime) {
                                                DB.child("${userID} : ${s?.startTime}-${s?.endTime}").setValue(
                                                    Schedule(userName!!, s!!.storeName, s.startTime, s.endTime, s.salary, s.store_id)
                                                )
                                                DB.child(n.key!!).setValue(null)

                                                // 알림 삭제
                                                Toast.makeText(this@SignAcitivity, "일정 변경 완료", Toast.LENGTH_SHORT).show()

                                                Firebase.database.getReference("Users/employee").addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            for(employee in snapshot.children) {
                                                                Firebase.database.getReference("Users/employee/${employee.key}/Sign/${data.schedule?.store_id}: ${data.selectedDate!!} ${s.startTime}-${s.endTime}").setValue(null)
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {

                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                }
                            )

                            requestDB.child("request").child(request.key!!).setValue(null)
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )
    }

    private fun rejectExchange(data: SignData) {
        // 알림 제거
        Firebase.database.getReference("Users/employee/${userID}/Sign/${data.schedule?.store_id}: ${data.selectedDate!!} ${data.schedule?.startTime}-${data.schedule?.endTime}").setValue(null)
    }
}