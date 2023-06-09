package com.example.albatong.ee

import android.app.AlertDialog
import android.content.Context
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ee_fragment_calendar, container, false)
        scheduleDateTextView = view.findViewById(R.id.scheduleDate)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val changeAllButton = view.findViewById<Button>(R.id.changeAllBtn)

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
            .child(year.toString() + "년").child(month.toString() + "월").child(day.toString() + "일")

        val option = FirebaseRecyclerOptions.Builder<Schedule>()
            .setQuery(cdb, Schedule::class.java)
            .build()
        scheduleAdapter = EEAdapterCalendar(option)
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

        changeAllButton.setOnClickListener {
            val selectedDate = scheduleDateTextView.text.toString()
            showChangeAllDialog(selectedDate)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleAdapter?.itemClickListener = this

    }

    override fun onItemNameClick(name: String) {
        showChangeDialog(requireContext(), name)
    }

    override fun onItemChangeClick(item: Schedule) {
        showChangeDialog(requireContext(), item.name)
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

    private fun showChangeDialog(context: Context, name: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.ee_calendar_dailog, null)
        val nameSpinner = dialogView.findViewById<Spinner>(R.id.nameSpinner)

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("교환요청")
            .setPositiveButton("확인") { dialog, _ ->
                val employeeId = nameSpinner.selectedItem.toString().split("/")[0]
                val employeeName = nameSpinner.selectedItem.toString().split("/")[1]

                // 선택한 직원 정보를 사용하여 교환 요청 처리
                sendExchangeRequest(employeeId, employeeName)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

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

    private fun showChangeAllDialog(selectedDate: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("$selectedDate")
            .setMessage("정말 모두에게 보내겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                // "확인" 버튼을 눌렀을 때 실행되는 코드
                sendExchangeRequestToAll(selectedDate)
            }
            .setNegativeButton("취소", null)

        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun sendExchangeRequestToAll(selectedDate: String) {
        TODO("Not yet implemented")
    }

    private fun sendExchangeRequest(employeeId: String, employeeName: String) {
        val requestsRef = Firebase.database.getReference("Stores/$store_id/storeManager/request")
    }
}

