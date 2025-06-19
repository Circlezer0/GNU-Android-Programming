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

/**
 * 홈 화면을 구성하는 Fragment로,
 * 월별 가계부 요약 및 달력/리스트 뷰 전환 기능을 제공합니다.
 */
class HomeFragment : Fragment() {

    // Ledger 데이터베이스 접근을 위한 DAO
    private lateinit var ledgerDao: LedgerDao

    // 연월 표시 TextView
    private lateinit var tvMonthYear: TextView
    // 월간 수입 값 표시 TextView
    private lateinit var tvIncome: TextView
    // 월간 지출 값 표시 TextView
    private lateinit var tvExpense: TextView
    // 월간 순이익 값 표시 TextView
    private lateinit var tvNet: TextView
    // 이전 달로 이동하는 버튼
    private lateinit var btnPrevMonth: Button
    // 다음 달로 이동하는 버튼
    private lateinit var btnNextMonth: Button
    // 달력 모드 전환 버튼
    private lateinit var btnCalendar: Button
    // 리스트 모드 전환 버튼
    private lateinit var btnList: Button
    // 새로운 가계부 항목 추가를 위한 FloatingActionButton
    private lateinit var fabAddEntry: FloatingActionButton

    // 현재 표시 중인 년/월 정보
    private var currentCalendar = Calendar.getInstance()

    /**
     * Fragment 레이아웃을 inflate합니다.
     * @param inflater 레이아웃 인플레이터
     * @param container 부모 뷰
     * @param savedInstanceState 저장된 인스턴스 상태
     * @return 생성된 View 객체
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_home.xml 레이아웃을 뷰로 생성하여 반환
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * 뷰가 생성된 후 초기화 로직을 수행합니다.
     * @param view 생성된 뷰
     * @param savedInstanceState 저장된 인스턴스 상태
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // DAO 초기화
        ledgerDao = LedgerDao(requireContext())

        // 뷰 요소 연결
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvIncome = view.findViewById(R.id.tvIncomeValue)
        tvExpense = view.findViewById(R.id.tvExpenseValue)
        tvNet = view.findViewById(R.id.tvNetValue)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        btnCalendar = view.findViewById(R.id.btnCalendar)
        btnList = view.findViewById(R.id.btnList)
        fabAddEntry = view.findViewById(R.id.fabAddEntry)

        // 이전 달 버튼 클릭 설정
        btnPrevMonth.setOnClickListener {
            // 달력을 한 달 이전으로 이동
            currentCalendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            // 현재 표시 중인 프래그먼트 새로고침
            refreshCurrentFragment()
        }
        // 다음 달 버튼 클릭 설정
        btnNextMonth.setOnClickListener {
            // 달력을 한 달 이후로 이동
            currentCalendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            // 현재 표시 중인 프래그먼트 새로고침
            refreshCurrentFragment()
        }

        // 달력 모드 버튼 클릭 시 달력 뷰로 전환
        btnCalendar.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.home_fragment_container,
                    HomeCalendarFragment.newInstance(getCurrentYearMonth())
                )
                .commit()
        }
        // 리스트 모드 버튼 클릭 시 리스트 뷰로 전환
        btnList.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.home_fragment_container,
                    HomeListFragment.newInstance(getCurrentYearMonth())
                )
                .commit()
        }

        // 항목 추가 FloatingActionButton 클릭 시 AddEntryFragment로 전환
        fabAddEntry.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddEntryFragment())
                .addToBackStack(null)
                .commit()
        }

        // 화면 초기 로드 시 연월 표시 업데이트
        updateMonthDisplay()

        // 초기 진입 시 달력 모드로 설정
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(
                    R.id.home_fragment_container,
                    HomeCalendarFragment.newInstance(getCurrentYearMonth())
                )
                .commit()
        }
    }

    /**
     * 연월 문자열을 기반으로 헤더와 통계값을 업데이트합니다.
     */
    private fun updateMonthDisplay() {
        // 연월 포맷 생성 ("yyyy-MM")
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val yearMonth = sdf.format(currentCalendar.time)
        tvMonthYear.text = yearMonth

        // DAO에서 월별 수입/지출/순이익 조회
        val (income, expense, net) = ledgerDao.getMonthlySummary(yearMonth)

        // 숫자 포맷 적용 (3자리 구분 쉼표)
        val formattedIncome = NumberFormat.getNumberInstance(Locale.getDefault()).format(income)
        val formattedExpense = NumberFormat.getNumberInstance(Locale.getDefault()).format(expense)
        val formattedNet = NumberFormat.getNumberInstance(Locale.getDefault()).format(net)

        // 포맷된 값 표시
        tvIncome.text = formattedIncome
        tvExpense.text = formattedExpense
        tvNet.text = formattedNet
    }

    /**
     * 현재 Calendar 인스턴스의 연월을 문자열로 반환합니다.
     * @return "yyyy-MM" 형식의 연월 문자열
     */
    private fun getCurrentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(currentCalendar.time)
    }

    /**
     * 현재 표시 중인 자식 Fragment를 동일한 연월을 기준으로 새로고침합니다.
     */
    private fun refreshCurrentFragment() {
        // 현재 자식 Fragment 식별
        val currentFragment = childFragmentManager.findFragmentById(R.id.home_fragment_container)
        // 해당 Fragment 새 인스턴스로 교체
        val newFragment = when (currentFragment) {
            is HomeCalendarFragment -> HomeCalendarFragment.newInstance(getCurrentYearMonth())
            is HomeListFragment -> HomeListFragment.newInstance(getCurrentYearMonth())
            else -> HomeCalendarFragment.newInstance(getCurrentYearMonth())
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, newFragment)
            .commit()
    }

    /**
     * Fragment가 재개될 때 월별 정보와 자식 Fragment를 갱신합니다.
     */
    override fun onResume() {
        super.onResume()
        // 화면 재진입 시 헤더 및 프래그먼트 새로고침
        updateMonthDisplay()
        refreshCurrentFragment()
    }
}
