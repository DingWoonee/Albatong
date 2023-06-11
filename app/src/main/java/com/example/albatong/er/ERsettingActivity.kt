package com.example.albatong.er

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.albatong.R
import com.example.albatong.databinding.ActivityErsettingBinding
import com.example.albatong.ee.EEActivitySpecificMain
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
            val User = FirebaseDatabase.getInstance().getReference("Users").child("employee")
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

                        use1.get().addOnSuccessListener {
                                if(it.child(storeId.toString()).child("storeInfo").child("employee").exists()){
                                    var use2 = use1.child(storeId.toString()).child("storeInfo").child("employee")
                                    use2.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(employee: DataSnapshot) {
                                            for (ee in employee.children) {
                                                User.child(ee.key.toString()).child("store").child(storeId.toString()).removeValue()
                                                Log.i("ee",ee.key.toString())
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                        }
                                    })
                                }

                        }

                        val i = Intent(this, LoginActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        i.putExtra("user_id",userID)
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