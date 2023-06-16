package com.example.albatong.employer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.albatong.databinding.ActivityEmployersettingBinding
import com.example.albatong.login.LoginActivity
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_ID_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_PW_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_WAS_LOGOUT
import com.example.albatong.login.LoginActivity.Companion.SHARED_PREF_NAME
import com.google.firebase.database.FirebaseDatabase

class Employersetting : AppCompatActivity() {
    lateinit var binding: ActivityEmployersettingBinding
    var userID: String? = EmployerActivityStoreList.settingUserId2
    var storeId: String? = EmployerActivityStoreList.settingStoreId2
    val storelist: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployersettingBinding.inflate(layoutInflater)
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



            binding.erLogout.setOnClickListener {
                val sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
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

            binding.erswitch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {

                } else {

                }
            }


        }
    }
}