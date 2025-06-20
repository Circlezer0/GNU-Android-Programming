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
import com.example.gnu_android_programming.cancelReservationAlarm
import com.example.gnu_android_programming.database.ReservationDao
import com.example.gnu_android_programming.scheduleReservationAlarm
import java.text.SimpleDateFormat
import java.util.*

/**
 * 기존 예약 정보를 수정하는 화면을 제공하는 Fragment
 * 전달된 ReservationData를 기반으로 입력 필드를 채우고
 * 수정 내용 저장 시 DB 업데이트 및 알림 스케줄을 조정함
 */
class ReservationEditFragment : Fragment() {

    companion object {
        private const val ARG_RES = "arg_reservation"
        /**
         * 주어진 ReservationData로 Fragment 인스턴스를 생성
         * @param res 수정할 ReservationData 객체
         */
        fun newInstance(res: ReservationData): ReservationEditFragment =
            ReservationEditFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_RES, res) }
            }
    }

    // DB 접근용 DAO
    private lateinit var reservationDao: ReservationDao
    // 수정 대상 예약 데이터
    private lateinit var reservation: ReservationData

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
    private lateinit var itemListContainer: LinearLayout// 동적 항목 뷰 컨테이너
    private lateinit var btnAddItem: Button             // 항목 추가 버튼
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
    private lateinit var btnSave: Button                // 저장 버튼

    // 카테고리 목록 상수
    private val categories = arrayOf("식물", "화분", "분갈이", "토분")

    /**
     * Fragment 생성 시 전달된 ReservationData 객체 초기화
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservation = requireArguments().getSerializable(ARG_RES) as ReservationData
    }

    /**
     * 레이아웃을 inflate하여 뷰 생성
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_reservation_add, container, false)

    /**
     * 뷰 생성 이후 초기 설정 및 데이터 바인딩
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reservationDao = ReservationDao(requireContext()) // DAO 초기화

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
        itemListContainer    = view.findViewById(R.id.item_list_container)
        btnAddItem           = view.findViewById(R.id.btnAddItem)
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
        btnSave              = view.findViewById(R.id.btnSaveReservation)

        // --- 이벤트 리스너 등록 ---
        btnPickResDate.setOnClickListener    { pickDateTime(etReservationDate) }
        btnPickTransDate.setOnClickListener  { pickDateTime(etTransactionDate) }
        cbPushAlert.setOnCheckedChangeListener { _, checked ->
            // 체크 시 라디오 그룹 보이기, 기본 5분 전 선택
            radioGroupPushTime.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) radio5Min.isChecked = true
        }
        radioGroupPushTime.setOnCheckedChangeListener { _, id ->
            // 사용자 지정 선택 시 입력 레이아웃 표시
            layoutCustomTime.visibility = if (id == R.id.radioCustom) View.VISIBLE else View.GONE
        }
        btnAddItem.setOnClickListener        { addNewItemView() }

        // --- 전달된 데이터로 초기화 ---
        etCustomerName.setText(reservation.customerName)
        etCustomerContact.setText(reservation.customerContact)
        etReservationDate.setText(reservation.reservationDateTime)
        etTransactionDate.setText(reservation.transactionDateTime)
        if (reservation.reservationType == "픽업") radioPickup.isChecked = true
        else radioDelivery.isChecked = true
        etTransactionLocation.setText(reservation.transactionLocation)

        // NumberPicker 범위 설정
        npHours.minValue = 0; npHours.maxValue = 23
        npMinutes.minValue = 0; npMinutes.maxValue = 59

        // 기존 아이템 리스트 표시
        itemListContainer.removeAllViews()
        reservation.items.forEach { addNewItemView(it) }
        updateTotalAmount()

        // 푸시 설정 초기화
        if (reservation.pushSetting != null) {
            cbPushAlert.isChecked = true
            radioGroupPushTime.visibility = View.VISIBLE
            val min = reservation.pushSetting!!.relativeMin
            val h = min / 60; val m = min % 60
            when {
                min == 5   -> radio5Min.isChecked = true
                min == 15  -> radio15Min.isChecked = true
                min == 30  -> radio30Min.isChecked = true
                min == 60  -> radio1Hour.isChecked = true
                else -> {
                    radioCustom.isChecked = true
                    layoutCustomTime.visibility = View.VISIBLE
                    npHours.value   = h
                    npMinutes.value = m
                }
            }
        } else {
            cbPushAlert.isChecked = false
            radioGroupPushTime.visibility = View.GONE
            layoutCustomTime.visibility  = View.GONE
        }

        // 저장 버튼 클릭 시 수정 처리
        btnSave.setOnClickListener {
            collectInputsInto(reservation)              // 입력 수집
            cancelReservationAlarm(requireContext(), reservation.id!!) // 기존 알림 취소
            reservationDao.update(reservation)          // DB 업데이트
            if (reservation.pushSetting != null) {
                scheduleReservationAlarm(requireContext(), reservation) // 알림 재설정
            }
            parentFragmentManager.popBackStack()        // 화면 복귀
        }
    }

    /**
     * DatePicker와 TimePicker를 순차적으로 표시하여
     * 선택된 날짜/시간을 target EditText에 설정
     * @param target 값을 설정할 EditText
     */
    private fun pickDateTime(target: EditText) {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            TimePickerDialog(requireContext(), { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                target.setText(fmt.format(cal.time))     // 선택된 값 표시
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     * 동적 예약 항목 뷰를 생성하고, 삭제 및 가격 변경 감지 리스너를 등록
     * @param prefill 기존 데이터가 있을 경우 해당 값으로 뷰 초기화
     */
    private fun addNewItemView(prefill: ReservationItemData? = null) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_reservation_add_item, itemListContainer, false)
        // 삭제 버튼 클릭 시 뷰 제거
        itemView.findViewById<TextView>(R.id.tvDeleteItem).setOnClickListener {
            itemListContainer.removeView(itemView)
            updateTotalAmount()
        }
        // 가격 입력 변경 시 총합 갱신
        itemView.findViewById<EditText>(R.id.etItemPrice)
            .addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, cnt: Int) {
                    updateTotalAmount()
                }
            })
        // 카테고리 Spinner 초기화
        val spinner = itemView.findViewById<Spinner>(R.id.spinnerCategory)
        spinner.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, categories
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        // 기존 데이터로 채우기
        prefill?.let {
            itemView.findViewById<EditText>(R.id.etItemName).setText(it.itemName)
            itemView.findViewById<EditText>(R.id.etItemPrice).setText(it.price.toString())
            spinner.setSelection(categories.indexOf(it.category))
            itemView.findViewById<EditText>(R.id.etItemMemo).setText(it.memo)
        }
        itemListContainer.addView(itemView) // 뷰 추가
        updateTotalAmount()                // 합계 갱신
    }

    /**
     * 현재 모든 예약 항목의 가격을 합산하여 총 합계 TextView에 반영
     */
    private fun updateTotalAmount() {
        var sum = 0
        for (i in 0 until itemListContainer.childCount) {
            val c = itemListContainer.getChildAt(i)
            sum += c.findViewById<EditText>(R.id.etItemPrice)
                .text.toString().toIntOrNull() ?: 0
        }
        tvTotalAmount.text = sum.toString()              // 합계 반영
    }

    /**
     * 사용자 입력값을 주어진 ReservationData 객체에 반영
     * @param res 수정 대상 ReservationData
     */
    private fun collectInputsInto(res: ReservationData) {
        res.customerName         = etCustomerName.text.toString()
        res.customerContact      = etCustomerContact.text.toString()
        res.reservationDateTime  = etReservationDate.text.toString()
        res.transactionDateTime  = etTransactionDate.text.toString()
        res.reservationType      = if (radioPickup.isChecked) "픽업" else "배달"
        res.transactionLocation  = etTransactionLocation.text.toString()

        // 항목 리스트 재구성
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
        res.items       = items                             // 리스트 설정
        res.totalAmount = tvTotalAmount.text.toString().toIntOrNull() ?: 0

        // 푸시 알림 설정 반영
        val pushMin = if (cbPushAlert.isChecked) {
            when (radioGroupPushTime.checkedRadioButtonId) {
                R.id.radio5Min   -> 5
                R.id.radio15Min  -> 15
                R.id.radio30Min  -> 30
                R.id.radio1Hour  -> 60
                else             -> npHours.value * 60 + npMinutes.value
            }
        } else null
        res.pushSetting = pushMin?.let { PushSettingData(it) }
    }
}
