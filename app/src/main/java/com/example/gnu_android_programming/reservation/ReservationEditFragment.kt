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

class ReservationEditFragment : Fragment() {

    companion object {
        private const val ARG_RES = "arg_reservation"
        fun newInstance(res: ReservationData): ReservationEditFragment =
            ReservationEditFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_RES, res) }
            }
    }

    private lateinit var reservationDao: ReservationDao
    private lateinit var reservation: ReservationData

    // 뷰 바인딩
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
    private lateinit var npHours: NumberPicker
    private lateinit var npMinutes: NumberPicker
    private lateinit var btnSave: Button

    private val categories = arrayOf("식물","화분","분갈이","토분")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservation = requireArguments().getSerializable(ARG_RES) as ReservationData
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_reservation_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reservationDao = ReservationDao(requireContext())

        // 1) 바인딩
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

        // 2) 리스너 등록
        btnPickResDate.setOnClickListener    { pickDateTime(etReservationDate) }
        btnPickTransDate.setOnClickListener  { pickDateTime(etTransactionDate) }
        cbPushAlert.setOnCheckedChangeListener { _, checked ->
            // 체크 시 라디오 그룹 보이기, 첫번째 기본 선택
            radioGroupPushTime.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) radio5Min.isChecked = true
        }
        radioGroupPushTime.setOnCheckedChangeListener { _, id ->
            layoutCustomTime.visibility = if (id == R.id.radioCustom) View.VISIBLE else View.GONE
        }
        btnAddItem.setOnClickListener        { addNewItemView() }

        // 3) 기본값 및 전달된 데이터로 초기화
        etCustomerName.setText(reservation.customerName)
        etCustomerContact.setText(reservation.customerContact)
        etReservationDate.setText(reservation.reservationDateTime)
        etTransactionDate.setText(reservation.transactionDateTime)
        if (reservation.reservationType == "픽업") radioPickup.isChecked = true
        else radioDelivery.isChecked = true
        etTransactionLocation.setText(reservation.transactionLocation)

        // NumberPicker 범위
        npHours.minValue = 0; npHours.maxValue = 23
        npMinutes.minValue = 0; npMinutes.maxValue = 59

        // 아이템 리스트
        itemListContainer.removeAllViews()
        reservation.items.forEach { addNewItemView(it) }
        updateTotalAmount()

        // 푸시 설정
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
                else       -> {
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

        // 4) 저장 버튼
        btnSave.setOnClickListener {
            collectInputsInto(reservation)
            cancelReservationAlarm(requireContext(), reservation.id!!)
            reservationDao.update(reservation)
            if (reservation.pushSetting != null) {
                scheduleReservationAlarm(requireContext(), reservation)
            }
            parentFragmentManager.popBackStack()
        }
    }

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

    private fun addNewItemView(prefill: ReservationItemData? = null) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_reservation_add_item, itemListContainer, false)

        itemView.findViewById<TextView>(R.id.tvDeleteItem).setOnClickListener {
            itemListContainer.removeView(itemView)
            updateTotalAmount()
        }
        itemView.findViewById<EditText>(R.id.etItemPrice)
            .addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, cnt: Int) {
                    updateTotalAmount()
                }
            })

        val spinner = itemView.findViewById<Spinner>(R.id.spinnerCategory)
        spinner.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, categories
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        prefill?.let {
            itemView.findViewById<EditText>(R.id.etItemName).setText(it.itemName)
            itemView.findViewById<EditText>(R.id.etItemPrice).setText(it.price.toString())
            spinner.setSelection(categories.indexOf(it.category))
            itemView.findViewById<EditText>(R.id.etItemMemo).setText(it.memo)
        }

        itemListContainer.addView(itemView)
        updateTotalAmount()
    }

    private fun updateTotalAmount() {
        var sum = 0
        for (i in 0 until itemListContainer.childCount) {
            val c = itemListContainer.getChildAt(i)
            val price = c.findViewById<EditText>(R.id.etItemPrice)
                .text.toString().toIntOrNull() ?: 0
            sum += price
        }
        tvTotalAmount.text = sum.toString()
    }

    private fun collectInputsInto(res: ReservationData) {
        res.customerName         = etCustomerName.text.toString()
        res.customerContact      = etCustomerContact.text.toString()
        res.reservationDateTime  = etReservationDate.text.toString()
        res.transactionDateTime  = etTransactionDate.text.toString()
        res.reservationType      = if (radioPickup.isChecked) "픽업" else "배달"
        res.transactionLocation  = etTransactionLocation.text.toString()

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
        res.items       = items
        res.totalAmount = tvTotalAmount.text.toString().toIntOrNull() ?: 0

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
