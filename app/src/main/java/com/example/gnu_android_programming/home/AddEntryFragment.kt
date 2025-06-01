package com.example.gnu_android_programming.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.LedgerDBHelper
import com.example.gnu_android_programming.home.list.LedgerData
import java.text.SimpleDateFormat
import java.util.*

class AddEntryFragment : Fragment() {

    private lateinit var ledgerDBHelper: LedgerDBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_add_entry.xml 레이아웃을 inflate
        return inflater.inflate(R.layout.fragment_add_entry, container, false)
    }

    @SuppressLint("ClickableViewAccessibility", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DB Helper 초기화
        ledgerDBHelper = LedgerDBHelper(requireContext())

        // 뷰 참조
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val radioGroupIncomeExpense = view.findViewById<RadioGroup>(R.id.radioGroupIncomeExpense)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val radioGroupPayment = view.findViewById<RadioGroup>(R.id.radioGroupPayment)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // 1) 날짜 칸에 기본값으로 현재 날짜 설정 (yyyy-MM-dd)
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(sdf.format(today.time))

        // 2) 카테고리 스피너 초기화 ("식물", "화분", "부자재", "운영비")
        val categories = arrayOf("식물", "화분", "부자재", "운영비")
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,    // 스피너 아이템 레이아웃 (예: spinner_item.xml)
            categories
        )
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCategory.adapter = adapterSpinner

        // 3) 날짜(EditText) 우측 아이콘 터치 시 DatePickerDialog 띄우기
        etDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // compound drawable(우측)에 터치했는지 확인
                val drawableRight = etDate.compoundDrawables[2]
                if (drawableRight != null) {
                    val touchX = event.rawX.toInt()
                    val editTextRight = etDate.right
                    val drawableWidth = drawableRight.bounds.width()
                    val drawablePadding = etDate.compoundDrawablePadding

                    // 터치 위치가 우측 drawable 영역 안에 들어오면
                    if (touchX >= (editTextRight - drawableWidth - drawablePadding)) {
                        // 현재 etDate에 설정된 텍스트를 기준으로 DatePicker를 열되, 없으면 오늘 날짜 사용
                        val calForPicker = Calendar.getInstance()
                        try {
                            val currentText = etDate.text.toString()
                            val parsedDate = sdf.parse(currentText)
                            if (parsedDate != null) calForPicker.time = parsedDate
                        } catch (e: Exception) {
                            // parsing error 발생 시, today 사용
                            calForPicker.time = today.time
                        }

                        DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                // month는 0부터 시작하므로 +1
                                val selectedDate = String.format(
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    dayOfMonth
                                )
                                etDate.setText(selectedDate)
                            },
                            calForPicker.get(Calendar.YEAR),
                            calForPicker.get(Calendar.MONTH),
                            calForPicker.get(Calendar.DAY_OF_MONTH)
                        ).show()

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // 4) 저장 버튼 클릭 시 예외 처리 + DB에 삽입
        btnSave.setOnClickListener {
            // --- 유효성 검사 시작 ---
            val dateText = etDate.text.toString().trim()
            if (TextUtils.isEmpty(dateText)) {
                Toast.makeText(requireContext(), "날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 수익/지출 라디오 중 하나라도 선택 안 되어 있다면
            val selectedIncomeExpenseId = radioGroupIncomeExpense.checkedRadioButtonId
            if (selectedIncomeExpenseId == -1) {
                Toast.makeText(requireContext(), "수익/지출을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioIncomeExpense = view.findViewById<RadioButton>(selectedIncomeExpenseId)
            val incomeExpense = radioIncomeExpense.text.toString()

            // 스피너(카테고리)는 항상 하나가 선택되므로 별도 검사 생략 가능

            // 금액 검사: 빈 문자열이거나 숫자가 아니면 예외
            val amountText = etAmount.text.toString().trim()
            if (TextUtils.isEmpty(amountText)) {
                Toast.makeText(requireContext(), "금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amountValue = amountText.toIntOrNull()
            if (amountValue == null || amountValue <= 0) {
                Toast.makeText(requireContext(), "유효한 금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 카드/현금 라디오 검사
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            if (selectedPaymentId == -1) {
                Toast.makeText(requireContext(), "결제 수단(카드/현금)을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioPayment = view.findViewById<RadioButton>(selectedPaymentId)
            val paymentMethod = radioPayment.text.toString()

            // --- 유효성 검사 끝 ---

            // 나머지 필드는 예외 처리 없이 바로 가져옴
            val category = spinnerCategory.selectedItem.toString()
            val memo = etMemo.text.toString().trim() // 비고는 빈 문자열 허용

            // LedgerEntry 객체 생성 (id는 자동 생성되므로 0으로 둠)
            val entry = LedgerData(
                date = dateText,
                incomeExpense = incomeExpense,
                category = category,
                amount = amountValue,
                paymentMethod = paymentMethod,
                memo = memo
            )

            // DB에 삽입
            ledgerDBHelper.insertEntry(entry)

            Toast.makeText(requireContext(), "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()

            // 저장 후 이전 Fragment(달력/리스트)로 돌아감
            parentFragmentManager.popBackStack()
        }
    }
}
