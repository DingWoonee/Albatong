package com.example.albatong.employee

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.*
import com.example.albatong.databinding.EmployeeFragmentCalendarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class EmployeeFragmentCalendar : Fragment() {

    var binding: EmployeeFragmentCalendarBinding?= null
    var adapter: EmployeeAdapterCalendarRecyclerView?= null
    var employeeDB: DatabaseReference?=null
    val model:EmployeeViewModel by activityViewModels()
    var user: UserData?=null
    var userID: String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var i = requireActivity().intent
        userID = i.getStringExtra("user_id")

        employeeDB = Firebase.database.getReference("Users/employee/$userID")

        binding = EmployeeFragmentCalendarBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.selectedUser.observe(viewLifecycleOwner, Observer {
            if(it != null)
                user = it
        })

        binding!!.apply {
            list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = EmployeeAdapterCalendarRecyclerView(ArrayList<Schedule>())
            list.adapter = adapter

            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                adapter!!.values.clear()

                // 해당 날짜 검색
                employeeDB!!.child("store").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (store in snapshot.children) {
                            val storeID = store.key

                            val storeDB = Firebase.database.getReference("Stores/$storeID/storeManager/calendar")
                            storeDB.child(year.toString()+"년").child((month+1).toString()+"월").child(dayOfMonth.toString()+"일")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (schedule in snapshot.children) {
                                        if (schedule.key!!.contains(userID!!)) {
                                            val time = schedule.getValue(Schedule::class.java)!!
                                            adapter!!.values.add(time)
                                            adapter!!.notifyDataSetChanged()
                                        } else {
                                            Log.i("Schedule", "$userID $storeID Schedule 없음")
                                        }
                                    }
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
                adapter!!.notifyDataSetChanged()
            }
        }
    }
}