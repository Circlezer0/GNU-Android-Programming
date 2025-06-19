package com.example.gnu_android_programming.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.cancelReservationAlarm
import com.example.gnu_android_programming.database.ReservationDao
import java.util.*

/**
 * 예약 목록을 월 단위로 보여주고
 * 예약 추가, 수정, 삭제 기능을 제공하는 Fragment
 */
class ReservationFragment : Fragment() {

    // --- 뷰 컴포넌트 ---
    private lateinit var btnPrevMonth: Button          // 이전 달 이동 버튼
    private lateinit var btnNextMonth: Button          // 다음 달 이동 버튼
    private lateinit var tvMonthYear: TextView         // 현재 표시 중인 연월 텍스트뷰
    private lateinit var fabAddReservation: FloatingActionButton // 예약 추가 버튼
    private lateinit var rv: RecyclerView             // 예약 목록 RecyclerView
    private lateinit var adapter: ReservationAdapter  // RecyclerView 어댑터
    private lateinit var reservationDao: ReservationDao // DB 접근 객체

    // 현재 표시 중인 연월을 관리하는 Calendar 객체
    private val current = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트 레이아웃을 inflate
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DAO 초기화 (Application context 사용)
        reservationDao = ReservationDao(requireContext())

        // 뷰 바인딩
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        fabAddReservation = view.findViewById(R.id.fabAddReservation)
        rv = view.findViewById(R.id.rvReservations)

        // RecyclerView 설정: 수직 스크롤, LinearLayoutManager 사용
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservationAdapter(
            mutableListOf(),
            object : ReservationAdapter.OnReservationActionListener {
                override fun onEdit(res: ReservationData) {
                    // 수정 버튼 클릭 시 ReservationEditFragment 로 전환
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ReservationEditFragment.newInstance(res)
                        )
                        .addToBackStack(null)
                        .commit()
                }

                override fun onDelete(res: ReservationData) {
                    // 삭제 버튼 클릭 시 알람 취소 후 DB에서 삭제
                    cancelReservationAlarm(requireContext(), res.id!!)
                    reservationDao.delete(res.id!!)
                    // 목록 새로 고침
                    refreshList()
                }
            }
        )
        rv.adapter = adapter

        // 이전/다음 달 버튼 리스너
        btnPrevMonth.setOnClickListener { moveMonth(-1) }
        btnNextMonth.setOnClickListener { moveMonth(+1) }

        // 예약 추가 플로팅 액션 버튼 클릭 시 ReservationAddFragment 로 전환
        fabAddReservation.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReservationAddFragment())
                .addToBackStack(null)
                .commit()
        }

        // 초기 목록 표시
        refreshList()
    }

    /**
     * current Calendar 에서 month 를 delta 만큼 이동 후 목록 새로 고침
     * @param delta 변경할 월(음수: 이전 달, 양수: 다음 달)
     */
    private fun moveMonth(delta: Int) {
        current.add(Calendar.MONTH, delta)
        refreshList()
    }

    /**
     * 현재 current 에 설정된 연월 기준으로
     * DB 에서 해당 월의 예약 목록을 가져와
     * 어댑터에 반영하고 화면에 표시
     */
    private fun refreshList() {
        // 연, 월 추출 (month 는 0-based 이므로 +1)
        val year = current.get(Calendar.YEAR)
        val month = current.get(Calendar.MONTH) + 1
        // 상단 텍스트뷰에 "YYYY년 MM월" 형식으로 표시
        tvMonthYear.text = "%d년 %02d월".format(year, month)

        // DAO 로 해당 월 데이터 조회 (month 0-based 사용)
        val list = reservationDao.getOfMonth(year, current.get(Calendar.MONTH))
        // 어댑터에 데이터 교체
        adapter.replace(list)
    }
}
