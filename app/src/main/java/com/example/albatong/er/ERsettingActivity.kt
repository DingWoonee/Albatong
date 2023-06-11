package com.example.albatong.er

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.albatong.R
import com.example.albatong.databinding.ActivityErsettingBinding
import com.example.albatong.ee.EEActivitySpecificMain
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.FirebaseDatabase

class ERsettingActivity : AppCompatActivity() {
    lateinit var binding: ActivityErsettingBinding
    var userID: String? = EmployerActivityStoreList.settingUserId2
    var storeId: String? = EmployerActivityStoreList.settingStoreId2
    val storelist: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErsettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")

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

            val use = FirebaseDatabase.getInstance().getReference("Users").child("employer")
                .child(userID.toString())
            val use1 = FirebaseDatabase.getInstance().getReference("Stores")

            binding.erexit.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원 탈퇴")
                    .setMessage("회원을 탈퇴하시겠습니까?")
                    .setPositiveButton("나가기", { dialog, id ->
                        use.removeValue()

                        val a =  FirebaseDatabase.getInstance().getReference("Stores")

                        a.get().addOnSuccessListener {
                            for(i in storelist){
                                if(it.child(i).child("storeInfo").child("employerId").child(storeId.toString()).exists()){
                                    a.child(i).removeValue()
                                }
                            }
                            val i = Intent(this, LoginActivity::class.java)
                            startActivity(i)
                        }
                    })
                    .setNegativeButton("취소", { dialog, id ->

                    })
                builder.show()
            }

            binding.erdelete.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("점포 삭제")
                    .setMessage("점포를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", { dialog, id ->
                        use.child("store").child(storeId.toString()).removeValue()

                        // 점포 안 직원들 삭제 기능


                        val i = Intent(this, EmployeeActivityMain::class.java)
                        startActivity(i)
                    })
                    .setNegativeButton("취소", { dialog, id ->

                    })
                builder.show()
            }

            binding.erLogout.setOnClickListener {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            }

            binding.erswitch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {

                } else {

                }
            }


        }
    }
}