package com.example.albatong.employee

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.albatong.R
import com.example.albatong.databinding.ActivityEesettingBinding
import com.example.albatong.databinding.ActivityEmployeesettingBinding
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Employeesetting : AppCompatActivity() {
    lateinit var binding: ActivityEmployeesettingBinding
    var storeId: String?= EmployeeFragmentStoreList.settingStoreId1
    var userID: String?=EmployeeFragmentStoreList.settingUserId1
    val storelist: ArrayList<String> = ArrayList()
    var check:String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeesettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")
        var ab1 = FirebaseDatabase.getInstance().getReference("Users").child("employee")
            .child(userID.toString())

        ab1.get().addOnSuccessListener {
            if (!it.child("SignCheck").exists()) {
                ab1.child("SignCheck").setValue("1")
            }
            check = it.child("SignCheck").value.toString()
            if(check=="1")
                binding.userswitch.setEnabled(true)
            else
                binding.userswitch.setEnabled(false)

            ab.get().addOnSuccessListener {
                var test = 0
                while (true) {
                    if (it.child(test.toString()).exists()) {
                        storelist.add(it.child(test.toString()).value.toString())
                        test++
                    } else {
                        break
                    }
                }

                val use = FirebaseDatabase.getInstance().getReference("Users").child("employee")
                    .child(userID.toString())


                binding.userdelete.setOnClickListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("회원 탈퇴")
                        .setMessage("회원을 탈퇴하시겠습니까?")
                        .setPositiveButton("나가기", { dialog, id ->

                            use.removeValue()

                            val a = FirebaseDatabase.getInstance().getReference("Stores")

                            a.get().addOnSuccessListener {
                                for (i in storelist) {
                                    if (it.child(i).child("storeInfo").child("employee").exists()) {
                                        a.child(i).child("storeInfo").child("employee")
                                            .child(userID.toString()).removeValue()
                                    }
                                    val calendarRef =
                                        Firebase.database.getReference("Stores").child(i!!)
                                            .child("storeManager").child("calendar")

                                    calendarRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(scheduleSnapshot: DataSnapshot) {
                                            for (yearSnapshot in scheduleSnapshot.children) {
                                                for (monthSnapshot in yearSnapshot.children) {
                                                    for (daySnapshot in monthSnapshot.children) {
                                                        for (snapshot in daySnapshot.children) {
                                                            val key =
                                                                snapshot.key
                                                            val employeeId =
                                                                key?.substringBefore(" : ")
                                                            if (employeeId == userID) {
                                                                snapshot.ref.removeValue()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }
                                    })

                                }
                                val i = Intent(this, LoginActivity::class.java)
                                startActivity(i)
                            }
                        })
                        .setNegativeButton("취소", { dialog, id ->

                        })
                    builder.show()
                }

                binding.userLogout.setOnClickListener {
                    val i = Intent(this, LoginActivity::class.java)
                    startActivity(i)
                }

                binding.userswitch.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if(check=="1"){
                        ab1.child("SignCheck").setValue("0")
                        binding.userswitch.setEnabled(false)
                    }
                    else{
                        ab1.child("SignCheck").setValue("1")
                        binding.userswitch.setEnabled(true)
                    }
                }

            }
        }
    }
}