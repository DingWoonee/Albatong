package com.example.albatong.employer

import android.app.AlertDialog
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.example.albatong.databinding.ActivityEmployersettingBinding
import com.example.albatong.databinding.ErSettingConfirmuserDialogBinding
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.FirebaseDatabase
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_ID_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_PW_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_WAS_LOGOUT
import com.example.albatong.login.LoginActivity.Companion.SHARED_PREF_NAME
import com.google.firebase.database.DatabaseReference

class Employersetting : AppCompatActivity() {
    lateinit var binding: ActivityEmployersettingBinding
    var userID: String? = LoginActivity.uId
    var storeId: String? = EmployerActivityStoreList.settingStoreId2
    val storelist: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployersettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = intent.getStringExtra("user_id")

        var use1 = FirebaseDatabase.getInstance().getReference("Users").child("employer")
            .child(userID.toString())

        use1.get().addOnSuccessListener {
            var name = it.child("name").value.toString() + "("  +it.child("user_id").value.toString()+")"
            var email =  it.child("email").value.toString()
            var tel = it.child("tel").value.toString()

            binding.eeemail.text = email
            binding.eetel.text = tel
            binding.eename.text = name
        }


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

            val use = FirebaseDatabase.getInstance().getReference("Users").child("employer").child(userID.toString())


            binding.erexit.setOnClickListener {
                showDeletDialog(use, storelist)
            }

            binding.erLogout.setOnClickListener {
                logout()
            }
        }
    }

    private fun logout() {
        val sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
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

    private fun showDeletDialog(use: DatabaseReference, storelist: ArrayList<String>) {
        val dlgBinding = ErSettingConfirmuserDialogBinding.inflate(layoutInflater)
        val userBuilder = AlertDialog.Builder(this)
        val userDlg = userBuilder.setView(dlgBinding.root).show()

        userDlg.window?.setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
        userDlg.window?.setGravity(Gravity.CENTER)
        userDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            use.removeValue()

            val a = FirebaseDatabase.getInstance().getReference("Stores")

            a.get().addOnSuccessListener {
                for (i in storelist) {
                    if (it.child(i).child("storeInfo").child("employerId").child(storeId.toString()).exists()) {
                        a.child(i).removeValue()
                    }
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
}