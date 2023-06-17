package com.example.albatong.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION_CODES.P
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.albatong.databinding.LoginActivityBinding
import com.example.albatong.ee.EEActivitySpecificMain
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.er.ERActivitySpecificMain
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
    var wasLogout = true
    private var backKeyPressedTime: Long = 0

    companion object{
        var uId:String = "null"

        public const val SHARED_PREF_NAME = "SharedPref"
        public const val KEY_WAS_LOGOUT = "wasLogout"
        public const val KEY_USER_PW_FOR_AUTO_LOGIN = "user_pw_for_auto_login"
        public const val KEY_USER_ID_FOR_AUTO_LOGIN = "user_id_for_auto_login"
        public const val KEY_SAVED_ID = "savedID"
        public const val KEY_IS_EMPLOYER = "isEmployer"

        var sign:Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkLoginData()
    }

    override fun onResume() {
        super.onResume()

        checkLoginData()
    }
    override fun onBackPressed() {
        // 현재 시간이 마지막으로 뒤로 가기 버튼을 눌렀던 시간보다 2초 이상 크면
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // 마지막 '뒤로 가기'버튼 누르기 후, 2초가 지나지 않은 상태에서 '뒤로 가기'버튼을 누르면
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish() // 앱 종료
        }
    }

    fun checkLoginData() {
        wasLogout = sharedPref.getBoolean("wasLogout", true)
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
                binding.loginIdInputEditText.setText(getSavedIdForAutoLogin(this))
            }
            binding.loginPwInputEditText.setText(getSavedPwForAutoLogin(this))

            if(!wasLogout) {
                binding.loginButton.performClick()
            }
        }
    }
    fun saveID(context: Context, id: String) {
        with(sharedPref.edit()) {
            putString(KEY_SAVED_ID, id)
            apply()
        }
    }
    fun saveForAutoLogin(context: Context, id: String, pw: String) {
        with(sharedPref.edit()) {
            putString(KEY_USER_PW_FOR_AUTO_LOGIN, pw)
            putString(KEY_USER_ID_FOR_AUTO_LOGIN, id)
            apply()
        }
    }
    fun getSavedID(context: Context): String? {
        return sharedPref.getString(KEY_SAVED_ID, null)
    }
    fun getSavedPwForAutoLogin(context: Context): String? {
        return sharedPref.getString(KEY_USER_PW_FOR_AUTO_LOGIN, null)
    }
    fun getSavedIdForAutoLogin(context: Context): String? {
        return sharedPref.getString(KEY_USER_ID_FOR_AUTO_LOGIN, null)
    }

    fun init(){
        rdb = Firebase.database.getReference("Users")
        sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
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
                saveForAutoLogin(this, "", "")
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
                            uId = id
                            loginResult = 2
                            break
                        }
                        else {
                            uId = id
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
                saveID(this,"")
                if(binding.idStoreCheckBox.isChecked) {
                    saveID(this,user_id)
                }
                if(binding.autoLoginCheckBox.isChecked) {
                    saveForAutoLogin(this,binding.loginIdInputEditText.text.toString(),binding.loginPwInputEditText.text.toString())

                    val editor = sharedPref.edit()
                    editor.putBoolean("wasLogout", false)
                    editor.putBoolean(KEY_IS_EMPLOYER, true)
                    editor.apply()
                }
                val i = Intent(this@LoginActivity, EmployerActivityStoreList::class.java)
                val i2 = Intent(this@LoginActivity, ERActivitySpecificMain::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra("user_id",user_id)
                i2.putExtra("user_id",user_id)
                startActivity(i)
            }
            3 -> {
                saveID(this,"")
                if(binding.idStoreCheckBox.isChecked) {
                    saveID(this,user_id)
                }
                if(binding.autoLoginCheckBox.isChecked) {
                    saveForAutoLogin(this,binding.loginIdInputEditText.text.toString(),binding.loginPwInputEditText.text.toString())

                    val editor = sharedPref.edit()
                    editor.putBoolean("wasLogout", false)
                    editor.putBoolean(KEY_IS_EMPLOYER, false)
                    editor.apply()
                }
                val i = Intent(this@LoginActivity, EmployeeActivityMain::class.java)
                val i1 = Intent(this@LoginActivity, EEActivitySpecificMain::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra("user_id",user_id)
                i1.putExtra("user_id",user_id)
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