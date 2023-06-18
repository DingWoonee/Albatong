package com.example.albatong.employee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.albatong.R
import com.example.albatong.data.UserData
import com.example.albatong.databinding.EmployeeActivityMainBinding
import com.example.albatong.login.SignAcitivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EmployeeActivityMain : AppCompatActivity() {
    lateinit var binding: EmployeeActivityMainBinding
    val viewModel: EmployeeViewModel by viewModels()
    val textarr = arrayListOf<String>("일정", "급여", "근무지")
    val imgarr = arrayListOf<Int>(
        R.drawable.baseline_calendar_month_24, R.drawable.money_image, R.drawable.workspace_image)
    var user: UserData ?= null
    private var backKeyPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EmployeeActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userID = intent.getStringExtra("user_id")
        val userName = intent.getStringExtra("user_name")
        val userDB = Firebase.database.getReference("Users/employee")

        userDB.child("$userID").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(UserData::class.java)!!
                    viewModel.setLiveData(user!!)
                } else {
                    Log.e("Employee", "Not a registered user")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Employee", "Database read error: " + databaseError.message)
            }
        })

        binding.employerSettingButton.setOnClickListener {
            val intent = Intent(this@EmployeeActivityMain, Employeesetting::class.java)
            intent.putExtra("user_id", userID)
            startActivity(intent)
        }

        binding.employerNotificationHistoryButton.setOnClickListener {
            val intent = Intent(this@EmployeeActivityMain, SignAcitivity::class.java)
            intent.putExtra("user_id", userID)
            intent.putExtra("user_type", this.intent.getStringExtra("user_type"))
            startActivity(intent)
        }

        initLayout()
    }

    private fun initLayout(){
        binding.employeeViewPager.adapter = EmployeeAdapterViewPage(this)
        TabLayoutMediator(binding.employeeTabLayout, binding.employeeViewPager) {
                tab, pos ->
            tab.text = textarr[pos]
            tab.setIcon(imgarr[pos])
        }.attach()
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

}