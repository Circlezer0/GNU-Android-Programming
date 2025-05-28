package com.example.gnu_android_programming.home.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.LedgerDBHelper
import java.util.*

class HomeCalendarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private var yearMonth: String? = null // "yyyy-MM" 형태

    companion object {
        fun newInstance(yearMonth: String): HomeCalendarFragment {
            val fragment = HomeCalendarFragment()
            val args = Bundle()
            args.putString("yearMonth", yearMonth)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        yearMonth = arguments?.getString("yearMonth")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ledger_custom_calendar, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCalendar)
        recyclerView.layoutManager = GridLayoutManager(context, 7)
        val dayDataList = generateCalendarData(yearMonth)
        adapter = CalendarAdapter(dayDataList)
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(WeekDividerItemDecoration())
        return view
    }

    /**
     * "yyyy-MM" 형태의 yearMonth에 해당하는 달력 데이터를 생성합니다.
     * 앞뒤의 빈 셀을 포함하여 7열 형태로 구성합니다.
     */
    private fun generateCalendarData(yearMonth: String?): List<DayData> {
        val list = mutableListOf<DayData>()
        if (yearMonth == null) return list

        val parts = yearMonth.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() // 1~12

        // 해당 월의 첫날을 설정 (월은 0부터 시작하므로 month - 1)
        val calendar = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)  // Sunday=1, Monday=2, ...
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 앞쪽 빈 셀 추가
        val emptyCells = firstDayOfWeek - 1
        for (i in 0 until emptyCells) {
            list.add(DayData(0))
        }
        // 1일부터 해당 월의 마지막 날까지 데이터 추가 (DB에서 수익/지출 조회)
        val ledgerDBHelper = LedgerDBHelper(requireContext())
        for (day in 1..daysInMonth) {
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            val (revenue, expense) = ledgerDBHelper.getDailySummary(dateStr)
            list.add(DayData(day, revenue, expense))
        }
        // 마지막 주 채우기 위해 뒤쪽 빈 셀 추가
        while (list.size % 7 != 0) {
            list.add(DayData(0))
        }
        return list
    }
}