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
import java.text.SimpleDateFormat
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
            date.text = String.format("%04d-%02d", year, month)
            loadSalary(year, month)

            leftBtn.setOnClickListener {
                adapter!!.values.clear()
                if(month>1) {
                    month--
                } else {
                    year--
                    month=12
                }
                date.text = String.format("%04d-%02d", year, month)
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
                date.text = String.format("%04d-%02d", year, month)
                loadSalary(year, month)
            }
        }
    }

    private fun loadSalary(year: Int, month: Int) {
        employeeDB!!.child("store").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalMinutes: Int
                var storeSalary: Int
                var totalSalary = 0

                for (store in snapshot.children) {
                    val storeID = store.key!!
                    var storeName: String?=null

                    val storeDB = Firebase.database.getReference("Stores/$storeID/storeManager/calendar")
                    storeDB.child(year.toString()+"년").child(month.toString()+"월")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            totalMinutes = 0
                            storeSalary = 0

                            for (day in snapshot.children) {
                                for(schedule in day.children) {
                                    if(schedule.key!!.contains(userID!!)) {
                                        val time = schedule.getValue(Schedule::class.java)!!
                                        storeName = time.storeName
                                        var dayMinutes = calculateSalary(time.startTime, time.endTime)
                                        //Log.w("알바/시간",storeName + dayMinutes.toString())
                                        totalMinutes += dayMinutes
                                        storeSalary += (dayMinutes / 60) * time.salary
                                    }
                                }
                            }

                            if(storeName != null && totalMinutes != 0) {
                                totalSalary += storeSalary
                                //Log.w("ss",storeSalary.toString())
                                binding!!.totalSalary.text = "총 급여: ￦"+formatNumberWithCommas(totalSalary)
                                //Log.w("total",totalSalary.toString())

                                adapter!!.values.add(UserSalary(storeName!!, totalMinutes, storeSalary))
                            }
                            adapter!!.notifyDataSetChanged()
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

    private fun calculateSalary(startTime: String?, endTime: String?): Int {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)

        val diffInMillis = end.time - start.time
        val diffInMinutes = diffInMillis / (1000 * 60)

        return diffInMinutes.toInt()
    }

    fun formatNumberWithCommas(number: Int): String {
        return String.format("%,d", number)
    }
}

