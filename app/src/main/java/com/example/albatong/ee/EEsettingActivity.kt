package com.example.albatong.ee

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.albatong.R
import com.example.albatong.databinding.ActivityEesettingBinding
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.childEvents
import com.google.firebase.database.ktx.snapshots

class EEsettingActivity : AppCompatActivity() {
    lateinit var binding:ActivityEesettingBinding
    var storeId: String?= EmployeeFragmentStoreList.settingStoreId1
    var userID: String?=EmployeeFragmentStoreList.settingUserId1
    val storelist: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEesettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")

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

                        use.child("store").child(storeId.toString()).removeValue()

                        FirebaseDatabase.getInstance().getReference("Stores").child(storeId.toString())
                            .child("storeInfo").child("employee").child(userID.toString()).removeValue()

                        val i = Intent(this, EmployeeActivityMain::class.java)
                        startActivity(i)
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
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            }

            binding.userswitch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked){

                }
                else{

                }
            }

        }
        }


}