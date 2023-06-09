package com.example.albatong.employee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.albatong.data.UserData
import com.example.albatong.databinding.EmployeeActivityMainBinding
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
    var user: UserData ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EmployeeActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userID = intent.getStringExtra("user_id")
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

        initLayout()
    }

    private fun initLayout(){
        binding.employeeViewPager.adapter = EmployeeAdapterViewPage(this)
        TabLayoutMediator(binding.employeeTabLayout, binding.employeeViewPager) {
                tab, pos ->
            tab.text = textarr[pos]
        }.attach()
    }


}