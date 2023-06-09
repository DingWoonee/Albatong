package com.example.albatong.employee

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.Schedule
import com.example.albatong.data.UserSalary
import com.example.albatong.databinding.EmployeeFragmentSalarycalculateBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class EmployeeFragmentSalaryCalculate : Fragment() {

    var binding: EmployeeFragmentSalarycalculateBinding? = null
    var adapter: EmployeeAdapterSalaryRecyclerView? = null
    var employeeDB: DatabaseReference?=null
    var userID: String?=null
    var monthlySalary = ArrayList<UserSalary>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var i = requireActivity().intent
        userID = i.getStringExtra("user_id")

        employeeDB = Firebase.database.getReference("Users/employee/$userID")

        binding = EmployeeFragmentSalarycalculateBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.apply {
            list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = EmployeeAdapterSalaryRecyclerView(monthlySalary)
            list.adapter = adapter

            var year = Calendar.getInstance().get(Calendar.YEAR)
            var month= Calendar.getInstance().get(Calendar.MONTH)+1
            date.text = "${year}년 ${month}월"
            loadSalary(year, month)

            leftBtn.setOnClickListener {
                adapter!!.values.clear()
                if(month>1) {
                    month--
                } else {
                    year--
                    month=12
                }
                date.text = "${year}년 ${month}월"
                loadSalary(year, month)
            }

            rightBtn.setOnClickListener {
                adapter!!.values.clear()
                if(month<12) {
                    month++
                } else {
                    year++
                    month=1
                }
                date.text = "${year}년 ${month}월"
                loadSalary(year, month)
            }
        }
    }

    private fun loadSalary(year: Int, month: Int) {
        var totalMinutes = 0
        var storeSalary = 0.0f
        var totalSalary = 0.0f

        employeeDB!!.child("store").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (store in snapshot.children) {
                    val storeID = store.key!!
                    var storeName: String?=null

                    val storeDB = Firebase.database.getReference("Stores/$storeID/storeManager/calendar")
                    storeDB.child(year.toString()+"년").child(month.toString()+"월")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            for (day in snapshot.children) {
                                for(schedule in day.children) {
                                    if(schedule.key!!.contains(userID!!)) {
                                        val time = schedule.getValue(Schedule::class.java)!!
                                        storeName = time.storeName
                                        totalMinutes += calculateTimeDifference(time.startTime, time.endTime)
                                        storeSalary += (totalMinutes / 60.0f) * time.salary
                                        totalSalary += storeSalary
                                        binding!!.totalSalary.text = "이번달 월급: $totalSalary 원"
                                    }
                                }
                            }
                            if(storeName != null && totalMinutes != 0) {
                                adapter!!.values.add(UserSalary(storeName!!, totalMinutes, storeSalary))
                            }
                            adapter!!.notifyDataSetChanged()
                            totalMinutes = 0
                            storeSalary = 0.0f
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Employee", "Database read error: " + error.message)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Employee", "Database read error: " + error.message)
            }
        })
    }

    fun calculateTimeDifference(startTime: String, endTime: String): Int {
        val startTimeParts = startTime.split(":")
        val endTimeParts = endTime.split(":")

        val startHour = startTimeParts[0].toInt()
        val startMinute = startTimeParts[1].toInt()
        val endHour = endTimeParts[0].toInt()
        val endMinute = endTimeParts[1].toInt()

        return (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
    }
}

