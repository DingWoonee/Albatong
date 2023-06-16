package com.example.albatong.ee

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.albatong.R
import com.example.albatong.databinding.ActivityEesettingBinding
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.childEvents
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_ID_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_PW_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_WAS_LOGOUT
import com.example.albatong.login.LoginActivity.Companion.SHARED_PREF_NAME

class EEsettingActivity : AppCompatActivity() {
    lateinit var binding:ActivityEesettingBinding
    var storeId: String?= EmployeeFragmentStoreList.settingStoreId1
    var userID: String?=LoginActivity.uId
    val storelist: ArrayList<String> = ArrayList()
    var check:String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEesettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userID = intent.getStringExtra("user_id")

        var ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")


        var use1 = FirebaseDatabase.getInstance().getReference("Users").child("employee")
            .child(userID.toString())

        use1.get().addOnSuccessListener {
            var name = it.child("name").value.toString() + "("  +it.child("user_id").value.toString()+")"
            var email =  it.child("email").value.toString()
            var tel = it.child("tel").value.toString()

            binding.eeemail.text = email
            binding.eetel.text = tel
            binding.eename.text = name
        }


            ab.get().addOnSuccessListener {
                var test=0
                while(true){
                    if(it.child(test.toString()).exists()){
                        storelist.add(it.child(test.toString()).value.toString())
                        test++
                    }
                    else{
                        break
                    }
                }

                val use = FirebaseDatabase.getInstance().getReference("Users").child("employee")
                    .child(userID.toString())

                binding.userexit.setOnClickListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("나가기")
                        .setMessage("점포를 나가시겠습니까?")
                        .setPositiveButton("나가기",{ dialog, id ->


                            val i = Intent(this, EmployeeActivityMain::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeInfo")
                                .child("employee").child(userID.toString()).removeValue()

                            FirebaseDatabase.getInstance().getReference("Users").child("employee")
                                .child(userID.toString()).child("store").child(storeId.toString()).removeValue()

                            val calendarRef =
                                FirebaseDatabase.getInstance().getReference("Stores").child(storeId!!).child("storeManager").child("calendar")

                            calendarRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                override fun onDataChange(a: DataSnapshot) {
                                    for (yearSnapshot in a.children) {
                                        for (monthSnapshot in yearSnapshot.children) {
                                            for (daySnapshot in monthSnapshot.children) {
                                                for (snapshot in daySnapshot.children) {
                                                    val key =
                                                        snapshot.key
                                                    val employeeId = key?.substringBefore(" : ")
                                                    if (employeeId == userID) {
                                                        snapshot.ref.removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    i.putExtra("user_id",userID)
                                    startActivity(i)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })

                        })
                        .setNegativeButton("취소",{ dialog, id ->

                        })
                    builder.show()
                }

                binding.userdelete.setOnClickListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("회원 탈퇴")
                        .setMessage("회원을 탈퇴하시겠습니까?")
                        .setPositiveButton("나가기",{ dialog, id ->

                            use.removeValue()

                            val a =  FirebaseDatabase.getInstance().getReference("Stores")

                            a.get().addOnSuccessListener {

                                for(i in storelist){
                                    if(it.child(i).child("storeInfo").child("employee").exists()){
                                        a.child(i).child("storeInfo").child("employee").child(userID.toString()).removeValue()
                                    }
                                    val calendarRef =
                                        Firebase.database.getReference("Stores").child(i!!).child("storeManager").child("calendar")

                                    calendarRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(scheduleSnapshot: DataSnapshot) {
                                            for (yearSnapshot in scheduleSnapshot.children) {
                                                for (monthSnapshot in yearSnapshot.children) {
                                                    for (daySnapshot in monthSnapshot.children) {
                                                        for (snapshot in daySnapshot.children) {
                                                            val key =
                                                                snapshot.key
                                                            val employeeId = key?.substringBefore(" : ")
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
                        .setNegativeButton("취소",{ dialog, id ->

                        })
                    builder.show()
                }

                binding.userLogout.setOnClickListener {
                    val sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean(KEY_WAS_LOGOUT, true)
                editor.putString(KEY_USER_PW_FOR_AUTO_LOGIN, "")
                editor.putString(KEY_USER_ID_FOR_AUTO_LOGIN, "")
                val result = editor.commit()
                if (result) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                }

                }


        }
        }





