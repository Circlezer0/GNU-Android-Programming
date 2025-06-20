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

/**
 * 예약 추가 화면을 관리하는 Fragment
 * 사용자 입력에 따라 날짜/시간 선택, 항목 추가,
 * 총 합계 계산, DB 저장 및 알림 스케줄링을 처리함
 */
class ReservationAddFragment : Fragment() {

    // --- 뷰 바인딩 변수들 ---
    private lateinit var etCustomerName: EditText       // 고객 이름 입력 필드
    private lateinit var etCustomerContact: EditText    // 고객 연락처 입력 필드
    private lateinit var etReservationDate: EditText    // 예약 일시 표시 EditText
    private lateinit var btnPickResDate: ImageButton    // 예약 일시 선택 버튼
    private lateinit var etTransactionDate: EditText    // 거래 일시 표시 EditText
    private lateinit var btnPickTransDate: ImageButton  // 거래 일시 선택 버튼
    private lateinit var radioGroupResType: RadioGroup  // 픽업/배달 선택 그룹
    private lateinit var radioPickup: RadioButton       // 픽업 옵션
    private lateinit var radioDelivery: RadioButton     // 배달 옵션
    private lateinit var etTransactionLocation: EditText// 거래 장소 입력 필드
    private lateinit var btnAddItem: Button             // 항목 추가 버튼
    private lateinit var itemListContainer: LinearLayout// 동적 항목 뷰 컨테이너
    private lateinit var tvTotalAmount: TextView        // 총 합계 금액 표시
    private lateinit var cbPushAlert: CheckBox          // 푸시 알림 여부 선택
    private lateinit var radioGroupPushTime: RadioGroup // 푸시 알림 시간 선택 그룹
    private lateinit var radio5Min: RadioButton         // 5분 전
    private lateinit var radio15Min: RadioButton        // 15분 전
    private lateinit var radio30Min: RadioButton        // 30분 전
    private lateinit var radio1Hour: RadioButton        // 1시간 전
    private lateinit var radioCustom: RadioButton       // 사용자 지정 알림
    private lateinit var layoutCustomTime: LinearLayout // 사용자 지정 시간 입력 레이아웃
    private lateinit var npHours: NumberPicker          // 사용자 지정 - 시간
    private lateinit var npMinutes: NumberPicker        // 사용자 지정 - 분
    private lateinit var btnSaveReservation: Button     // 저장 버튼

    // --- DB 접근용 DAO ---
    private lateinit var reservationDao: ReservationDao

    /**
     * Fragment 뷰 생성 시 레이아웃 inflate
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_reservation_add, container, false)

    /**
     * 뷰 생성 후 초기 설정 및 리스너 등록
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reservationDao = ReservationDao(requireContext()) // DAO 초기화

        // --- 뷰 바인딩 ---
        etCustomerName        = view.findViewById(R.id.etCustomerName)
        etCustomerContact     = view.findViewById(R.id.etCustomerContact)
        etReservationDate     = view.findViewById(R.id.etReservationDate)
        btnPickResDate        = view.findViewById(R.id.btnPickResDate)
        etTransactionDate     = view.findViewById(R.id.etTransactionDate)
        btnPickTransDate      = view.findViewById(R.id.btnPickTransDate)
        radioGroupResType     = view.findViewById(R.id.radioGroupResType)
        radioPickup           = view.findViewById(R.id.radioPickup)
        radioDelivery         = view.findViewById(R.id.radioDelivery)
        etTransactionLocation = view.findViewById(R.id.etTransactionLocation)
        btnAddItem            = view.findViewById(R.id.btnAddItem)
        itemListContainer     = view.findViewById(R.id.item_list_container)
        tvTotalAmount         = view.findViewById(R.id.tvTotalAmount)
        cbPushAlert           = view.findViewById(R.id.cbPushAlert)
        radioGroupPushTime    = view.findViewById(R.id.radioGroupPushTime)
        radio5Min             = view.findViewById(R.id.radio5Min)
        radio15Min            = view.findViewById(R.id.radio15Min)
        radio30Min            = view.findViewById(R.id.radio30Min)
        radio1Hour            = view.findViewById(R.id.radio1Hour)
        radioCustom           = view.findViewById(R.id.radioCustom)
        layoutCustomTime      = view.findViewById(R.id.layoutCustomTime)
        npHours               = view.findViewById(R.id.npHours)
        npMinutes             = view.findViewById(R.id.npMinutes)
        btnSaveReservation    = view.findViewById(R.id.btnSaveReservation)

        // --- 초기값 설정 ---
        val now = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        etReservationDate.setText(fmt.format(now.time))                 // 예약일시 현재시간
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        etTransactionDate.setText(fmt.format(tomorrow.time))            // 거래일시 내일

        radioPickup.isChecked = true                                    // 기본 픽업
        npHours.minValue = 0; npHours.maxValue = 23; npHours.wrapSelectorWheel = true
        npMinutes.minValue = 0; npMinutes.maxValue = 59; npMinutes.wrapSelectorWheel = true

        // --- 리스너 등록 ---
        btnPickResDate.setOnClickListener   { pickDateTime(etReservationDate) }
        btnPickTransDate.setOnClickListener { pickDateTime(etTransactionDate) }
        btnAddItem.setOnClickListener       { addNewItemView() }
        cbPushAlert.setOnCheckedChangeListener { _, checked ->
            radioGroupPushTime.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) radio5Min.isChecked = true                   // 푸시 체크 시 기본 5분
        }
        radioGroupPushTime.setOnCheckedChangeListener { _, id ->
            layoutCustomTime.visibility = if (id == R.id.radioCustom) View.VISIBLE else View.GONE
        }
        btnSaveReservation.setOnClickListener { saveReservation() }
    }

    /**
     * DatePicker + TimePicker 순차적으로 표시하여
     * 선택된 날짜/시간을 target EditText에 설정
     */
    private fun pickDateTime(target: EditText) {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            TimePickerDialog(requireContext(), { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                target.setText(fmt.format(cal.time))                   // 선택 값 반영
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     * 동적 예약 항목 뷰 생성 및 리스너 설정
     */
    private fun addNewItemView() {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_reservation_add_item, itemListContainer, false)
        // 삭제 버튼 클릭 시 뷰 제거 및 합계 갱신
        itemView.findViewById<TextView>(R.id.tvDeleteItem).setOnClickListener {
            itemListContainer.removeView(itemView)
            updateTotalAmount()
        }
        // 가격 입력 시 총합 갱신
        itemView.findViewById<EditText>(R.id.etItemPrice)
            .addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateTotalAmount()
                }
            })
        // 카테고리 Spinner 초기화
        val cats = arrayOf("식물", "화분", "분갈이", "토분")
        itemView.findViewById<Spinner>(R.id.spinnerCategory).adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, cats
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        // 컨테이너에 추가 및 합계 갱신
        itemListContainer.addView(itemView)
        updateTotalAmount()
    }

    /**
     * 모든 예약 항목의 가격을 합산하여 총합을 업데이트
     */
    private fun updateTotalAmount() {
        var sum = 0
        for (i in 0 until itemListContainer.childCount) {
            val child = itemListContainer.getChildAt(i)
            sum += child.findViewById<EditText>(R.id.etItemPrice).text.toString().toIntOrNull() ?: 0
        }
        tvTotalAmount.text = sum.toString()                           // 합계 반영
    }

    /**
     * 저장 클릭 시 입력값 수집, DB 저장, 알림 스케줄링, 뒤로 가기 처리
     */
    private fun saveReservation() {
        // 1) 아이템 리스트 수집
        val items = mutableListOf<ReservationItemData>()
        for (i in 0 until itemListContainer.childCount) {
            val c = itemListContainer.getChildAt(i)
            items += ReservationItemData(
                itemName  = c.findViewById<EditText>(R.id.etItemName).text.toString(),
                price     = c.findViewById<EditText>(R.id.etItemPrice).text.toString().toIntOrNull() ?: 0,
                category  = c.findViewById<Spinner>(R.id.spinnerCategory).selectedItem.toString(),
                memo      = c.findViewById<EditText>(R.id.etItemMemo).text.toString()
            )
        }
        // 2) 푸시 알림 분 단위 결정
        val pushMin = if (cbPushAlert.isChecked) when (radioGroupPushTime.checkedRadioButtonId) {
            R.id.radio5Min   -> 5
            R.id.radio15Min  -> 15
            R.id.radio30Min  -> 30
            R.id.radio1Hour  -> 60
            R.id.radioCustom -> npHours.value * 60 + npMinutes.value
            else             -> null
        } else null
        // 3) DTO 생성
        val data = ReservationData(
            customerName         = etCustomerName.text.toString(),
            customerContact      = etCustomerContact.text.toString(),
            reservationDateTime  = etReservationDate.text.toString(),
            transactionDateTime  = etTransactionDate.text.toString(),
            reservationType      = if (radioPickup.isChecked) "픽업" else "배달",
            transactionLocation  = etTransactionLocation.text.toString(),
            items                = items,
            totalAmount          = tvTotalAmount.text.toString().toInt(),
            pushSetting          = pushMin?.let { PushSettingData(it) }
        )
        // 4) DB 삽입 및 ID 설정
        val newId = reservationDao.insert(data)
        data.id = newId
        // 5) 알림 스케줄링 및 화면 복귀
        scheduleReservationAlarm(requireContext(), data)
        parentFragmentManager.popBackStack()
    }

    /**
     * 가격 입력 시 변화를 감지하기 위한 단순 TextWatcher
     */
    abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }
}
