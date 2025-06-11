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

class HomeListFragment : Fragment() {
    private lateinit var ledgerDao: LedgerDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LedgerAdapter
    private var yearMonth: String? = null

    companion object {
        fun newInstance(yearMonth: String): HomeListFragment  {
            val fragment = HomeListFragment()
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
        // XML 파일 내 RecyclerView ID는 반드시 recyclerViewList로 유지
        val view = inflater.inflate(R.layout.home_ledger_list, container, false)
        ledgerDao = LedgerDao(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LedgerAdapter(emptyList())
        recyclerView.adapter = adapter

        // 아이템 클릭 이벤트
        adapter.onItemClick = { entry ->
            val editDialog = EditEntryDialogFragment.newInstance(entry)
            editDialog.setEditEntryListener(object : EditEntryDialogFragment.EditEntryListener {
                override fun onEntryUpdated() {
                    // 수정된 후 리스트 데이터를 다시 로드
                    loadEntries()
                }
                override fun onEntryDeleted() {
                    // 삭제된 후 리스트 데이터를 다시 로드
                    loadEntries()
                }
            })
            editDialog.show(parentFragmentManager, "edit_entry_dialog")
        }

        loadEntries()
        return view
    }

    private fun loadEntries() {
        // 1) DAO로 한 방에 불러오기
        val rawEntries = ledgerDao.getEntriesByMonth(yearMonth)

        // 2) yearMonth 파싱
        if (yearMonth.isNullOrEmpty()) {
            adapter.updateData(rawEntries)
            return
        }
        val (year, month) = yearMonth!!.split("-").map { it.toInt() }

        // 3) 해당 월 총 일수 계산
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 4) 날짜별로 그룹화
        val grouped = rawEntries.groupBy {
            it.date.split("-").getOrNull(2)?.toIntOrNull() ?: 0
        }

        // 5) 1일부터 maxDay일까지, 빈 날은 더미로 채우기
        val newList = mutableListOf<LedgerData>()
        for (day in 1..maxDay) {
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            val entries = grouped[day]
            if (entries.isNullOrEmpty()) {
                // 기록 없는 날 → id=0, date만 있고 나머지는 "-"/0
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
                // 기록 있는 날 → 첫 행엔 date, 나머진 date=""
                entries.sortedBy { it.id }.forEachIndexed { idx, e ->
                    newList += if (idx == 0) e
                    else e.copy(date = "")
                }
            }
        }

        // 6) 어댑터에 전달
        adapter.updateData(newList)
    }
}
