package com.example.albatong.ee

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Employee
import com.example.albatong.data.RequestManager
import com.example.albatong.data.Schedule
import com.example.albatong.data.SignData
import com.example.albatong.databinding.EeCalendarDailogBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class EEFragmentCalendar : Fragment(), EEAdapterCalendar.OnItemClickListener {
    private lateinit var scheduleDateTextView: TextView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var sdb: DatabaseReference
    private lateinit var edb: DatabaseReference
    private lateinit var cdb: DatabaseReference

    private lateinit var scheduleRecyclerView: RecyclerView
    var scheduleAdapter: EEAdapterCalendar? = null
    var store_id: String? = null
    var store_name: String? = null
    var userID: String? = null
    var employeeCount = 0
    var currentDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ee_fragment_calendar, container, false)
        scheduleDateTextView = view.findViewById(R.id.scheduleDate)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        val i = requireActivity().intent
        store_id = i.getStringExtra("store_id")
        store_name = i.getStringExtra("store_name")
        userID = i.getStringExtra("user_id")

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
            .child(year.toString() + "년").child(month.toString() + "월").child(day.toString() + "일")
        sdb = Firebase.database.getReference("Stores")
        edb = Firebase.database.getReference("Stores").child(store_id!!).child("storeInfo").child("employee")

        val option = FirebaseRecyclerOptions.Builder<Schedule>()
            .setQuery(cdb, Schedule::class.java)
            .build()


        scheduleAdapter = EEAdapterCalendar(option, userID, store_id)
        scheduleRecyclerView.adapter = scheduleAdapter

        //초기 선택된 상태
        val initialDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
        scheduleDateTextView.text = initialDate
        updateScheduleAdapter(initialDate)
        calendarView.date = calendar.timeInMillis

        scheduleAdapter?.notifyDataSetChanged()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleAdapter?.itemClickListener = this

    }

    override fun onItemChangeClick(item: Schedule) {
        showChangeDialog(requireContext(), currentDate!!, item)
    }

    private fun updateScheduleAdapter(selectedDate: String) {
        //update 할때마다 해당 날짜
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(selectedDate)!!

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        currentDate = "${year}-${month}-${day}"

        cdb = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager").child("calendar")
            .child(year.toString() + "년").child(month.toString() + "월").child(day.toString() + "일")

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

    private fun showChangeDialog(context: Context,  selectedDate: String, schedule: Schedule) {
        val dlgBinding = EeCalendarDailogBinding.inflate(layoutInflater)
        val nameSpinner = dlgBinding.nameSpinner

        val dialogBuilder = AlertDialog.Builder(context)
        val dlg = dialogBuilder.setView(dlgBinding.root).show()

        dlg.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setGravity(Gravity.CENTER)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.registerBtn.setOnClickListener {
            if(nameSpinner.selectedItem.toString() == "모두") {
                sendExchangeRequestToAll(selectedDate, schedule)
            } else {
                val employeeId = nameSpinner.selectedItem.toString().split("/")[0]
                val employeeName = nameSpinner.selectedItem.toString().split("/")[1]

                // 선택한 직원 정보를 사용하여 교환 요청 처리
                sendExchangeRequest(selectedDate, employeeId, employeeName, schedule)
            }
            dlg.dismiss()
        }

        dlgBinding.cancelBtn.setOnClickListener{
            dlg.dismiss()

        }

        edb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val employeeList: MutableList<String> = ArrayList()

                for (employeeSnapshot in dataSnapshot.children) {
                    if(employeeSnapshot.child("employeeId").value.toString() != userID) {
                        val employeeInfo = employeeSnapshot.child("employeeId").value.toString() +
                                "/" +
                                employeeSnapshot.child("name").value.toString()
                        employeeList.add(employeeInfo)
                        employeeCount++
                    }
                }
                employeeList.add("모두")

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    employeeList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                nameSpinner.adapter = adapter

                dlg.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun sendExchangeRequestToAll(selectedDate: String, schedule: Schedule) {
        val request = RequestManager(schedule, userID!!, schedule.name, "all", employeeCount)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = current.format(formatter)
        val dateTime = LocalDateTime.parse(date, formatter)
        val dayOfWeek = dateTime.dayOfWeek.toString()

        val storeRef = Firebase.database.getReference("Stores/$store_id")
        storeRef.child("storeInfo/employee").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(employee in snapshot.children) {
                    val notificationRef = Firebase.database.getReference("Users/employee/${employee.key}/Sign")
                    notificationRef.addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var count = 0
                                for(n in snapshot.children) {
                                    count++
                                }

                                val msg = "${store_name}\n${schedule.name}님께서 대타 요청을 보냈습니다."
                                notificationRef.child(count.toString()).setValue(SignData(msg, date, "2", schedule, selectedDate, dayOfWeek))
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        storeRef.child("storeManager/request").child("all: $selectedDate ${schedule.startTime}-${schedule.endTime}").setValue(request)
        Toast.makeText(context, "모두에게 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun sendExchangeRequest( selectedDate: String, employeeId: String, employeeName: String, schedule: Schedule) {
        val request = RequestManager(schedule, userID!!, schedule.name, employeeId, 1)

        val requestsRef = Firebase.database.getReference("Stores/$store_id/storeManager/request")
        requestsRef.child("${employeeId}: $selectedDate ${schedule.startTime}-${schedule.endTime}").setValue(request)

        val notificationRef = Firebase.database.getReference("Users/employee/${employeeId}/Sign")
        notificationRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    for(n in snapshot.children) {
                        count++
                    }
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val date = current.format(formatter)
                    val dateTime = LocalDateTime.parse(date, formatter)
                    val dayOfWeek = dateTime.dayOfWeek.toString()

                    val msg = "${store_name}\n${schedule.name}님께서 대타 요청을 보냈습니다."
                    notificationRef.child(count.toString()).setValue(SignData(msg, date, "2", schedule, selectedDate, dayOfWeek))

                    Toast.makeText(context, "${employeeName}님에게 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )
    }
}

