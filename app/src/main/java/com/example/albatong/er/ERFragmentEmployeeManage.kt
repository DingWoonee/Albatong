package com.example.albatong.er

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ERFragmentEmployeeManage : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: ERAdapterEmployeeManage
    private lateinit var sdb: DatabaseReference
    private lateinit var edb: DatabaseReference

    private val scheduleList: MutableList<Schedule> = mutableListOf()
    private var store_id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.er_fragment_employeemanage, container, false)

        val i = requireActivity().intent
        store_id = i.getStringExtra("store_id")

        recyclerView = view.findViewById(R.id.recyclerViewSalary)
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        sdb = Firebase.database.getReference("Stores")
        edb = Firebase.database.getReference("Stores").child(store_id!!).child("storeInfo").child("employee")

        loadScheduleData()

        return view
    }

    private fun loadScheduleData() {
        val scheduleRef = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager").child("calendar")
            .child("2023년").child("6월").child("4일")

        val options = FirebaseRecyclerOptions.Builder<Schedule>()
            .setQuery(scheduleRef, Schedule::class.java)
            .build()

        adapter = ERAdapterEmployeeManage(options)
        recyclerView.adapter = adapter

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scheduleList.clear()
                for (snapshot in dataSnapshot.children) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val startTime = snapshot.child("startTime").getValue(String::class.java)
                    val endTime = snapshot.child("endTime").getValue(String::class.java)

                    // 월급 계산 로직 추가
                    val salary = calculateSalary(startTime, endTime).toString()

                    val schedule = Schedule(name!!, startTime!!, endTime!!, salary!!)
                    scheduleList.add(schedule)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }

    private fun calculateSalary(startTime: String?, endTime: String?): Double {
        val hourlyRate = 10.0
        val hoursWorked = calculateHoursWorked(startTime, endTime)
        return hourlyRate * hoursWorked
    }

    private fun calculateHoursWorked(startTime: String?, endTime: String?): Double {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)

        val diffInMillis = end.time - start.time
        val diffInHours = diffInMillis / (1000.0 * 60.0 * 60.0)

        return diffInHours
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
