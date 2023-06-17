package com.example.albatong.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.albatong.R
import com.example.albatong.ee.EEActivitySpecificMain
import com.example.albatong.employee.EmployeeActivityMain
import com.example.albatong.employer.EmployerActivityStoreList
import com.example.albatong.er.ERActivitySpecificMain
import com.example.albatong.login.LoginActivity.Companion.KEY_IS_EMPLOYER
import com.example.albatong.login.LoginActivity.Companion.KEY_USER_ID_FOR_AUTO_LOGIN
import com.example.albatong.login.LoginActivity.Companion.KEY_WAS_LOGOUT

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // 로그인 상태를 확인합니다.
            val sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val wasLogout = sharedPreferences.getBoolean(KEY_WAS_LOGOUT, true)
            val isEmployer = sharedPreferences.getBoolean(KEY_IS_EMPLOYER, true)
            val user_id = sharedPreferences.getString(KEY_USER_ID_FOR_AUTO_LOGIN, "")

            // 사용자가 로그인 상태라면 EmployeeActivityMain나 EmployerActivitySotreList를, 아니라면 LoginActivity를 시작합니다.
            if (!wasLogout) {
                if (isEmployer){
                    val i = Intent(this, EmployerActivityStoreList::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    i.putExtra("user_id",user_id)
                    startActivity(i)
                }
                else{
                    val i = Intent(this, EmployeeActivityMain::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    i.putExtra("user_id",user_id)
                    startActivity(i)
                }
            } else {
                val i = Intent(this, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
        }, 1500)  // 1.5초 동안 스플래시 화면을 보여줍니다.
    }
}