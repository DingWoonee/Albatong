package com.example.albatong.er

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.databinding.ErActivityTimeTableBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ERActivityTimeTable : AppCompatActivity() {
    private lateinit var store_id:String
    private lateinit var selectedDate: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var timeTableAdapter: ERAdapterTimeTable
    lateinit var binding : ErActivityTimeTableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ErActivityTimeTableBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initRecyclerView()
        loadScheduleData(selectedDate)
    }

    // toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        selectedDate = intent.getStringExtra("selectedDate") ?: ""
        val dateTextView = binding.dateTextView
        dateTextView.text = selectedDate

        recyclerView = binding.timeTableRecyclerView
        timeTableAdapter = ERAdapterTimeTable()

        recyclerView.adapter = timeTableAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadScheduleData(selectedDate: String) {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(selectedDate)!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        store_id = intent.getStringExtra("store_id") ?: ""
        val scheduleRef = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager")
            .child("calendar")
            .child(year.toString() + "년")
            .child(month.toString() + "월")
            .child(day.toString() + "일")

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeTableData = mutableListOf<String>()

                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = format.parse("00:00")!!

                while (calendar.time.before(format.parse("24:00")!!)) {
                    val startTime = format.format(calendar.time)
                    calendar.add(Calendar.MINUTE, 10)
                    var endTime = format.format(calendar.time)
                    if(startTime == "23:50"){
                        endTime = "24:00"
                    }
                    val data = findDataInRange(snapshot, startTime, endTime)
                    if (data.isNullOrEmpty()) {
                        timeTableData.add(startTime + "-" + endTime)
                    } else {
                        timeTableData.addAll(data)
                    }
                }

                timeTableAdapter.setData(timeTableData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun findDataInRange(snapshot: DataSnapshot, startTime: String, endTime: String): List<String> {
        val dataInRange = mutableListOf<String>()

        for (timeSnapshot in snapshot.children) {
            val startTimeDb = timeSnapshot.child("startTime").getValue(String::class.java)
            val endTimeDb = timeSnapshot.child("endTime").getValue(String::class.java)
            val name = timeSnapshot.child("name").getValue(String::class.java)

            if (startTimeDb != null && endTimeDb != null && name != null) {
                if (startTime >= startTimeDb && endTime <= endTimeDb) {
                    val newData = startTime + "-" + endTime + " (" + name + ")"
                    // 겹치는 시간 데이터 추가 확인
                    val existingData = dataInRange.find { it.substringBefore(" (") == newData.substringBefore(" (") }
                    if (existingData != null) {
                        // 겹치는 데이터 업데이트
                        val updatedData = existingData.replace(")", ", $name)")
                        dataInRange[dataInRange.indexOf(existingData)] = updatedData
                    } else {
                        dataInRange.add(newData)
                    }
                }
            }
        }

        return dataInRange
    }


}

