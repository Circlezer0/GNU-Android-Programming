package com.example.gnu_android_programming.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.LedgerDao
import com.example.gnu_android_programming.home.calendar.HomeCalendarFragment
import com.example.gnu_android_programming.home.list.HomeListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var ledgerDao: LedgerDao
    private lateinit var tvMonthYear: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvNet: TextView
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var btnCalendar: Button
    private lateinit var btnList: Button
    private lateinit var fabAddEntry: FloatingActionButton

    // 현재 표시 중인 년/월
    private var currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_home.xml을 inflate
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ledgerDao = LedgerDao(requireContext())

        // 헤더 컨트롤 초기화
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvIncome = view.findViewById(R.id.tvIncomeValue)
        tvExpense = view.findViewById(R.id.tvExpenseValue)
        tvNet = view.findViewById(R.id.tvNetValue)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        btnCalendar = view.findViewById(R.id.btnCalendar)
        btnList = view.findViewById(R.id.btnList)
        fabAddEntry = view.findViewById(R.id.fabAddEntry)

        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            // 이전 달로 이동 시 현재 프래그먼트 새로고침
            refreshCurrentFragment()
        }
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            // 다음 달로 이동 시 현재 프래그먼트 새로고침
            refreshCurrentFragment()
        }

        btnCalendar.setOnClickListener {
            // 달력 모드: CustomCalendarFragment 사용
            childFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, HomeCalendarFragment.newInstance(getCurrentYearMonth()))
                .commit()
        }
        btnList.setOnClickListener {
            // 리스트 모드: ListFragment 사용
            childFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, HomeListFragment.newInstance(getCurrentYearMonth()))
                .commit()
        }

        // FloatingActionButton 클릭 시 AddEntryActivity로 이동
        fabAddEntry.setOnClickListener {
            // AddEntryFragment 를 불러와서 fragment_container에 교체하고,
            // 뒤로 가기 버튼을 눌렀을 때 이전 Fragment로 돌아갈 수 있게 BackStack에 추가
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddEntryFragment())
                .addToBackStack(null)
                .commit()
        }

        updateMonthDisplay()

        // 최초 진입 시 달력 모드로 시작
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.home_fragment_container, HomeCalendarFragment.newInstance(getCurrentYearMonth()))
                .commit()
        }
    }

    private fun updateMonthDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val yearMonth = sdf.format(currentCalendar.time)
        tvMonthYear.text = yearMonth

        // DB에서 월간 통계 불러오기
        val (income, expense, net) = ledgerDao.getMonthlySummary(yearMonth)

        // NumberFormat을 사용해 3자리마다 쉼표 붙이기
        val formattedIncome = NumberFormat.getNumberInstance(Locale.getDefault()).format(income)
        val formattedExpense = NumberFormat.getNumberInstance(Locale.getDefault()).format(expense)
        val formattedNet = NumberFormat.getNumberInstance(Locale.getDefault()).format(net)

        tvIncome.text = formattedIncome
        tvExpense.text = formattedExpense
        tvNet.text = formattedNet
    }

    private fun getCurrentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(currentCalendar.time)
    }

    // 현재 프래그먼트에 따라 새로고침
    private fun refreshCurrentFragment() {
        val currentFragment = childFragmentManager.findFragmentById(R.id.home_fragment_container)
        val newFragment = when (currentFragment) {
            is HomeCalendarFragment -> HomeCalendarFragment.newInstance(getCurrentYearMonth())
            is HomeListFragment -> HomeListFragment.newInstance(getCurrentYearMonth())
            else -> HomeCalendarFragment.newInstance(getCurrentYearMonth())
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, newFragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        updateMonthDisplay()
        refreshCurrentFragment()
    }
}