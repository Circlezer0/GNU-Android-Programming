package com.example.gnu_android_programming.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.home.AddEntryFragment

class ReservationFragment : Fragment() {
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var tvMonthYear: TextView
    private lateinit var btnAddReservation: ImageButton

    // (실제 데이터 조회용 DB Helper 등 필요 시 선언)
    // private lateinit var reservationDBHelper: ReservationDBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // reservationDBHelper = ReservationDBHelper(requireContext())

        // 1) 상단 달력 내비게이션 바인딩
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)

        // 기본적으로 현재 달을 표시하도록 (예시: 2025년 05월)
        tvMonthYear.text = "2025년 05월"

        btnPrevMonth.setOnClickListener {
            // TODO: 달력 월 정보를 “이전달”로 변경 (e.g. Calendar 계산 후 문자열 포맷)
            Toast.makeText(requireContext(), "이전달 클릭됨", Toast.LENGTH_SHORT).show()
        }

        btnNextMonth.setOnClickListener {
            // TODO: 달력 월 정보를 “다음달”로 변경
            Toast.makeText(requireContext(), "다음달 클릭됨", Toast.LENGTH_SHORT).show()
        }

        // 2) 예약 항목 토글 로직 (예시: 하드코딩 아이템 1,2번)
        val container = view.findViewById<LinearLayout>(R.id.container_reservations)

        // 항목 1
        val ivToggle1 = container.findViewById<ImageView>(R.id.ivToggle1)
        val expandedArea1 = container.findViewById<LinearLayout>(R.id.expanded_area1)
        ivToggle1.setOnClickListener {
            if (expandedArea1.visibility == View.GONE) {
                expandedArea1.visibility = View.VISIBLE
                ivToggle1.setImageResource(R.drawable.ic_arrow_drop_up) // 접힐 때 화살표 위쪽
            } else {
                expandedArea1.visibility = View.GONE
                ivToggle1.setImageResource(R.drawable.ic_arrow_drop_down)
            }
        }

        // 항목 2
        val ivToggle2 = container.findViewById<ImageView>(R.id.ivToggle2)
        val expandedArea2 = container.findViewById<LinearLayout>(R.id.expanded_area2)
        ivToggle2.setOnClickListener {
            if (expandedArea2.visibility == View.GONE) {
                expandedArea2.visibility = View.VISIBLE
                ivToggle2.setImageResource(R.drawable.ic_arrow_drop_up)
            } else {
                expandedArea2.visibility = View.GONE
                ivToggle2.setImageResource(R.drawable.ic_arrow_drop_down)
            }
        }

        // (실제 구현 시 RecyclerView 어댑터 내 ViewHolder에서 처리)

        // 3) 하단 “+” 버튼 → 예약 추가 Fragment로 이동
        btnAddReservation = view.findViewById(R.id.fabAddReservation)
        btnAddReservation.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReservationAddFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}