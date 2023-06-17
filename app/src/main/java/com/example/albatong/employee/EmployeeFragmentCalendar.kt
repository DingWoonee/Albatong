package com.example.albatong.employee

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.albatong.R
import com.example.albatong.data.Schedule
import com.example.albatong.data.StoreList
import com.example.albatong.databinding.EmployeeCalendarDayBinding
import com.example.albatong.databinding.EmployeeCalendarHeaderBinding
import com.example.albatong.databinding.EmployeeFragmentCalendarBinding
import com.example.albatong.ee.EEActivitySpecificMain
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale

enum class Month(val numStr: String) {
    JANUARY("1"),
    FEBRUARY("2"),
    MARCH("3"),
    APRIL("4"),
    MAY("5"),
    JUNE("6"),
    JULY("7"),
    AUGUST("8"),
    SEPTEMBER("9"),
    OCTOBER("10"),
    NOVEMBER("11"),
    DECEMBER("12");
    companion object {
        fun fromNumStr(numStr: String): Month {
            return values().first { it.numStr == numStr }
        }
    }
}

class EmployeeFragmentCalendar : Fragment() {
    lateinit var binding: EmployeeFragmentCalendarBinding
    private var selectedDate: LocalDate? = null
    private lateinit var employee_store_db: DatabaseReference
    private lateinit var all_store_db: DatabaseReference
    private var scheduleAdapter:EmployeeAdapterMainCalendar? = null
    private var user_id:String? = null
    private var monthSchedule: Array<MutableList<Schedule>> = Array<MutableList<Schedule>>(31) { mutableListOf<Schedule>() }
    private var storeListMap: MutableMap<String, Int> = mutableMapOf()
    private var monthCalc:Long = 0
    private var isFirst:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = requireActivity().intent
        user_id = i.getStringExtra("user_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        employee_store_db = Firebase.database.getReference("Users/employee/${user_id}/store")
        all_store_db = Firebase.database.getReference("Stores")
        val view = inflater.inflate(R.layout.employee_fragment_calendar, container, false)
        binding = EmployeeFragmentCalendarBinding.bind(view)

        binding.calendarView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = scheduleAdapter
        }
        scheduleAdapter?.notifyDataSetChanged()

        updateMonthSchedule(YearMonth.now().year.toString()+"년", Month.valueOf(YearMonth.now().month.toString()).numStr+"월")
        init()
        initRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()
        scheduleAdapter?.notifyDataSetChanged()
    }

    fun initRecyclerView() {
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        scheduleAdapter = EmployeeAdapterMainCalendar(mutableListOf())
        scheduleAdapter?.itemClickListener = object : EmployeeAdapterMainCalendar.OnItemClickListener{
            override fun OnItemClick(schedule:Schedule) {
                val i = Intent(requireActivity(), EEActivitySpecificMain::class.java)
                i.putExtra("store_id", schedule.store_id)
                i.putExtra("store_name", schedule.storeName)
                i.putExtra("user_id", user_id)
                startActivity(i)
            }
        }
        binding.scheduleRecyclerView.adapter = scheduleAdapter
    }

    fun init() {
        binding.exFiveNextMonthImage.setOnClickListener {
            monthCalc++
            scheduleAdapter?.items?.clear()
            scheduleAdapter?.notifyDataSetChanged()
            updateMonthSchedule(YearMonth.now().plusMonths(monthCalc).year.toString()+"년", Month.valueOf(YearMonth.now().plusMonths(monthCalc).month.toString()).numStr+"월",1)
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            monthCalc--
            scheduleAdapter?.items?.clear()
            scheduleAdapter?.notifyDataSetChanged()
            updateMonthSchedule(YearMonth.now().plusMonths(monthCalc).year.toString()+"년", Month.valueOf(YearMonth.now().plusMonths(monthCalc).month.toString()).numStr+"월",-1)
        }
    }

    fun calendarBinding() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.toString()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
            }
        }
    }

    private fun updateMonthSchedule(year:String, month:String, goWhere:Int = 0) {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    calendarBinding()
                    isFirst = false
                }

                var goWhere2 = goWhere
                monthSchedule = Array<MutableList<Schedule>>(31) { mutableListOf<Schedule>() }
                for (childSnapshot in dataSnapshot.children) {
                    val storeList = childSnapshot.getValue(StoreList::class.java)

                    if(storeList != null) {
                        storeListMap[storeList.store_id] = storeList.storeColor
                        searchScheduleByStoreId(storeList!!.store_id, year, month, goWhere2)
                    }
                    goWhere2 = 0
                }
                scheduleAdapter?.storeBackColorMap = storeListMap
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Employee", "Database read error: " + databaseError.message)
            }
        }
        employee_store_db.addValueEventListener(valueEventListener)
    }

    private fun searchScheduleByStoreId(store_id: String, year:String, month:String, goWhere:Int = 0) {
        all_store_db.child("$store_id/storeManager/calendar/$year/$month").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i:Int
                for (scheduleSnapshot in snapshot.children) {
                    i = scheduleSnapshot.key.toString().substringBefore("일").toInt() - 1

                    for(aSche in scheduleSnapshot.children) {
                        val aScheId = aSche.key.toString().substringBefore(" ")

                        if(aScheId == user_id){
                            val a = aSche.getValue(Schedule::class.java)
                            if(a!=null) {
                                monthSchedule[i].add(a)
                            }
                        }
                    }
                    monthSchedule[i] = monthSchedule[i].sortedWith(compareBy({ it.startTime }, { it.endTime })).toMutableList()
                }
                if(isFirst) {
                    calendarBinding()
                    isFirst = false
                } else {
                    when(goWhere){
                        1 -> {
                            binding.calendarView.findFirstVisibleMonth()?.let {
                                binding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
                            }
                        }
                        -1 -> {
                            binding.calendarView.findFirstVisibleMonth()?.let {
                                binding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
                            }
                        }
                    }
                    configureBinders(daysOfWeek())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Employee", "Database read error: " + error.message)
            }
        })
    }

    private fun updateScheduleAdapterForDate(date: LocalDate?) {
        if(date != null) {
            val dayOfWeek = date.dayOfWeek //요일
            val dayOfMonth = date.dayOfMonth //일
            val month = date.monthValue //월
            val year = date.year //년

            scheduleAdapter?.items = monthSchedule[dayOfMonth-1]
            scheduleAdapter?.notifyDataSetChanged()
        }
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = EmployeeCalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@EmployeeFragmentCalendar.binding
                            binding.calendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                            updateScheduleAdapterForDate(day.date)
                        }
                    }
                }
            }
        }
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.day
                val layout = container.binding.calendarDayLayout
                textView.text = data.date.dayOfMonth.toString()

                val schedule1 = container.binding.schedule1
                val schedule2 = container.binding.schedule2
                val schedule3 = container.binding.schedule3
                schedule1.background = null
                schedule2.background = null
                schedule3.background = null

                if (data.position == DayPosition.MonthDate) {
                    //textView.setTextColorRes(R.color.example_5_text_grey)
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.employee_badge_selected else 0)
                    if(data.date.dayOfWeek.toString() == "SATURDAY")
                        textView.setTextColor(Color.parseColor("#FF0D48F6"))
                    else if(data.date.dayOfWeek.toString() == "SUNDAY")
                        textView.setTextColor(Color.parseColor("#FF0000"))

                    val daySchedule = monthSchedule[data.date.dayOfMonth-1]

                    if (daySchedule.count() > 0) {
                        when(daySchedule.count()){
                            1 -> {
                                schedule1.setBackgroundColor(storeListMap[daySchedule[0].store_id]!!)
                                schedule1.text = daySchedule[0].storeName
                            }
                            2 -> {
                                schedule1.setBackgroundColor(storeListMap[daySchedule[0].store_id]!!)
                                schedule1.text = daySchedule[0].storeName
                                schedule2.setBackgroundColor(storeListMap[daySchedule[1].store_id]!!)
                                schedule2.text = daySchedule[1].storeName
                            }
                            3 -> {
                                schedule1.setBackgroundColor(storeListMap[daySchedule[0].store_id]!!)
                                schedule1.text = daySchedule[0].storeName
                                schedule2.setBackgroundColor(storeListMap[daySchedule[1].store_id]!!)
                                schedule2.text = daySchedule[1].storeName
                                schedule3.setBackgroundColor(storeListMap[daySchedule[2].store_id]!!)
                                schedule3.text = daySchedule[2].storeName
                            }
                        }
                    }
                } else {
                    textView.setTextColor(Color.parseColor("#999999"))
                    //textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.sub_color))
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = EmployeeCalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                //tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.text = daysOfWeek[index].toString().substring(0,3)
                                //tv.setTextColorRes(R.color.white)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }
}