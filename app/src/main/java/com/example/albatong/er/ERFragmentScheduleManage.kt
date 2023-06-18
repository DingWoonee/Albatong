package com.example.albatong.er

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.example.albatong.databinding.ErScheduleDialogBinding
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
    private var scheduleDialog: AlertDialog? = null
    private var isDialogOpen: Boolean = false

    private lateinit var scheduleRecyclerView: RecyclerView
    var scheduleAdapter: ERAdapterSchedule ?= null
    var store_id: String? = null
    var store_name:String? = null

    private lateinit var mondayCheckBox: CheckBox
    private lateinit var tuesdayCheckBox: CheckBox
    private lateinit var wednesdayCheckBox: CheckBox
    private lateinit var thursdayCheckBox: CheckBox
    private lateinit var fridayCheckBox: CheckBox
    private lateinit var saturdayCheckBox: CheckBox
    private lateinit var sundayCheckBox: CheckBox

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
            if (!isDialogOpen) {
                isDialogOpen = true
                showScheduleDialog(selectedDate)
            }
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
        scheduleAdapter?.notifyDataSetChanged()
    }


    private fun showTimeTableActivity(selectedDate: String, store_id:String) {
        val intent = Intent(requireContext(), ERActivityTimeTable::class.java)
        intent.putExtra("store_id", store_id)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
    }

    fun setDialogOpen(isOpen: Boolean) {
        isDialogOpen = isOpen
    }

    private fun showScheduleDialog(selectedDate: String) {
        if (!isDialogOpen) {
            return
        }

        val dlgBinding = ErScheduleDialogBinding.inflate(layoutInflater)
        val nameSpinner = dlgBinding.nameSpinner
        val dialogBuilder = AlertDialog.Builder(requireContext())
        scheduleDialog = dialogBuilder.setView(dlgBinding.root).show()

        scheduleDialog?.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        scheduleDialog?.window?.setGravity(Gravity.CENTER)
        scheduleDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener{

            val id = nameSpinner.selectedItem.toString().split("/")[0]
            val name = nameSpinner.selectedItem.toString().split("/")[1]
            val startTime = "${dlgBinding.startHourSpinner.selectedItem}:${dlgBinding.startMinuteSpinner.selectedItem}"
            val endTime = "${dlgBinding.endHourSpinner.selectedItem}:${dlgBinding.endMinuteSpinner.selectedItem}"
            val salary = dlgBinding.editSalary.text.toString()

            if (name.isNotEmpty() && id.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty() && salary.isNotEmpty()) {
                if (isTimeValid(startTime, endTime)) {
                    if(salary.toInt()>=9620)
                        saveScheduleToDatabase(selectedDate, name, id, startTime, endTime, salary)
                    else
                        Toast.makeText(requireContext(),"최저시급 기준을 넘어야 합니다!",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "시간을 올바르게 설정해주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "모든 데이터를 양식에 맞춰 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
            isDialogOpen = false
            scheduleDialog?.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener{
            isDialogOpen = false
            scheduleDialog?.dismiss()

        }

        edb.addListenerForSingleValueEvent(object : ValueEventListener {
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

                scheduleDialog?.show()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Employer", "김태정 오류: " + databaseError.message)

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

    private fun saveScheduleToDatabase(selectedDate: String, name: String, employeeId: String, startTime: String, endTime: String, salary:String) {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(selectedDate)!!

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val monthRef = sdb.child(store_id!!).child("storeManager")
            .child("calendar").child(year.toString() + "년").child(month.toString() + "월")
        val dayRef = monthRef.child(day.toString() + "일")
        val employeeRef = dayRef.child("$employeeId : $startTime-$endTime")

        val schedule = Schedule(name, store_name!!, startTime, endTime, salary.toInt(), store_id!!)

        isScheduleOverlap(dayRef, name, employeeId, startTime, endTime) { isOverlap ->
            if (!isOverlap) {
                employeeRef.setValue(schedule)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "일정 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "이미 겹치는 일정이 존재합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun isScheduleOverlap(dayRef: DatabaseReference, name: String, employeeId: String, startTime: String, endTime: String, callback: (Boolean) -> Unit) {
        dayRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isOverlap = false

                for (employeeSnapshot in snapshot.children) {
                    val key = employeeSnapshot.key
                    val startTimeSnapshot = employeeSnapshot.child("startTime").getValue(String::class.java)
                    val endTimeSnapshot = employeeSnapshot.child("endTime").getValue(String::class.java)
                    val nameSnapshot = employeeSnapshot.child("name").getValue(String::class.java)

                    if (key != null
                        && (key != "$employeeId : $startTime-$endTime" || nameSnapshot != null && nameSnapshot != name)
                        && startTimeSnapshot != null && endTimeSnapshot != null) {
                        if (isTimeOverlap(startTime, endTime, startTimeSnapshot, endTimeSnapshot)) {
                            if (nameSnapshot == name) {
                                isOverlap = true
                                break
                            }
                        }
                    }
                }
                callback(isOverlap)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(true)
            }
        })
    }


    private fun isTimeOverlap(startTime1: String, endTime1: String, startTime2: String, endTime2: String): Boolean
    {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start1 = format.parse(startTime1)
        val end1 = format.parse(endTime1)
        val start2 = format.parse(startTime2)
        val end2 = format.parse(endTime2)

        return (start1 != null && end1 != null && start2 != null && end2 != null) &&
                (start1.before(end2) && end1.after(start2))
    }
}