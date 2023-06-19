package com.example.albatong.er

import android.app.AlertDialog
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import com.example.albatong.databinding.ActivityErsettingBinding
import com.example.albatong.databinding.ErSettingConfirmstoreDialogBinding
import com.example.albatong.databinding.ErSettingConfirmuserDialogBinding
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.login.LoginActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_ID_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_PW_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_WAS_LOGOUT
import com.example.albatong.login.LoginActivity.Companion.SHARED_PREF_NAME
import com.google.firebase.database.DatabaseReference

class ERsettingActivity : AppCompatActivity() {
    lateinit var binding: ActivityErsettingBinding
    private lateinit var ab : DatabaseReference
    private lateinit var use1 : DatabaseReference

    var userID: String? = LoginActivity.uId
    var storeId: String? = EmployerActivityStoreList.settingStoreId2
    val storelist: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErsettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = intent.getStringExtra("user_id")

        use1 = FirebaseDatabase.getInstance().getReference("Users").child("employer")
            .child(userID.toString())

        use1.get().addOnSuccessListener {
            var name = it.child("name").value.toString() + "("  +it.child("user_id").value.toString()+")"
            var email =  it.child("email").value.toString()
            var tel = it.child("tel").value.toString()

            binding.eremail.text = email
            binding.ertel.text = tel
            binding.ername.text = name
        }

        ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")

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
            val User = FirebaseDatabase.getInstance().getReference("Users").child("employee")
            val use1 = FirebaseDatabase.getInstance().getReference("Stores")

            binding.erexit.setOnClickListener {
                showExitDialog(use, storelist)
            }

            binding.erdelete.setOnClickListener {
                showDeleteDialog(use, User, use1, storeId.toString())
            }

            binding.erLogout.setOnClickListener {
                logout()
            }
        }
    }

    private fun showExitDialog(use: DatabaseReference, storelist: ArrayList<String>) {
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

    private fun showDeleteDialog(use: DatabaseReference, userRef: DatabaseReference, storeRef: DatabaseReference, storeId: String) {
        val dlgBinding = ErSettingConfirmstoreDialogBinding.inflate(layoutInflater)
        val storeBuilder = AlertDialog.Builder(this)
        val storeDlg = storeBuilder.setView(dlgBinding.root).show()

        storeDlg.window?.setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
        storeDlg.window?.setGravity(Gravity.CENTER)
        storeDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            use.child("store").child(storeId).removeValue()

            storeRef.get().addOnSuccessListener {
                if (it.child(storeId).child("storeInfo").child("employee").exists()) {
                    val use2 = storeRef.child(storeId).child("storeInfo").child("employee")
                    use2.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(employee: DataSnapshot) {
                            for (ee in employee.children) {
                                userRef.child(ee.key.toString()).child("store").child(storeId).removeValue()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }

            storeRef.child(storeId).removeValue()

            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            i.putExtra("user_id", userID)
            startActivity(i)

            storeDlg.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener {
            storeDlg.dismiss()
        }
    }

    private fun logout() {
        val sharedPref = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
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
