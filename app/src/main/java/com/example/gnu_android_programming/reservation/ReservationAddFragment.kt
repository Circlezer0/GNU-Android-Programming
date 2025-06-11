package com.example.gnu_android_programming.reservation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.database.ReservationDao
import com.example.gnu_android_programming.scheduleReservationAlarm
import java.text.SimpleDateFormat
import java.util.*

class ReservationAddFragment : Fragment() {

    // 1) 뷰 바인딩 변수
    private lateinit var etCustomerName: EditText
    private lateinit var etCustomerContact: EditText
    private lateinit var etReservationDate: EditText
    private lateinit var btnPickResDate: ImageButton
    private lateinit var etTransactionDate: EditText
    private lateinit var btnPickTransDate: ImageButton
    private lateinit var radioGroupResType: RadioGroup
    private lateinit var radioPickup: RadioButton
    private lateinit var radioDelivery: RadioButton
    private lateinit var etTransactionLocation: EditText
    private lateinit var btnAddItem: Button
    private lateinit var itemListContainer: LinearLayout
    private lateinit var tvTotalAmount: TextView
    private lateinit var cbPushAlert: CheckBox
    private lateinit var radioGroupPushTime: RadioGroup
    private lateinit var radio5Min: RadioButton
    private lateinit var radio15Min: RadioButton
    private lateinit var radio30Min: RadioButton
    private lateinit var radio1Hour: RadioButton
    private lateinit var radioCustom: RadioButton
    private lateinit var layoutCustomTime: LinearLayout
    private lateinit var npHours: NumberPicker
    private lateinit var npMinutes: NumberPicker
    private lateinit var btnSaveReservation: Button

    // 2) DB 헬퍼
    private lateinit var reservationDao: ReservationDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_reservation_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reservationDao = ReservationDao(requireContext())

        // --- 뷰 바인딩 ---
        etCustomerName       = view.findViewById(R.id.etCustomerName)
        etCustomerContact    = view.findViewById(R.id.etCustomerContact)
        etReservationDate    = view.findViewById(R.id.etReservationDate)
        btnPickResDate       = view.findViewById(R.id.btnPickResDate)
        etTransactionDate    = view.findViewById(R.id.etTransactionDate)
        btnPickTransDate     = view.findViewById(R.id.btnPickTransDate)
        radioGroupResType    = view.findViewById(R.id.radioGroupResType)
        radioPickup          = view.findViewById(R.id.radioPickup)
        radioDelivery        = view.findViewById(R.id.radioDelivery)
        etTransactionLocation= view.findViewById(R.id.etTransactionLocation)
        btnAddItem           = view.findViewById(R.id.btnAddItem)
        itemListContainer    = view.findViewById(R.id.item_list_container)
        tvTotalAmount        = view.findViewById(R.id.tvTotalAmount)
        cbPushAlert          = view.findViewById(R.id.cbPushAlert)
        radioGroupPushTime   = view.findViewById(R.id.radioGroupPushTime)
        radio5Min            = view.findViewById(R.id.radio5Min)
        radio15Min           = view.findViewById(R.id.radio15Min)
        radio30Min           = view.findViewById(R.id.radio30Min)
        radio1Hour           = view.findViewById(R.id.radio1Hour)
        radioCustom          = view.findViewById(R.id.radioCustom)
        layoutCustomTime     = view.findViewById(R.id.layoutCustomTime)
        npHours              = view.findViewById(R.id.npHours)
        npMinutes            = view.findViewById(R.id.npMinutes)
        btnSaveReservation   = view.findViewById(R.id.btnSaveReservation)

        // --- 초기 설정 ---
        val now = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        etReservationDate.setText(fmt.format(now.time))
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        etTransactionDate.setText(fmt.format(tomorrow.time))

        radioPickup.isChecked = true
        npHours.minValue   = 0;  npHours.maxValue   = 23; npHours.wrapSelectorWheel   = true
        npMinutes.minValue = 0;  npMinutes.maxValue = 59; npMinutes.wrapSelectorWheel = true

        // --- 리스너 등록 ---
        btnPickResDate.setOnClickListener    { pickDateTime(etReservationDate) }
        btnPickTransDate.setOnClickListener  { pickDateTime(etTransactionDate) }

        btnAddItem.setOnClickListener        { addNewItemView() }

        cbPushAlert.setOnCheckedChangeListener { _, checked ->
            radioGroupPushTime.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) {
                // 푸시 체크 시 항상 첫번째(5분 전) 선택
                radio5Min.isChecked = true
            }
        }
        radioGroupPushTime.setOnCheckedChangeListener { _, id ->
            layoutCustomTime.visibility = if (id == R.id.radioCustom) View.VISIBLE else View.GONE
        }

        btnSaveReservation.setOnClickListener { saveReservation() }
    }

    /** 날짜+시간 픽커 띄우기 */
    private fun pickDateTime(target: EditText) {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, y, m, d ->
                val cal = Calendar.getInstance().apply { set(y, m, d) }
                TimePickerDialog(requireContext(),
                    { _, h, min ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, min)
                        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        target.setText(fmt.format(cal.time))
                    }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
                ).show()
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /** 한 줄짜리 예약 항목 뷰 생성 */
    private fun addNewItemView() {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_reservation_add_item, itemListContainer, false)

        // 삭제 버튼
        itemView.findViewById<TextView>(R.id.tvDeleteItem).setOnClickListener {
            itemListContainer.removeView(itemView)
            updateTotalAmount()
        }

        // 가격 변경 감지
        itemView.findViewById<EditText>(R.id.etItemPrice)
            .addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, cnt: Int) {
                    updateTotalAmount()
                }
            })

        // 카테고리 Spinner 세팅
        val cats = arrayOf("식물","화분","분갈이","토분")
        itemView.findViewById<Spinner>(R.id.spinnerCategory).apply {
            adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, cats
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }

        itemListContainer.addView(itemView)
        updateTotalAmount()
    }

    /** 총 금액 합계 갱신 */
    private fun updateTotalAmount() {
        var sum = 0
        for (i in 0 until itemListContainer.childCount) {
            val child = itemListContainer.getChildAt(i)
            val price = child.findViewById<EditText>(R.id.etItemPrice)
                .text.toString().toIntOrNull() ?: 0
            sum += price
        }
        tvTotalAmount.text = sum.toString()
    }

    /** 저장 버튼 눌렀을 때 DB에 삽입하고 뒤로 돌아가기 */
    private fun saveReservation() {
        // 1) 아이템 리스트 수집
        val items = mutableListOf<ReservationItemData>()
        for (i in 0 until itemListContainer.childCount) {
            val c = itemListContainer.getChildAt(i)
            items += ReservationItemData(
                itemName = c.findViewById<EditText>(R.id.etItemName).text.toString(),
                price    = c.findViewById<EditText>(R.id.etItemPrice).text.toString().toIntOrNull() ?: 0,
                category = c.findViewById<Spinner>(R.id.spinnerCategory).selectedItem.toString(),
                memo     = c.findViewById<EditText>(R.id.etItemMemo).text.toString()
            )
        }

        // 2) 푸시 알림 시각 결정
        val pushMin = if (cbPushAlert.isChecked) {
            when (radioGroupPushTime.checkedRadioButtonId) {
                R.id.radio5Min   -> 5
                R.id.radio15Min  -> 15
                R.id.radio30Min  -> 30
                R.id.radio1Hour  -> 60
                R.id.radioCustom -> npHours.value * 60 + npMinutes.value
                else             -> null
            }
        } else null

        // 3) DTO 생성
        val data = ReservationData(
            customerName      = etCustomerName.text.toString(),
            customerContact   = etCustomerContact.text.toString(),
            reservationDateTime   = etReservationDate.text.toString(),
            transactionDateTime   = etTransactionDate.text.toString(),
            reservationType   = if (radioPickup.isChecked) "픽업" else "배달",
            transactionLocation   = etTransactionLocation.text.toString(),
            items             = items,
            totalAmount       = tvTotalAmount.text.toString().toInt(),
            pushSetting       = pushMin?.let { PushSettingData(it) }
        )

        // 4) DB에 삽입
        val newId = reservationDao.insert(data)
        data.id = newId

        // 예약 알림 스케줄링
        scheduleReservationAlarm(requireContext(), data)

        // 5) 저장 완료 토스트 + 이전 화면으로
        parentFragmentManager.popBackStack()
    }

    /** 가격 감지만을 위한 간단 TextWatcher */
    abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }
}
