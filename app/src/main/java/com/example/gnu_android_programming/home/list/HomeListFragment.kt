package com.example.gnu_android_programming.home.list

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.LedgerDao
import com.example.gnu_android_programming.home.edit.EditEntryDialogFragment
import java.util.Calendar

/**
 * 해당 월의 가계부 항목을 리스트 형태로 표시하는 Fragment
 */
class HomeListFragment : Fragment() {
    // Ledger 데이터베이스 접근용 DAO
    private lateinit var ledgerDao: LedgerDao
    // RecyclerView UI
    private lateinit var recyclerView: RecyclerView
    // 데이터 바인딩용 어댑터
    private lateinit var adapter: LedgerAdapter
    // 인자로 전달된 "yyyy-MM" 형식의 연월 문자열
    private var yearMonth: String? = null

    companion object {
        /**
         * Fragment 인스턴스를 생성하고 연월 인자를 설정합니다.
         * @param yearMonth "yyyy-MM" 형식 연월
         * @return 초기화된 HomeListFragment
         */
        fun newInstance(yearMonth: String): HomeListFragment {
            val fragment = HomeListFragment()
            val args = Bundle()
            args.putString("yearMonth", yearMonth)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Fragment 생성 시 전달된 인자를 읽어옵니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        yearMonth = arguments?.getString("yearMonth")
    }

    /**
     * RecyclerView 및 어댑터를 초기화하고, 데이터 로드를 수행합니다.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃 파일(home_ledger_list.xml)을 인플레이트하여 반환
        val view = inflater.inflate(R.layout.home_ledger_list, container, false)
        // DAO 초기화
        ledgerDao = LedgerDao(requireContext())
        // RecyclerView 참조 및 레이아웃 매니저 설정
        recyclerView = view.findViewById(R.id.recyclerViewList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // 어댑터 초기화 및 연결
        adapter = LedgerAdapter(emptyList())
        recyclerView.adapter = adapter

        // 아이템 클릭 시 수정/삭제 다이얼로그 표시
        adapter.onItemClick = { entry ->
            val editDialog = EditEntryDialogFragment.newInstance(entry)
            editDialog.setEditEntryListener(object : EditEntryDialogFragment.EditEntryListener {
                override fun onEntryUpdated() {
                    // 항목 수정 후 리스트 새로 고침
                    loadEntries()
                }
                override fun onEntryDeleted() {
                    // 항목 삭제 후 리스트 새로 고침
                    loadEntries()
                }
            })
            editDialog.show(parentFragmentManager, "edit_entry_dialog")
        }

        // 초기 데이터 로드
        loadEntries()
        return view
    }

    /**
     * DAO에서 해당 월 데이터 조회 후 날짜별로 그룹핑하여 어댑터에 전달합니다.
     */
    private fun loadEntries() {
        // 1) 해당 월의 모든 항목을 한 번에 조회
        val rawEntries = ledgerDao.getEntriesByMonth(yearMonth)

        // 2) yearMonth가 없으면 조회 결과 그대로 사용
        if (yearMonth.isNullOrEmpty()) {
            adapter.updateData(rawEntries)
            return
        }
        // "yyyy-MM"을 연도와 월로 분리
        val (year, month) = yearMonth!!.split("-").map { it.toInt() }

        // 3) 월의 일수 계산 (1일부터 말일까지)
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 4) 조회된 항목을 "일" 단위로 그룹화
        val grouped = rawEntries.groupBy {
            it.date.split("-").getOrNull(2)?.toIntOrNull() ?: 0
        }

        // 5) 1일부터 말일까지 순회하며, 데이터 없는 날은 더미 항목 생성
        val newList = mutableListOf<LedgerData>()
        for (day in 1..maxDay) {
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            val entries = grouped[day]
            if (entries.isNullOrEmpty()) {
                // 기록 없는 날 → 기본 더미 데이터 삽입
                newList += LedgerData(
                    id = 0,
                    date = dateStr,
                    incomeExpense = "-",
                    category = "-",
                    amount = 0,
                    paymentMethod = "-",
                    memo = "-"
                )
            } else {
                // 해당 날짜 기록이 있으면 첫 행만 날짜 표시, 나머지는 빈 날짜
                entries.sortedBy { it.id }.forEachIndexed { idx, e ->
                    newList += if (idx == 0) e
                    else e.copy(date = "")
                }
            }
        }

        // 6) 어댑터에 가공된 리스트 전달
        adapter.updateData(newList)
    }
}
