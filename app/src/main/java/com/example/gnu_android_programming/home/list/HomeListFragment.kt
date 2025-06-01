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
//        adapter.onItemClick = { entry ->
//            showEditDialog(entry)
//        }

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

    // 항목 클릭 시 수정 다이얼로그 표시
//    @SuppressLint("ClickableViewAccessibility")
//    private fun showEditDialog(entry: LedgerEntry) {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_entry, null)
//
//        // 날짜
//        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
//        // 수익/지출 RadioGroup 및 버튼
//        val radioGroupType = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupType)
//        val radioIncome = dialogView.findViewById<android.widget.RadioButton>(R.id.radioIncome)
//        val radioExpense = dialogView.findViewById<android.widget.RadioButton>(R.id.radioExpense)
//        // 카테고리 Spinner
//        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
//        // 금액
//        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
//        // 결제방식 RadioGroup 및 버튼
//        val radioGroupPayment = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment)
//        val radioCard = dialogView.findViewById<android.widget.RadioButton>(R.id.radioCard)
//        val radioCash = dialogView.findViewById<android.widget.RadioButton>(R.id.radioCash)
//        // 비고
//        val etMemo = dialogView.findViewById<EditText>(R.id.etMemo)
//        // 버튼들
//        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
//        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
//
//        // 기존 데이터 채우기
//        etDate.setText(entry.date)
//        // 수익/지출: 해당 값에 따라 라디오 버튼 선택
//        if (entry.incomeExpense == "수익") {
//            radioIncome.isChecked = true
//        } else if (entry.incomeExpense == "지출") {
//            radioExpense.isChecked = true
//        }
//
//        // 카테고리: Spinner 설정 (예시: "식물", "화분", "부자재", "운영비")
//        val categories = arrayOf("식물", "화분", "부자재", "운영비")
//        val adapterSpinner = ArrayAdapter(
//            requireContext(),
//            R.layout.spinner_item,    // 동일한 커스텀 선택 항목 레이아웃
//            categories
//        )
//        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item) // 동일한 드롭다운 레이아웃
//        spinnerCategory.adapter = adapterSpinner
//
//        // 기존 값 선택 (카테고리 값이 categories 배열에 있을 경우)
//        val catIndex = categories.indexOf(entry.category)
//        if (catIndex >= 0) {
//            spinnerCategory.setSelection(catIndex)
//        }
//
//        etAmount.setText(entry.amount.toString())
//
//        // 결제방식: 라디오 버튼 선택 ("카드" 또는 "현금")
//        if (entry.paymentMethod == "카드") {
//            radioCard.isChecked = true
//        } else if (entry.paymentMethod == "현금") {
//            radioCash.isChecked = true
//        }
//
//        etMemo.setText(entry.memo)
//
//        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
//            .setTitle("항목 수정")
//            .setView(dialogView)
//            .create()
//
//        // etDate 오른쪽에 달력 아이콘 터치 시 DatePickerDialog 호출
//        etDate.setOnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                val drawableRight = etDate.compoundDrawables[2]
//                if (drawableRight != null) {
//                    if (event.rawX >= (etDate.right - drawableRight.bounds.width() - etDate.compoundDrawablePadding)) {
//                        val calendar = Calendar.getInstance()
//                        // etDate에 이미 입력된 날짜가 있으면 해당 날짜로 초기화
//                        val currentText = etDate.text.toString()
//                        if (currentText.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
//                            val parts = currentText.split("-")
//                            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
//                        }
//                        val datePicker = DatePickerDialog(
//                            requireContext(),
//                            { _, year, month, dayOfMonth ->
//                                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
//                                etDate.setText(selectedDate)
//                            },
//                            calendar.get(Calendar.YEAR),
//                            calendar.get(Calendar.MONTH),
//                            calendar.get(Calendar.DAY_OF_MONTH)
//                        )
//                        datePicker.show()
//                        return@setOnTouchListener true
//                    }
//                }
//            }
//            false
//        }
//
//        btnSave.setOnClickListener {
//            // 수익/지출 값
//            val selectedTypeId = radioGroupType.checkedRadioButtonId
//            val type = if (selectedTypeId == radioIncome.id) "수익" else if (selectedTypeId == radioExpense.id) "지출" else ""
//
//            // 선택한 카테고리
//            val categorySelected = spinnerCategory.selectedItem.toString()
//
//            // 결제방식
//            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
//            val paymentMethod = if (selectedPaymentId == radioCard.id) "카드" else if (selectedPaymentId == radioCash.id) "현금" else ""
//
//            // 업데이트된 항목 생성
//            val updatedEntry = entry.copy(
//                date = etDate.text.toString(),
//                incomeExpense = type,
//                category = categorySelected,
//                amount = etAmount.text.toString().toIntOrNull() ?: 0,
//                paymentMethod = paymentMethod,
//                memo = etMemo.text.toString()
//            )
//
//            // DB 업데이트 호출
//            ledgerDBHelper.updateEntry(updatedEntry)
//            loadEntries()
//            dialog.dismiss()
//        }
//
//        btnDelete.setOnClickListener {
//            // DB 삭제 호출
//            ledgerDBHelper.deleteEntry(entry.id)
//            loadEntries()
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }

}
