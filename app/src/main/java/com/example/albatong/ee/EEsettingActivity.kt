package com.example.albatong.ee

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.example.albatong.databinding.ActivityEesettingBinding
import com.example.albatong.databinding.EeSettingConfirmstoreDialogBinding
import com.example.albatong.databinding.ErSettingConfirmstoreDialogBinding
import com.example.albatong.databinding.ErSettingConfirmuserDialogBinding
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employee.EmployeeFragmentStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
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
                showExitDialog(use, storeId!!)
            }

            binding.userdelete.setOnClickListener {
                showDeleteDialog(use, storelist)
            }

            binding.userLogout.setOnClickListener {
                logout()
            }
        }
    }

    private fun showExitDialog(use: DatabaseReference, storeId: String) {
        val dlgBinding = EeSettingConfirmstoreDialogBinding.inflate(layoutInflater)
        val storeBuilder = AlertDialog.Builder(this)
        val storeDlg = storeBuilder.setView(dlgBinding.root).show()

        storeDlg.window?.setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
        storeDlg.window?.setGravity(Gravity.CENTER)
        storeDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            val i = Intent(this, EmployeeActivityMain::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeInfo")
                .child("employee").child(userID.toString()).removeValue()

            FirebaseDatabase.getInstance().getReference("Users").child("employee")
                .child(userID.toString()).child("store").child(storeId).removeValue()

            val calendarRef =
                FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("storeManager").child("calendar")

            calendarRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(a: DataSnapshot) {
                    for (yearSnapshot in a.children) {
                        for (monthSnapshot in yearSnapshot.children) {
                            for (daySnapshot in monthSnapshot.children) {
                                for (snapshot in daySnapshot.children) {
                                    val key = snapshot.key
                                    val employeeId = key?.substringBefore(" : ")
                                    if (employeeId == userID) {
                                        snapshot.ref.removeValue()
                                    }
                                }
                            }
                        }
                    }
                    i.putExtra("user_id", userID)
                    startActivity(i)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
            storeDlg.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener {
            storeDlg.dismiss()
        }
    }

    private fun showDeleteDialog(use: DatabaseReference, storelist: ArrayList<String>) {
        val dlgBinding = ErSettingConfirmuserDialogBinding.inflate(layoutInflater)
        val userBuilder = AlertDialog.Builder(this)
        val userDlg = userBuilder.setView(dlgBinding.root).show()

        userDlg.window?.setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
        userDlg.window?.setGravity(Gravity.CENTER)
        userDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        dlgBinding.registerBtn.setOnClickListener {
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
            userDlg.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener {
            userDlg.dismiss()
        }
    }

    private fun logout() {
        val sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(KEY_WAS_LOGOUT, true)
        editor.putString(KEY_USER_PW_FOR_AUTO_LOGIN, "")
        editor.putString(KEY_USER_ID_FOR_AUTO_LOGIN, "")
        val result = editor.commit()
        if (result) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
