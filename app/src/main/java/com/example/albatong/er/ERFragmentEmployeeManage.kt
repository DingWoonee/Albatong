package com.example.albatong.er

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.currentCoroutineContext
import java.text.SimpleDateFormat
import java.util.*

class ERFragmentEmployeeManage : Fragment() {
    private lateinit var salaryMonthTextView : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: ERAdapterEmployeeManage
    private lateinit var sdb: DatabaseReference
    private lateinit var edb: DatabaseReference
    private var fireDialog: Dialog? = null

    private val scheduleList: MutableList<Schedule> = mutableListOf()
    private var store_id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.er_fragment_employeemanage, container, false)
        val fireButton = view.findViewById<Button>(R.id.fireBtn)
        salaryMonthTextView= view.findViewById(R.id.salaryMonth)

        val i = requireActivity().intent
        store_id = i.getStringExtra("store_id")

        recyclerView = view.findViewById(R.id.recyclerViewSalary)
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        sdb = Firebase.database.getReference("Stores")
        edb = Firebase.database.getReference("Stores").child(store_id!!).child("storeInfo")
            .child("employee")

        val calendar = Calendar.getInstance()
        val selectedDate = calendar.time
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate).substring(5,7)

        salaryMonthTextView.text = formattedDate + "월 월급"

        loadScheduleData()

        fireButton.setOnClickListener {
            if (fireDialog?.isShowing != true) {
                showFireDialog()
            }
        }

        return view
    }

    private fun showFireDialog() {
        if (fireDialog == null) {

            val dialogView = layoutInflater.inflate(R.layout.er_employee_dialog, null)
            val nameSpinner = dialogView.findViewById<Spinner>(R.id.nameSpinner)

            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ ->
                    val id = nameSpinner.selectedItem.toString().split("/")[0]

                    val confirmationDialogBuilder = AlertDialog.Builder(requireContext())
                    confirmationDialogBuilder.setTitle("추방 확인")
                    confirmationDialogBuilder.setMessage("정말로 추방시키시겠습니까?")
                    confirmationDialogBuilder.setPositiveButton("확인") { _, _ ->
                        performFiring(id)
                    }
                    confirmationDialogBuilder.setNegativeButton("취소", null)

                    val confirmationDialog = confirmationDialogBuilder.create()
                    confirmationDialog.show()
                }
                .setNegativeButton("취소", null)

            fireDialog = dialogBuilder.create()

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
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Employer", "김태정 오류 : " + databaseError.message)
                }
            })

        }

        fireDialog?.show()
    }

    private fun performFiring(id: String) {
        val query = edb.orderByChild("employeeId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (employeeSnapshot in dataSnapshot.children) {
                    employeeSnapshot.ref.removeValue()
                }

                val userRef = Firebase.database.getReference("Users")
                userRef.child("employee").child(id).child("store").child(store_id!!)
                    .removeValue()

                val calendarRef =
                    sdb.child(store_id!!).child("storeManager").child("calendar")

                calendarRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(scheduleSnapshot: DataSnapshot) {
                        for (yearSnapshot in scheduleSnapshot.children) {
                            for (monthSnapshot in yearSnapshot.children) {
                                for (daySnapshot in monthSnapshot.children) {
                                    for (snapshot in daySnapshot.children) {
                                        val key =
                                            snapshot.key // "$employeeId : $startTime-$endTime"
                                        val employeeId = key?.substringBefore(" : ")
                                        if (employeeId == id) {
                                            snapshot.ref.removeValue()
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(requireContext(),"추방이 완료되었습니다.",Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Employer", "김태정 오류 : " + error.message)
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Employer", "김태정 오류 : " + databaseError.message)
            }
        })
    }

    private fun loadScheduleData() {
        val scheduleRef = Firebase.database.getReference("Stores").child(store_id!!)
            .child("storeManager").child("calendar")

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val employeeSalaryMap: MutableMap<String, Int> = mutableMapOf()

                for (yearSnapshot in dataSnapshot.children) {
                    for (monthSnapshot in yearSnapshot.children) {
                        for (daySnapshot in monthSnapshot.children) {
                            for (snapshot in daySnapshot.children) {
                                val name = snapshot.child("name").getValue(String::class.java)
                                val startTime =
                                    snapshot.child("startTime").getValue(String::class.java)
                                val endTime = snapshot.child("endTime").getValue(String::class.java)
                                val strSalary = snapshot.child("salary").getValue(Int::class.java)!! * calculateSalary(startTime,endTime) / 60

                                val employeeId =
                                    snapshot.child("employeeId").getValue(String::class.java)
                                val key = "$name/$employeeId"

                                if (employeeSalaryMap.containsKey(key)) {
                                    val currentSalary = employeeSalaryMap[key]!!
                                    employeeSalaryMap[key] = currentSalary + strSalary!!
                                } else {
                                    employeeSalaryMap[key] = strSalary!!
                                }
                            }
                        }
                    }
                }

                scheduleList.clear()

                for ((employeeInfo, totalSalary) in employeeSalaryMap) {
                    val name = employeeInfo.split("/")[0]
                    val schedule = Schedule(name, "storeName", "", "", totalSalary)
                    scheduleList.add(schedule)
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Employer", "김태정 오류 : " + error.message)
            }
        })

        adapter = ERAdapterEmployeeManage(scheduleList)
        recyclerView.adapter = adapter
    }


    private fun calculateSalary(startTime: String?, endTime: String?): Int {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)

        val diffInMillis = end.time - start.time
        val diffInMinutes = diffInMillis / (1000 * 60)

        return diffInMinutes.toInt()
    }
}

