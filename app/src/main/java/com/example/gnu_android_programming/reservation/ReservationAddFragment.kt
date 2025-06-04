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
import java.text.SimpleDateFormat
import java.util.*

class ReservationAddFragment : Fragment() {

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

    private lateinit var itemListContainer: LinearLayout
    private lateinit var btnAddItem: Button

    private lateinit var tvTotalAmount: TextView

    private lateinit var cbPushAlert: CheckBox
    private lateinit var radioGroupPushTime: RadioGroup
    private lateinit var radio5Min: RadioButton
    private lateinit var radio15Min: RadioButton
    private lateinit var radio30Min: RadioButton
    private lateinit var radio1Hour: RadioButton
    private lateinit var radioCustom: RadioButton
    private lateinit var layoutCustomTime: LinearLayout
    private lateinit var npMinutes: NumberPicker
    private lateinit var npHours: NumberPicker

    private lateinit var btnSaveReservation: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 고객명 / 연락처
        etCustomerName = view.findViewById(R.id.etCustomerName)
        etCustomerContact = view.findViewById(R.id.etCustomerContact)

        // 2) 예약 날짜 (DateTime Picker)
        etReservationDate = view.findViewById(R.id.etReservationDate)
        btnPickResDate = view.findViewById(R.id.btnPickResDate)
        btnPickResDate.setOnClickListener {
            pickDateTime(etReservationDate)
        }

        // 3) 거래 날짜 (DateTime Picker)
        etTransactionDate = view.findViewById(R.id.etTransactionDate)
        btnPickTransDate = view.findViewById(R.id.btnPickTransDate)
        btnPickTransDate.setOnClickListener {
            pickDateTime(etTransactionDate)
        }

        // 4) 예약 유형 라디오
        radioGroupResType = view.findViewById(R.id.radioGroupResType)
        radioPickup = view.findViewById(R.id.radioPickup)
        radioDelivery = view.findViewById(R.id.radioDelivery)
        radioDelivery.isChecked = true  // 기본값: 배달

        // 5) 거래 장소
        etTransactionLocation = view.findViewById(R.id.etTransactionLocation)

        // 6) 예약 항목 리스트
        itemListContainer = view.findViewById(R.id.item_list_container)
        btnAddItem = view.findViewById(R.id.btnAddItem)
        btnAddItem.setOnClickListener {
            addNewItemView()
        }

        // 7) 총 금액
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount)
//        updateTotalAmount()  // 초기 총합 0

        // 8) 푸시 알림 설정
        cbPushAlert = view.findViewById(R.id.cbPushAlert)
        radioGroupPushTime = view.findViewById(R.id.radioGroupPushTime)
        radio5Min = view.findViewById(R.id.radio5Min)
        radio15Min = view.findViewById(R.id.radio15Min)
        radio30Min = view.findViewById(R.id.radio30Min)
        radio1Hour = view.findViewById(R.id.radio1Hour)
        radioCustom = view.findViewById(R.id.radioCustom)
        layoutCustomTime = view.findViewById(R.id.layoutCustomTime)
        npMinutes = view.findViewById(R.id.npMinutes)
        npHours = view.findViewById(R.id.npHours)

        // XML에서는 min/max를 제거했으므로 코드에서 설정
        npMinutes.minValue = 0
        npMinutes.maxValue = 59
        npMinutes.wrapSelectorWheel = true

        npHours.minValue = 0
        npHours.maxValue = 23
        npHours.wrapSelectorWheel = true

        cbPushAlert.setOnCheckedChangeListener { _, isChecked ->
            radioGroupPushTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        radioGroupPushTime.setOnCheckedChangeListener { _, checkedId ->
            layoutCustomTime.visibility = if (checkedId == R.id.radioCustom) View.VISIBLE else View.GONE
        }

        // 9) 저장 버튼
        btnSaveReservation = view.findViewById(R.id.btnSaveReservation)
        btnSaveReservation.setOnClickListener {
            saveReservation()
        }
    }

    /**
     * DatePickerDialog → TimePickerDialog 연속 호출하여
     * "yyyy-MM-dd HH:mm" 형식으로 EditText에 입력
     */
    private fun pickDateTime(targetEditText: EditText) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selCal = Calendar.getInstance()
                selCal.set(year, month, dayOfMonth)
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        selCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selCal.set(Calendar.MINUTE, minute)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        targetEditText.setText(sdf.format(selCal.time))
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * “항목 추가” 버튼 클릭 시 호출:
     * res_item_reservation_add_item.xml을 inflate하여 container에 붙이고,
     * 가격 EditText에 TextWatcher를 걸어 총액 갱신
     */
    private fun addNewItemView() {
        val inflater = LayoutInflater.from(requireContext())
        val itemView = inflater.inflate(
            R.layout.res_item_reservation_add_item,
            itemListContainer,
            false
        )

        // 가격 EditText (ID: etItemPrice1)
        val etItemPrice = itemView.findViewById<EditText>(R.id.etItemPrice1)
        etItemPrice.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTotalAmount()
            }
        })

        // 카테고리 Spinner (ID: spinnerCategory1)
        val spinnerCategory = itemView.findViewById<Spinner>(R.id.spinnerCategory1)
        val categories = arrayOf("식물", "화분", "부자재", "운영비")
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterSpinner

        itemListContainer.addView(itemView)
    }

    /**
     * container에 들어 있는 모든 항목의 가격을 합산하여 화면에 표시
     */
    private fun updateTotalAmount() {
        var sum = 0
        for (i in 0 until itemListContainer.childCount) {
            val child = itemListContainer.getChildAt(i)
            val etItemPrice = child.findViewById<EditText>(R.id.etItemPrice1)
            val price = etItemPrice.text.toString().trim().toIntOrNull() ?: 0
            sum += price
        }
        tvTotalAmount.text = sum.toString()
    }

    /**
     * 입력값 유효성 검사 후 Reservation 객체 생성, DB에 저장 또는 서버 전송 처리
     */
    private fun saveReservation() {
        val customerName = etCustomerName.text.toString().trim()
        val customerContact = etCustomerContact.text.toString().trim()
        val resDate = etReservationDate.text.toString().trim()
        val transDate = etTransactionDate.text.toString().trim()

        if (customerName.isEmpty()) {
            Toast.makeText(requireContext(), "고객명을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (customerContact.isEmpty()) {
            Toast.makeText(requireContext(), "고객 연락처를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (resDate.isEmpty()) {
            Toast.makeText(requireContext(), "예약 날짜를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (transDate.isEmpty()) {
            Toast.makeText(requireContext(), "거래 날짜를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedResTypeId = radioGroupResType.checkedRadioButtonId
        if (selectedResTypeId != R.id.radioPickup && selectedResTypeId != R.id.radioDelivery) {
            Toast.makeText(requireContext(), "예약 유형을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val resType = if (selectedResTypeId == R.id.radioPickup) "픽업" else "배달"

        val transactionLocation = etTransactionLocation.text.toString().trim()
        if (transactionLocation.isEmpty()) {
            Toast.makeText(requireContext(), "거래 장소를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 예약 항목 최소 한 개 이상
        if (itemListContainer.childCount == 0) {
            Toast.makeText(requireContext(), "최소 한 개의 예약 항목을 추가하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val reservationItems = mutableListOf<ReservationItem>()
        for (i in 0 until itemListContainer.childCount) {
            val child = itemListContainer.getChildAt(i)
            val etItemName = child.findViewById<EditText>(R.id.etItemName1)
            val etItemPrice = child.findViewById<EditText>(R.id.etItemPrice1)
            val spinnerCategory = child.findViewById<Spinner>(R.id.spinnerCategory1)
            val etItemMemo = child.findViewById<EditText>(R.id.etItemMemo1)

            val itemName = etItemName.text.toString().trim()
            val price = etItemPrice.text.toString().trim().toIntOrNull() ?: -1
            val category = spinnerCategory.selectedItem.toString()
            val memo = etItemMemo.text.toString().trim()

            if (itemName.isEmpty()) {
                Toast.makeText(requireContext(), "${i + 1}번 항목: 상품명을 입력하세요.", Toast.LENGTH_SHORT).show()
                return
            }
            if (price < 0) {
                Toast.makeText(requireContext(), "${i + 1}번 항목: 가격을 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
                return
            }
            reservationItems.add(ReservationItem(itemName, price, category, memo))
        }

        // 푸시 알림 설정
        var pushSetting: PushSetting? = null
        if (cbPushAlert.isChecked) {
            val selectedPushId = radioGroupPushTime.checkedRadioButtonId
            when (selectedPushId) {
                R.id.radio5Min -> pushSetting = PushSetting(relativeMin = 5)
                R.id.radio15Min -> pushSetting = PushSetting(relativeMin = 15)
                R.id.radio30Min -> pushSetting = PushSetting(relativeMin = 30)
                R.id.radio1Hour -> pushSetting = PushSetting(relativeMin = 60)
                R.id.radioCustom -> {
                    val customMin = npMinutes.value
                    val customHour = npHours.value
                    pushSetting = PushSetting(relativeMin = customHour * 60 + customMin)
                }
                else -> {
                    Toast.makeText(requireContext(), "푸시 알림 시간을 선택하세요.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        val totalAmount = tvTotalAmount.text.toString().toIntOrNull() ?: 0

        val reservation = Reservation(
            customerName = customerName,
            customerContact = customerContact,
            reservationDateTime = resDate,
            transactionDateTime = transDate,
            reservationType = resType,
            transactionLocation = transactionLocation,
            items = reservationItems,
            totalAmount = totalAmount,
            pushSetting = pushSetting
        )

        // TODO: 실제로는 DB Helper를 이용해 저장하거나, 서버에 전송하는 로직을 구현
        Toast.makeText(requireContext(), "예약이 저장되었습니다.", Toast.LENGTH_SHORT).show()

        parentFragmentManager.popBackStack()
    }
}

/**
 * 텍스트 변경 감지를 위한 간단한 추상 클래스
 */
abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}
    abstract override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
}

/**
 * 예약 항목 데이터 모델
 */
data class ReservationItem(
    val itemName: String,
    val price: Int,
    val category: String,
    val memo: String
)

/**
 * 푸시 알림 설정 데이터 모델 (분 단위)
 */
data class PushSetting(
    val relativeMin: Int
)

/**
 * 예약 전체 데이터 모델
 */
data class Reservation(
    val customerName: String,
    val customerContact: String,
    val reservationDateTime: String,
    val transactionDateTime: String,
    val reservationType: String,     // "픽업" 또는 "배달"
    val transactionLocation: String,
    val items: List<ReservationItem>,
    val totalAmount: Int,
    val pushSetting: PushSetting?    // null이면 알림 없음
)
