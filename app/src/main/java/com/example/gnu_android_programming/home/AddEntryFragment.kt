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
import com.example.gnu_android_programming.database.LedgerDao
import com.example.gnu_android_programming.home.list.LedgerData
import java.text.SimpleDateFormat
import java.util.*

/**
 * 가계부 항목 추가 화면을 제공하는 Fragment
 */
class AddEntryFragment : Fragment() {

    // Ledger 데이터베이스 접근을 위한 DAO
    private lateinit var ledgerDao: LedgerDao

    /**
     * Fragment 레이아웃을 inflate 합니다.
     * @param inflater 레이아웃 인플레이터
     * @param container 부모 뷰
     * @param savedInstanceState 저장된 인스턴스 상태
     * @return 생성된 View 객체
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_add_entry.xml 레이아웃을 뷰로 생성하여 반환
        return inflater.inflate(R.layout.fragment_add_entry, container, false)
    }

    /**
     * 뷰 생성 후 초기화 로직을 수행합니다.
     * @param view 생성된 뷰
     * @param savedInstanceState 저장된 인스턴스 상태
     */
    @SuppressLint("ClickableViewAccessibility", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // DB Helper 초기화
        ledgerDao = LedgerDao(requireContext())

        // 뷰 참조
        val etDate = view.findViewById<EditText>(R.id.etDate)                                       // 날짜 입력 EditText
        val radioGroupIncomeExpense = view.findViewById<RadioGroup>(R.id.radioGroupIncomeExpense)   // 수익/지출 선택 라디오 그룹
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)                      // 카테고리 선택 Spinner
        val etAmount = view.findViewById<EditText>(R.id.etAmount)                                   // 금액 입력 EditText
        val radioGroupPayment = view.findViewById<RadioGroup>(R.id.radioGroupPayment)               // 결제 수단 선택 라디오 그룹
        val etMemo = view.findViewById<EditText>(R.id.etMemo)                                       // 메모 입력 EditText
        val btnSave = view.findViewById<Button>(R.id.btnSave)                                       // 저장 버튼

        // 1) 날짜 칸에 기본값으로 현재 날짜 설정 (yyyy-MM-dd)
        val today = Calendar.getInstance()                              // 오늘 날짜 캘린더
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())   // 날짜 포맷터
        etDate.setText(sdf.format(today.time))                          // EditText에 기본값 설정

        // 2) 카테고리 스피너 초기화 ("식물", "화분", "부자재", "운영비")
        val categories = arrayOf("식물", "화분", "부자재", "운영비")
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            categories
        )
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item)  // 드롭다운 레이아웃 설정
        spinnerCategory.adapter = adapterSpinner                                // Spinner에 어댑터 설정

        // 3) 날짜(EditText) 우측 아이콘 터치 시 DatePickerDialog 띄우기
        etDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etDate.compoundDrawables[2]                      // 우측 drawable
                if (drawableRight != null) {
                    val touchX = event.rawX.toInt()
                    val editTextRight = etDate.right
                    val drawableWidth = drawableRight.bounds.width()
                    val drawablePadding = etDate.compoundDrawablePadding

                    // 터치 위치가 우측 아이콘 영역인지 검사
                    if (touchX >= (editTextRight - drawableWidth - drawablePadding)) {
                        // 현재 텍스트로 날짜 파싱, 실패 시 today 사용
                        val calForPicker = Calendar.getInstance()
                        try {
                            val currentText = etDate.text.toString()
                            val parsedDate = sdf.parse(currentText)
                            if (parsedDate != null) calForPicker.time = parsedDate
                        } catch (e: Exception) {
                            calForPicker.time = today.time
                        }

                        // DatePickerDialog 표시
                        DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                // 선택된 날짜를 yyyy-MM-dd 형식으로 설정
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

        // 4) 저장 버튼 클릭 시 예외 처리 및 DB에 삽입
        btnSave.setOnClickListener {
            // --- 유효성 검사 시작 ---
            val dateText = etDate.text.toString().trim()                             // 날짜 텍스트
            if (TextUtils.isEmpty(dateText)) {
                Toast.makeText(requireContext(), "날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 수익/지출 선택 검사
            val selectedIncomeExpenseId = radioGroupIncomeExpense.checkedRadioButtonId
            if (selectedIncomeExpenseId == -1) {
                Toast.makeText(requireContext(), "수익/지출을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioIncomeExpense = view.findViewById<RadioButton>(selectedIncomeExpenseId)
            val incomeExpense = radioIncomeExpense.text.toString()                  // 선택된 수익/지출 텍스트

            // 금액 입력 검사
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

            // 결제 수단 선택 검사
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            if (selectedPaymentId == -1) {
                Toast.makeText(requireContext(), "결제 수단(카드/현금)을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioPayment = view.findViewById<RadioButton>(selectedPaymentId)
            val paymentMethod = radioPayment.text.toString()                         // 선택된 결제 수단
            // --- 유효성 검사 끝 ---

            // 나머지 필드 가져오기
            val category = spinnerCategory.selectedItem.toString()                   // 선택된 카테고리
            val memo = etMemo.text.toString().trim()                                 // 메모 (빈 문자열 허용)

            // LedgerData 객체 생성 (id 자동생성)
            val entry = LedgerData(
                date = dateText,
                incomeExpense = incomeExpense,
                category = category,
                amount = amountValue,
                paymentMethod = paymentMethod,
                memo = memo
            )

            // DB에 삽입
            ledgerDao.insert(entry)

            Toast.makeText(requireContext(), "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()

            // 저장 후 이전 Fragment(달력/리스트)로 돌아감
            parentFragmentManager.popBackStack()
        }
    }
}
