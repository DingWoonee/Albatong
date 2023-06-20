package com.example.albatong.login

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
import com.example.albatong.data.Store
import com.example.albatong.databinding.ActivitySignAcitivityBinding
import com.example.albatong.databinding.SignDialogBinding
import com.example.albatong.employer.EmployerActivityStoreList
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SignAcitivity : AppCompatActivity() {
    lateinit var binding: ActivitySignAcitivityBinding
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: SignAdapter
    lateinit var recyclerViewDB: DatabaseReference

    var storeId = EmployerActivityStoreList.settingStoreId2
    var userID: String? = null
    var userName: String? = null
    var userType: String ?= null
    var employerID: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var i = intent
        userID = i.getStringExtra("user_id")
        userType = i.getStringExtra("user_type")

        init()
    }

    private fun init() {
        val userDB = FirebaseDatabase.getInstance().getReference("Users")

        userDB.child("employee/${userID!!}/name").get().addOnSuccessListener {
            if(it.exists())
                userName = it.getValue(String::class.java)
        }

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewDB = Firebase.database.getReference("Users/$userType/$userID/Sign")
        val query = recyclerViewDB.limitToLast(50)
        val option = FirebaseRecyclerOptions.Builder<SignData>()
            .setQuery(query, SignData::class.java)
            .build()
        adapter = SignAdapter(option)
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
        binding!!.apply {
            recyclerView2.layoutManager = layoutManager
            recyclerView2.adapter = adapter
        }

    }

    private fun acceptExchange(data: SignData) {
        // 일정 교환
        Firebase.database.getReference("Stores/${data.schedule?.store_id}").get().addOnSuccessListener {
            if (it.exists())
                employerID = it.getValue(Store::class.java)?.storeInfo?.employerId
        }

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
                                                DB.child("$userID : ${s?.startTime}-${s?.endTime}").setValue(
                                                    Schedule(userName!!, s!!.storeName, s.startTime, s.endTime, s.salary, s.store_id)
                                                )
                                                DB.child(n.key!!).setValue(null)

                                                // 알림 삭제
                                                Toast.makeText(this@SignAcitivity, "일정 변경을 완료했습니다.", Toast.LENGTH_SHORT).show()

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

                                                // 사장 알림 추가

                                                Log.i("check", "$employerID")
                                                Firebase.database.getReference("Users/employer/$employerID/Sign").addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            val current = LocalDateTime.now()
                                                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                            val date = current.format(formatter)

                                                            var count = 0
                                                            if(n in snapshot.children) {
                                                                count++
                                                            }
                                                            var msg = "${data.schedule?.storeName}\n일정 변경(${data.selectedDate!!}): $senderName -> $userName"
                                                            Firebase.database.getReference("Users/employer/$employerID/Sign").child(count.toString())
                                                                .setValue(SignData(msg, date, "1", null, null, null))
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
        Toast.makeText(this@SignAcitivity, "교환 요청을 거절했습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening() // 변화 자동 감지
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}