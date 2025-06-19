package com.example.gnu_android_programming.home.edit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.gnu_android_programming.R;
import com.example.gnu_android_programming.database.LedgerDao;
import com.example.gnu_android_programming.home.list.LedgerData;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 거래 내역 수정 다이얼로그를 표시하고
 * 사용자의 입력을 받아 데이터베이스에 업데이트/삭제 처리하는 Fragment
 */
class EditEntryDialogFragment : DialogFragment() {

    /**
     * 수정 및 삭제 완료 시 호출할 콜백 리스너 인터페이스
     */
    interface EditEntryListener {
        /** 수정 완료 시 호출 */
        fun onEntryUpdated()
        /** 삭제 완료 시 호출 */
        fun onEntryDeleted()
    }

    // 리스너 객체 (호출한 Fragment에서 설정 필요)
    private var listener: EditEntryListener? = null

    // 다이얼로그에 표시할 LedgerData (Serializable로 전달)
    private lateinit var entry: LedgerData

    // DB 접근용 DAO
    private lateinit var ledgerDao: LedgerDao

    // 레이아웃 뷰 참조용 변수들
    // 변수 용도: 날짜 입력 EditText
    private lateinit var etDate: EditText
    // 변수 용도: 수익/지출 선택 RadioGroup
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioIncome: RadioButton  // 수익 선택
    private lateinit var radioExpense: RadioButton // 지출 선택
    // 변수 용도: 카테고리 선택 Spinner
    private lateinit var spinnerCategory: Spinner
    // 변수 용도: 금액 입력 EditText
    private lateinit var etAmount: EditText
    // 변수 용도: 결제 수단 선택 RadioGroup
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var radioCard: RadioButton  // 카드 선택
    private lateinit var radioCash: RadioButton  // 현금 선택
    // 변수 용도: 메모 입력 EditText
    private lateinit var etMemo: EditText
    // 변수 용도: 저장 버튼
    private lateinit var btnSave: Button
    // 변수 용도: 삭제 버튼
    private lateinit var btnDelete: Button

    companion object {
        private const val ARG_ENTRY = "arg_entry"

        /**
         * 수정할 거래 내역 객체를 Serializable로 전달하여
         * 다이얼로그 인스턴스를 생성하는 팩토리 메서드
         * @param entry 수정할 LedgerData
         * @return EditEntryDialogFragment 인스턴스
         */
        fun newInstance(entry: LedgerData): EditEntryDialogFragment {
            val frag = EditEntryDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_ENTRY, entry)
            frag.arguments = args
            return frag
        }
    }

    /**
     * Fragment 생성 시 전달된 인자를 처리하여 entry 변수에 초기화
     * @throws IllegalStateException ARG_ENTRY가 없을 경우 예외 발생
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entry = arguments?.getSerializable(ARG_ENTRY) as? LedgerData
            ?: throw IllegalStateException("EditEntryDialogFragment requires a LedgerData argument")
    }

    /**
     * 다이얼로그가 시작될 때 너비를 화면 전체로 설정
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 다이얼로그 UI를 생성하고
     * 뷰 바인딩, 초기 데이터 채우기, 이벤트 리스너 설정 수행
     */
    @SuppressLint("ClickableViewAccessibility", "UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        // 뷰를 inflate
        val inflater = LayoutInflater.from(requireContext())
        val rootView = inflater.inflate(R.layout.dialog_edit_entry, null)

        // DAO 초기화
        ledgerDao = LedgerDao(requireContext())

        // 뷰 바인딩
        etDate = rootView.findViewById(R.id.etDate)
        radioGroupType = rootView.findViewById(R.id.radioGroupType)
        radioIncome = rootView.findViewById(R.id.radioIncome)
        radioExpense = rootView.findViewById(R.id.radioExpense)
        spinnerCategory = rootView.findViewById(R.id.spinnerCategory)
        etAmount = rootView.findViewById(R.id.etAmount)
        radioGroupPayment = rootView.findViewById(R.id.radioGroupPayment)
        radioCard = rootView.findViewById(R.id.radioCard)
        radioCash = rootView.findViewById(R.id.radioCash)
        etMemo = rootView.findViewById(R.id.etMemo)
        btnSave = rootView.findViewById(R.id.btnSave)
        btnDelete = rootView.findViewById(R.id.btnDelete)

        // 1) 기존 데이터를 다이얼로그에 채워준다
        etDate.setText(entry.date)
        if (entry.incomeExpense == "수익") radioIncome.isChecked = true
        else if (entry.incomeExpense == "지출") radioExpense.isChecked = true

        // Spinner 초기화 (카테고리 목록 설정)
        val categories = arrayOf("식물", "화분", "부자재", "운영비")
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            categories
        )
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCategory.adapter = adapterSpinner

        // 기존 카테고리 위치로 설정
        val idx = categories.indexOf(entry.category)
        if (idx >= 0) spinnerCategory.setSelection(idx)

        etAmount.setText(entry.amount.toString())
        if (entry.paymentMethod == "카드") radioCard.isChecked = true
        else if (entry.paymentMethod == "현금") radioCash.isChecked = true

        etMemo.setText(entry.memo)

        // 2) 날짜(EditText) 우측 Drawable 터치 시 DatePickerDialog 표시
        etDate.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etDate.compoundDrawables[2]
                if (drawableRight != null) {
                    val touchX = event.rawX.toInt()
                    val editTextRight = etDate.right
                    val drawableWidth = drawableRight.bounds.width()
                    val drawablePadding = etDate.compoundDrawablePadding
                    // 우측 아이콘 터치 여부 확인
                    if (touchX >= (editTextRight - drawableWidth - drawablePadding)) {
                        // 현재 etDate 값을 파싱해 DatePicker 초기값 설정
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val calForPicker = Calendar.getInstance()
                        try {
                            val parsedDate = sdf.parse(etDate.text.toString())
                            if (parsedDate != null) calForPicker.time = parsedDate
                        } catch (_: Exception) {
                            // 파싱 실패 시 오늘 날짜 유지
                        }
                        // DatePickerDialog 생성 및 표시
                        DatePickerDialog(
                            requireContext(),
                            { _, y, m, d ->
                                val selected = String.format("%04d-%02d-%02d", y, m + 1, d)
                                etDate.setText(selected)
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

        // 3) [저장] 버튼 클릭 리스너 설정
        btnSave.setOnClickListener {
            // --- 유효성 검사 ---
            val newDate = etDate.text.toString().trim()
            if (TextUtils.isEmpty(newDate)) {
                Toast.makeText(requireContext(), "날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedTypeId = radioGroupType.checkedRadioButtonId
            if (selectedTypeId == -1) {
                Toast.makeText(requireContext(), "수익/지출을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newType = if (selectedTypeId == radioIncome.id) "수익" else "지출"

            val amountText = etAmount.text.toString().trim()
            if (TextUtils.isEmpty(amountText)) {
                Toast.makeText(requireContext(), "금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newAmount = amountText.toIntOrNull()
            if (newAmount == null || newAmount <= 0) {
                Toast.makeText(requireContext(), "유효한 금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            if (selectedPaymentId == -1) {
                Toast.makeText(requireContext(), "결제 수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newPayment = if (selectedPaymentId == radioCard.id) "카드" else "현금"

            val newCategory = spinnerCategory.selectedItem.toString()
            val newMemo = etMemo.text.toString().trim()
            // --- 유효성 검사 종료 ---

            // 업데이트할 LedgerData 객체 생성
            val updatedEntry = entry.copy(
                date = newDate,
                incomeExpense = newType,
                category = newCategory,
                amount = newAmount,
                paymentMethod = newPayment,
                memo = newMemo
            )

            // DB 업데이트 실행
            ledgerDao.update(updatedEntry)
            listener?.onEntryUpdated()
            dialog?.dismiss()
        }

        // 4) [삭제] 버튼 클릭 리스너 설정
        btnDelete.setOnClickListener {
            ledgerDao.delete(entry.id)
            listener?.onEntryDeleted()
            dialog?.dismiss()
        }

        // 5) 다이얼로그 빌더로 AlertDialog 생성
        val alert = AlertDialog.Builder(requireContext())
            .setView(rootView)
            .create()

        return alert
    }

    /**
     * 다이얼로그 취소(취소 버튼 또는 외부 터치) 시 호출
     * 추가 처리 필요 시 이곳에 구현
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    /**
     * 호스트 Fragment/Activity에서 콜백 리스너를 설정하는 메서드
     * @param listener EditEntryListener 구현체
     */
    fun setEditEntryListener(listener: EditEntryListener) {
        this.listener = listener
    }
}
