package com.example.albatong.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.albatong.R
import com.example.albatong.data.UserData
import com.example.albatong.databinding.SignUpActivityBinding
import com.example.albatong.databinding.SignUpDialogInfoCheckBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest


class SignUpActivity : AppCompatActivity() {
    lateinit var binding: SignUpActivityBinding
    lateinit var rdb: DatabaseReference
    var isIdOk:Boolean = false
    var isNameOk = false
    var isPasswordOk = false
    var isPasswordConfirmOk = false
    var isTelOk = false
    var isEmailOk = false
    val regexTel = Regex("""^\d{3}-\d{4}-\d{4}$""")
    val regexEmail = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    val regexId = Regex("""^[A-Za-z0-9]+$""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        init()
    }

    override fun onResume() {
        super.onResume()
        binding.signUpButton.isEnabled = false
        binding.signUpButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_color)
        binding.duplicationCheckButton.isEnabled = false
        binding.duplicationCheckButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_color)
    }
    fun init(){
        rdb = Firebase.database.getReference("Users")

        binding.signUpButton.setOnClickListener {
            saveUser(if(binding.signUpRadioGroup.checkedRadioButtonId==binding.signUpEmployerRadioButton.id) "사장"
            else "알바")
        }
        binding.signUpId.addTextChangedListener {
            isIdOk = false
            binding.idCheckImage.isGone = true
            binding.duplicationCheckButton.isEnabled = false
            binding.duplicationCheckButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_color)
            binding.errorCheck.isVisible = true
            if(binding.signUpId.text.toString().length == 0 ) {
                binding.errorCheck.text = ""
            } else if (!regexId.containsMatchIn(binding.signUpId.text.toString())){
                binding.errorCheck.text = "   대소문자와 숫자만 입력해주세요."
            } else if (it.toString().length < 5) {
                binding.errorCheck.text = "   5자 이상 입력해주세요."
            } else if(it.toString().length > 15) {
                binding.errorCheck.text = "   15자 이하로 입력해주세요."
            } else {
                binding.duplicationCheckButton.isEnabled = true
                binding.duplicationCheckButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.main_color)
                binding.errorCheck.text = ""
                binding.errorCheck.isGone = true
            }
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.signUpName.addTextChangedListener {
            isNameOk = false
            if(binding.signUpName.length()>0)
                isNameOk = true
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.signUpPassword.addTextChangedListener {
            isPasswordOk = false
            isPasswordConfirmOk = false
            if(binding.signUpPassword.text.toString().length>0) {
                isPasswordOk = true
            }
            if(isPasswordOk && binding.signUpPassword.text.toString() == binding.signUpPasswordConfirm.text.toString()){
                isPasswordConfirmOk = true
                binding.passwordCheckImage.isVisible = true
            } else {
                binding.passwordCheckImage.isGone = true
            }
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.signUpPasswordConfirm.addTextChangedListener {
            isPasswordConfirmOk = false
            if(isPasswordOk && binding.signUpPassword.text.toString() == binding.signUpPasswordConfirm.text.toString()){
                isPasswordConfirmOk = true
                binding.passwordCheckImage.isVisible = true
            } else {
                binding.passwordCheckImage.isGone = true
            }
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.signUpTel.addTextChangedListener {
            isTelOk = false
            if(regexTel.containsMatchIn(binding.signUpTel.text.toString())) {
                isTelOk = true
                binding.telCheckImage.isVisible = true
            } else {
                binding.telCheckImage.isGone = true
            }
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.signUpEmail.addTextChangedListener {
            isEmailOk = false
            if(regexEmail.containsMatchIn(binding.signUpEmail.text.toString())) {
                isEmailOk = true
                binding.emailCheckImage.isVisible = true
            } else {
                binding.emailCheckImage.isGone = true
            }
            isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
        }
        binding.duplicationCheckButton.setOnClickListener {
            isUserExist()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    fun inputCheckDlg(role: String, onOk: () -> Unit){
        val dlgBinding = SignUpDialogInfoCheckBinding.inflate(layoutInflater)

        val signUpName = binding.signUpName.text
        val signupId = binding.signUpId.text
        val signupTel = binding.signUpTel.text
        val signUpEmail = binding.signUpEmail.text

        dlgBinding.id.text = signupId
        dlgBinding.email.text = signUpEmail
        dlgBinding.tel.text = signupTel
        dlgBinding.role.text = role

        val builder = AlertDialog.Builder(this)
        val dlg = builder.setView(dlgBinding.root).show()

        dlg.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setGravity(Gravity.CENTER)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))



        dlgBinding.registerBtn.setOnClickListener{
            onOk()
        }

        dlgBinding.cancelBtn.setOnClickListener{
            dlg?.dismiss()

        }
    }
    private fun isUserExist(){
        val employeeDB = rdb.child("employee")
        val employerDB = rdb.child("employer")
        isIdOk = true

        val ee_query = employeeDB.orderByChild("user_id").equalTo(binding.signUpId.text.toString())
        val er_query = employerDB.orderByChild("user_id").equalTo(binding.signUpId.text.toString())

        ee_query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    isIdOk = false
                    idExistDlg()
                } else {
                    er_query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                isIdOk = false
                                idExistDlg()
                            } else {
                                idNotExistDlg()
                                binding.idCheckImage.isVisible = true
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            println("loadPost:onCancelled ${error.toException()}")
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("loadPost:onCancelled ${error.toException()}")
            }
        })
    }
    private fun saveUser(role: String) {
        binding.apply {
            val employeeDB = rdb.child("employee")
            val employerDB = rdb.child("employer")

            inputCheckDlg(role) {
                var newUserData = UserData(
                    signUpId.text.toString(),
                    getSHA256Hash(signUpPassword.text.toString()),
                    signUpName.text.toString(),
                    signUpTel.text.toString(),
                    signUpEmail.text.toString(),
                    null
                )
                if (role.equals("알바"))
                    employeeDB.child(signUpId.text.toString()).setValue(newUserData)
                else if (role.equals("사장"))
                    employerDB.child(signUpId.text.toString()).setValue(newUserData)

                val i = Intent(this@SignUpActivity, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
        }
    }
    fun idExistDlg() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("존재하는 ID입니다.")
            //.setTitle("")
            .setPositiveButton("OK"){
                    dlg,_ -> dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }
    fun idNotExistDlg() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("사용 가능한 ID입니다.")
            //.setTitle("")
            .setPositiveButton("OK"){
                    dlg,_ ->
                dlg.dismiss()
                isAllRight(isIdOk,isNameOk,isPasswordOk,isPasswordConfirmOk,isTelOk,isEmailOk)
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

    fun isAllRight(Id:Boolean, Name:Boolean, Password:Boolean, PasswordConfirm:Boolean, Tel:Boolean, Email:Boolean) {
        if(Id&&Name&&Password&&PasswordConfirm&&Tel&&Email){
            binding.signUpButton.isEnabled = true
            binding.signUpButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.main_color)
        } else {
            binding.signUpButton.isEnabled = false
            binding.signUpButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_color)
        }
    }
}