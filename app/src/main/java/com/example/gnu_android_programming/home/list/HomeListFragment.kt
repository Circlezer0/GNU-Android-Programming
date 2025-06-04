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
import com.example.gnu_android_programming.database.LedgerDBHelper
import com.example.gnu_android_programming.home.edit.EditEntryDialogFragment
import java.util.Calendar

class HomeListFragment : Fragment() {
    private lateinit var ledgerDBHelper: LedgerDBHelper
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
        ledgerDBHelper = LedgerDBHelper(requireContext())
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
        // 1. DB에서 해당 월의 모든 기록을 읽어옴
        val rawEntries = mutableListOf<LedgerData>()
        val db = ledgerDBHelper.readableDatabase
        val selection = if (!yearMonth.isNullOrEmpty()) "${LedgerDBHelper.COL_DATE} LIKE ?" else null
        val selectionArgs = if (!yearMonth.isNullOrEmpty()) arrayOf("$yearMonth%") else null
        val cursor: Cursor = db.query(
            LedgerDBHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${LedgerDBHelper.COL_DATE} DESC"
        )
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_ID))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_DATE))
            val incomeExpense = cursor.getString(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_TYPE))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_CATEGORY))
            val amount = cursor.getInt(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_AMOUNT))
            val paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_PAYMENT_METHOD))
            val memo = cursor.getString(cursor.getColumnIndexOrThrow(LedgerDBHelper.COL_MEMO))
            rawEntries.add(LedgerData(id, date, incomeExpense, category, amount, paymentMethod, memo))
        }
        cursor.close()

        // 2. 해당 월의 년, 월 정보를 추출 (yearMonth: "yyyy-MM")
        if (yearMonth == null) return
        val parts = yearMonth!!.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()

        // 3. 해당 월의 총 일수 계산
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 4. 날짜별로 그룹화 (key: day(Int), value: LedgerEntry 리스트)
        val grouped = mutableMapOf<Int, MutableList<LedgerData>>()
        for (entry in rawEntries) {
            // entry.date: "yyyy-MM-dd"
            val day = entry.date.split("-").getOrNull(2)?.toIntOrNull()
            if (day != null) {
                grouped.getOrPut(day) { mutableListOf() }.add(entry)
            }
        }

        // 5. 새 리스트 생성: 1일부터 maxDay까지
        val newList = mutableListOf<LedgerData>()
        for (day in 1..maxDay) {
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            val entriesForDay = grouped[day]
            if (entriesForDay.isNullOrEmpty()) {
                // 기록이 없는 날짜: dummy 항목, date는 유지, 나머지는 "-"
                newList.add(LedgerData(0, dateStr, "-", "-", 0, "-", "-"))
            } else {
                // 기록이 있는 경우: 첫 항목은 날짜 그대로, 나머지는 날짜를 빈 문자열로 처리
                entriesForDay.sortBy { it.id } // 원하는 기준으로 정렬
                entriesForDay.forEachIndexed { index, entry ->
                    if (index > 0) {
                        newList.add(entry.copy(date = ""))
                    } else {
                        newList.add(entry)
                    }
                }
            }
        }

        adapter.updateData(newList)
    }
}
