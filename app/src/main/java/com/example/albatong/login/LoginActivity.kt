package com.example.albatong.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION_CODES.P
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.albatong.databinding.LoginActivityBinding
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.employee.EmployeeActivityMain
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    lateinit var binding: LoginActivityBinding
    lateinit var rdb: DatabaseReference
    var loginResult :Int = 0
    lateinit var sharedPref:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkLoginData()
    }

    fun checkLoginData() {
        binding.loginPwInputEditText.setText("")

        val idSaveChecked = sharedPref.getBoolean("IDCheckBoxState", false)
        binding.idStoreCheckBox.isChecked = idSaveChecked
        if(idSaveChecked) {
            binding.loginIdInputEditText.setText(getSavedID(this))
        }

        val loginChecked = sharedPref.getBoolean("LoginCheckBoxState", false)
        binding.autoLoginCheckBox.isChecked = loginChecked
        if(loginChecked) {
            if(!idSaveChecked) {
                binding.loginIdInputEditText.setText(getSavedID(this))
            }
            binding.loginPwInputEditText.setText(getSavedPW(this))

            binding.loginButton.performClick()
        }
    }
    fun saveID(context: Context, id: String) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("savedID", id)
            apply()
        }
    }
    fun savePW(context: Context, pw: String) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("savedPW", pw)
            apply()
        }
    }
    fun getSavedID(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("savedID", null)
    }
    fun getSavedPW(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_pw", null)
    }

    fun init(){
        rdb = Firebase.database.getReference("Users")
        sharedPref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
        binding.loginButton.setOnClickListener {
            if(binding.loginIdInputEditText.text.toString()==""){
                idRequestDlg()
            } else if (binding.loginPwInputEditText.text.toString()==""){
                pwRequestDlg()
            } else {
                detectUser(binding.loginIdInputEditText.text.toString(), binding.loginPwInputEditText.text.toString())
            }
        }
        binding.signUpBotton.setOnClickListener {
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }
        binding.idStoreCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val editor = sharedPref.edit()
            editor.putBoolean("IDCheckBoxState", isChecked)
            editor.apply()
            if(!isChecked) {
                saveID(this,"")
            }
        }
        binding.autoLoginCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val editor = sharedPref.edit()
            editor.putBoolean("LoginCheckBoxState", isChecked)
            editor.apply()
            if(!isChecked){
                savePW(this, "")
            }
        }
    }
    fun detectUser(id: String, pw: String){
        val employeeDB = rdb.child("employee")
        val employerDB = rdb.child("employer")

        employerDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loginResult = 0
                for (userSnapshot in dataSnapshot.children) {
                    if(userSnapshot.child("user_id").value == id) {
                        if(userSnapshot.child("password").value == getSHA256Hash(pw)){
                            loginResult = 2
                            break
                        }
                        else {
                            loginResult = 1
                            break
                        }
                    }
                }
                if (loginResult == 0) {
                    employeeDB.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (userSnapshot in dataSnapshot.children) {
                                if (userSnapshot.child("user_id").value == id) {
                                    if (userSnapshot.child("password").value == getSHA256Hash(pw)) {
                                        loginResult = 3
                                        break
                                    } else {
                                        loginResult = 1
                                        break
                                    }
                                }
                            }
                            handleLoginResult(id)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d("Database Error", databaseError.message)
                            handleLoginResult(id)
                        }
                    })
                } else {
                    handleLoginResult(id)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Database Error", databaseError.message)
                handleLoginResult(id)
            }
        })
    }
    fun handleLoginResult(user_id:String){
        when (loginResult) {
            0 -> noUserDlg()
            1 -> noMatchPasswordDlg()
            2 -> {
                if(binding.idStoreCheckBox.isChecked) {
                    saveID(this,user_id)
                } else if(binding.autoLoginCheckBox.isChecked) {
                    savePW(this,binding.loginPwInputEditText.text.toString())
                } else {
                    saveID(this,"")
                }
                val i = Intent(this@LoginActivity, EmployerActivityStoreList::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra("user_id",user_id)
                startActivity(i)
            }
            3 -> {
                if(binding.idStoreCheckBox.isChecked) {
                    saveID(this,user_id)
                } else if(binding.autoLoginCheckBox.isChecked) {
                    savePW(this,binding.loginPwInputEditText.text.toString())
                } else {
                    saveID(this,"")
                }
                val i = Intent(this@LoginActivity, EmployeeActivityMain::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra("user_id",user_id)
                startActivity(i)
            }
        }
    }
    fun idRequestDlg(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            "아이디를 입력해주세요.")
            //.itle("")
            .setPositiveButton("OK"){ dlg,_ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }
    fun pwRequestDlg(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            "비밀번호를 입력해주세요.")
            //.setTitle("")
            .setPositiveButton("OK"){ dlg,_ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }
    fun noMatchPasswordDlg(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            "비밀번호가 틀립니다.")
            //.setTitle("")
            .setPositiveButton("OK"){ dlg,_ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }
    fun noUserDlg(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            "존재하지 않는 계정입니다.")
            //.setTitle("")
            .setPositiveButton("OK"){ dlg,_ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }
    fun getSHA256Hash(input: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}