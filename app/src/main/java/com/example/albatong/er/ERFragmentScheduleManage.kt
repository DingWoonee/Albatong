package com.example.albatong.er

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class ERFragmentScheduleManage : Fragment() {
    private lateinit var scheduleDateTextView: TextView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var sdb: DatabaseReference
    private lateinit var edb: DatabaseReference
    private lateinit var cdb: DatabaseReference

    private lateinit var scheduleRecyclerView: RecyclerView
    var scheduleAdapter: ERAdapterSchedule ?= null
    var store_id: String? = null
    var store_name:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.er_fragment_schedulemanage, container, false)
        scheduleDateTextView = view.findViewById(R.id.scheduleDate)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val schedulePlusButton = view.findViewById<Button>(R.id.schedulePlus)
        val timeTableButton = view.findViewById<Button>(R.id.timeTable)

        val i = requireActivity().intent
        store_id = i.getStringExtra("store_id")
        store_name = i.getStringExtra("store_name")

        scheduleRecyclerView = view.findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(requireContext())
        scheduleRecyclerView.layoutManager = layoutManager

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.time

            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate)

            scheduleDateTextView.text = formattedDate

            updateScheduleAdapter(formattedDate)

        }

        //초기 recyclerview은 오늘날짜
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        cdb = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager").child("calendar")
            .child(year.toString()+"년").child(month.toString()+"월").child(day.toString()+"일")

        val option = FirebaseRecyclerOptions.Builder<Schedule>()
            .setQuery(cdb, Schedule::class.java)
            .build()
        scheduleAdapter = ERAdapterSchedule(option)
        scheduleRecyclerView.adapter = scheduleAdapter

        //초기 선택된 상태
        val initialDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
        scheduleDateTextView.text = initialDate
        updateScheduleAdapter(initialDate)
        calendarView.date = calendar.timeInMillis

        //database 설정
        sdb = Firebase.database.getReference("Stores")
        edb = Firebase.database.getReference("Stores").child(store_id!!).child("storeInfo").child("employee")

        scheduleAdapter?.notifyDataSetChanged()


        schedulePlusButton.setOnClickListener {
            val selectedDate = scheduleDateTextView.text.toString()
            showScheduleDialog(selectedDate)
        }

        timeTableButton.setOnClickListener {
            val selectedDate = scheduleDateTextView.text.toString()
            showTimeTableActivity(selectedDate,store_id!!)
        }


        return view
    }

    private fun updateScheduleAdapter(selectedDate: String) {
        //update 할때마다 해당 날짜
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(selectedDate)!!

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        cdb = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager").child("calendar")
            .child(year.toString()+"년").child(month.toString()+"월").child(day.toString()+"일")

        val option = FirebaseRecyclerOptions.Builder<Schedule>()
            .setQuery(cdb, Schedule::class.java)
            .build()

        scheduleAdapter?.updateOptions(option)

        scheduleAdapter?.startListening()
        scheduleAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        scheduleAdapter?.startListening()
        scheduleAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        scheduleAdapter?.stopListening()
    }

    private fun showTimeTableActivity(selectedDate: String, store_id:String) {
        val intent = Intent(requireContext(), ERActivityTimeTable::class.java)
        intent.putExtra("store_id",store_id!!)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
    }

    private fun showScheduleDialog(selectedDate: String) {
        val dialogView = layoutInflater.inflate(R.layout.er_schedule_dialog, null)
        val nameSpinner = dialogView.findViewById<Spinner>(R.id.nameSpinner)
        val startHourSpinner = dialogView.findViewById<Spinner>(R.id.startHourSpinner)
        val startMinuteSpinner = dialogView.findViewById<Spinner>(R.id.startMinuteSpinner)
        val endHourSpinner = dialogView.findViewById<Spinner>(R.id.endHourSpinner)
        val endMinuteSpinner = dialogView.findViewById<Spinner>(R.id.endMinuteSpinner)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("$selectedDate")
            .setPositiveButton("저장") { _, _ ->
                val id = nameSpinner.selectedItem.toString().split("/")[0]
                val name = nameSpinner.selectedItem.toString().split("/")[1]
                val startTime = "${startHourSpinner.selectedItem}:${startMinuteSpinner.selectedItem}"
                val endTime = "${endHourSpinner.selectedItem}:${endMinuteSpinner.selectedItem}"

                if (name.isNotEmpty() && id.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                    if (isTimeValid(startTime, endTime)) {
                        saveScheduleToDatabase(selectedDate, name, id, startTime, endTime)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "시간을 올바르게 설정해주세요!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "모든 데이터를 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)

        val dialog = dialogBuilder.create()

        edb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val employeeList: MutableList<String> = ArrayList()

                for (employeeSnapshot in dataSnapshot.children) {
                    val employeeInfo = employeeSnapshot.child("employeeId").value.toString() +
                            "/" +
                            employeeSnapshot.child("name").value.toString()
                    employeeList.add(employeeInfo)
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    employeeList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                nameSpinner.adapter = adapter

                dialog.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    //dialog시간 역전 방지
    private fun isTimeValid(startTime: String, endTime: String): Boolean {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)
        return start?.before(end) == true
    }

    private fun saveScheduleToDatabase(selectedDate: String, name:String, employeeId: String, startTime: String, endTime: String) {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(selectedDate)!!

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val monthRef = sdb.child(store_id!!).child("storeManager")
            .child("calendar").child(year.toString() + "년").child(month.toString() + "월")
        val dayRef = monthRef.child(day.toString() + "일")
        val employeeRef = dayRef.child("$employeeId : $startTime-$endTime")

        val schedule = Schedule(name, store_name!!, startTime, endTime,50000)

        employeeRef.setValue(schedule)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "일정 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

        employeeRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val scheduleList = mutableListOf<Schedule>()
                for (employeeSnapshot in snapshot.children) {
                    val employeeId = employeeSnapshot.key

                    if (employeeId != null) {
                        val startTime =
                            employeeSnapshot.child("startTime").getValue(String::class.java)
                        val endTime = employeeSnapshot.child("endTime").getValue(String::class.java)
                        val name = employeeSnapshot.child("name").getValue(String::class.java)

                        if (startTime != null && endTime != null && name != null) {
                            val schedule = Schedule(name, store_name!!, startTime, endTime,10000)
                            scheduleList.add(schedule)
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}