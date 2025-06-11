package com.example.gnu_android_programming.home.edit

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.LedgerDao
import com.example.gnu_android_programming.home.list.LedgerData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEntryDialogFragment : DialogFragment() {

    interface EditEntryListener {
        fun onEntryUpdated()   // 수정이 완료되었을 때 호출
        fun onEntryDeleted()   // 삭제가 완료되었을 때 호출
    }

    private var listener: EditEntryListener? = null

    // 다이얼로그에 표시할 LedgerData (Serializable로 전달)
    private lateinit var entry: LedgerData

    // DB Helper
    private lateinit var ledgerDao: LedgerDao

    // 레이아웃 뷰 참조용
    private lateinit var etDate: EditText
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioIncome: RadioButton
    private lateinit var radioExpense: RadioButton
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var radioCard: RadioButton
    private lateinit var radioCash: RadioButton
    private lateinit var etMemo: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    companion object {
        private const val ARG_ENTRY = "arg_entry"

        /**
         * 다이얼로그를 띄울 때 수정할 LedgerData(Serializable)를 인자로 전달
         */
        fun newInstance(entry: LedgerData): EditEntryDialogFragment {
            val frag = EditEntryDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_ENTRY, entry)   // Serializable로 전달
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 Serializable로 꺼내기
        entry = arguments?.getSerializable(ARG_ENTRY) as? LedgerData
            ?: throw IllegalStateException("EditEntryDialogFragment requires a LedgerData argument")
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그 너비를 MATCH_PARENT로 설정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @SuppressLint("ClickableViewAccessibility", "UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        // 뷰를 inflate
        val inflater = LayoutInflater.from(requireContext())
        val rootView = inflater.inflate(R.layout.dialog_edit_entry, null)

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

        // Spinner 초기화 (카테고리)
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

        // 2) 날짜(EditText) 우측 Drawable 터치 시 DatePickerDialog
        etDate.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etDate.compoundDrawables[2]
                if (drawableRight != null) {
                    val touchX = event.rawX.toInt()
                    val editTextRight = etDate.right
                    val drawableWidth = drawableRight.bounds.width()
                    val drawablePadding = etDate.compoundDrawablePadding
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

        // 3) [저장] 버튼 클릭 리스너
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
            // --- 유효성 검사 끝 ---

            // 업데이트할 객체 생성
            val updatedEntry = entry.copy(
                date = newDate,
                incomeExpense = newType,
                category = newCategory,
                amount = newAmount,
                paymentMethod = newPayment,
                memo = newMemo
            )

            // DB 업데이트
            ledgerDao.update(updatedEntry)
            listener?.onEntryUpdated()
            dialog?.dismiss()
        }

        // 4) [삭제] 버튼 클릭 리스너
        btnDelete.setOnClickListener {
            ledgerDao.delete(entry.id)
            listener?.onEntryDeleted()
            dialog?.dismiss()
        }

        // 5) 다이얼로그 생성 (단순 닫기만 처리)
        val alert = AlertDialog.Builder(requireContext())
            .setView(rootView)
            .create()

        return alert
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        // 취소 버튼 또는 바깥 터치 시 특별히 처리할 로직이 있으면 여기에…
    }

    /**
     * 호출한 Fragment(HomeListFragment 등)에서 반드시 listener를 설정해야 합니다.
     */
    fun setEditEntryListener(listener: EditEntryListener) {
        this.listener = listener
    }
}
