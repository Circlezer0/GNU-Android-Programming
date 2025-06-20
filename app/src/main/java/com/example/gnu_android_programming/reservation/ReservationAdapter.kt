// ReservationAdapter.kt
package com.example.gnu_android_programming.reservation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R

/**
 * 예약 목록을 RecyclerView에 표시하고
 * 각 예약에 대한 수정/삭제 및 상세 토글 기능을 처리하는 Adapter
 * @param data 예약 데이터 리스트 (mutable)
 * @param listener 예약 수정/삭제 이벤트 리스너
 */
class ReservationAdapter(
    private val data: MutableList<ReservationData>,  // 예약 데이터 리스트
    private val listener: OnReservationActionListener // 수정/삭제 콜백 리스너
) : RecyclerView.Adapter<ReservationAdapter.ResViewHolder>() {

    /**
     * 예약 항목 수정/삭제 시 호출되는 콜백 인터페이스
     */
    interface OnReservationActionListener {
        /** 예약 수정 요청 처리 */
        fun onEdit(reservation: ReservationData)
        /** 예약 삭제 요청 처리 */
        fun onDelete(reservation: ReservationData)
    }

    /**
     * 예약 뷰의 각 UI 컴포넌트를 보관하는 ViewHolder
     * @property tvCustomerName 고객 이름 표시 TextView
     * @property tvResDate 예약 일시 표시 TextView
     * @property tvResType 예약 타입 표시 TextView
     * @property ivToggle 상세 영역 토글 ImageView
     * @property expanded 상세 영역 LinearLayout
     * @property tvTransDate 거래 날짜 표시 TextView
     * @property tvAddress 거래 장소 표시 TextView
     * @property tvContact 고객 연락처 표시 TextView
     * @property ivCall 전화걸기 ImageView
     * @property rvItemList 예약 항목 목록 RecyclerView
     * @property tvTotal 합계 금액 표시 TextView
     * @property tvPushInfo 푸시 알림 설정 정보 표시 TextView
     * @property btnEdit 수정 버튼
     * @property btnDelete 삭제 버튼
     */
    inner class ResViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvCustomerName: TextView = v.findViewById(R.id.tvCustomerName)
        val tvResDate: TextView = v.findViewById(R.id.tvResDate)
        val tvResType: TextView = v.findViewById(R.id.tvResType)
        val ivToggle: ImageView = v.findViewById(R.id.ivToggle)
        val expanded: LinearLayout = v.findViewById(R.id.expandedArea)

        val tvTransDate: TextView = v.findViewById(R.id.tvTransDateDetail)
        val tvAddress: TextView = v.findViewById(R.id.tvAddress)
        val tvContact: TextView = v.findViewById(R.id.tvContact)
        val ivCall: ImageView = v.findViewById(R.id.ivCall)
        val rvItemList: RecyclerView = v.findViewById(R.id.rvItemList)
        val tvTotal: TextView = v.findViewById(R.id.tvTotal)
        val tvPushInfo: TextView = v.findViewById(R.id.tvPushInfo)

        val btnEdit: Button = v.findViewById(R.id.btnEdit)
        val btnDelete: Button = v.findViewById(R.id.btnDelete)
    }

    /**
     * 새로운 ViewHolder 생성 시 호출
     * @param parent 부모 ViewGroup
     * @param viewType 아이템 뷰 타입
     * @return 생성된 ResViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false) // item_reservation 레이아웃 inflate
        return ResViewHolder(view)
    }

    /**
     * ViewHolder에 데이터 바인딩 및 이벤트 설정
     * @param holder 바인딩할 ViewHolder
     * @param pos 아이템 위치
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ResViewHolder, pos: Int) {
        val item = data[pos]

        // ── 1) 헤더 정보 표시 ──
        holder.tvCustomerName.text = item.customerName  // 고객명
        holder.tvResDate.text = item.reservationDateTime // 예약 일시
        holder.tvResType.text = item.reservationType     // 예약 유형

        // 토글 버튼 이미지 설정 (펼침/접힘)
        holder.ivToggle.setImageResource(
            if (holder.expanded.visibility == View.VISIBLE)
                R.drawable.ic_arrow_drop_up
            else
                R.drawable.ic_arrow_drop_down
        )
        holder.ivToggle.setOnClickListener {
            // 상세 영역 보이기/숨기기 토글
            if (holder.expanded.visibility == View.VISIBLE) {
                holder.expanded.visibility = View.GONE
                holder.ivToggle.setImageResource(R.drawable.ic_arrow_drop_down)
            } else {
                holder.expanded.visibility = View.VISIBLE
                holder.ivToggle.setImageResource(R.drawable.ic_arrow_drop_up)
            }
        }

        // ── 2) 상세 주소·연락처 표시 ──
        holder.tvTransDate.text = "거래날짜: ${item.transactionDateTime}"
        holder.tvAddress.text = "거래장소: ${item.transactionLocation}"
        holder.tvContact.text = "연락처: ${item.customerContact}"

        // 전화 걸기 아이콘 클릭 시 다이얼러 실행
        holder.ivCall.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${item.customerContact}")
            }
            holder.itemView.context.startActivity(dialIntent)
        }

        // ── 3) 예약 항목 RecyclerView 설정 ──
        holder.rvItemList.apply {
            layoutManager = LinearLayoutManager(context)      // 세로 스크롤
            adapter = ReservationItemAdapter(item.items)      // 하위 아이템 어댑터
            isNestedScrollingEnabled = true

            // 스크롤 시 부모가 터치 이벤트 가로채지 않도록 설정
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
                }
                false // RecyclerView 터치 이벤트 정상 동작
            }
        }

        // ── 4) 총 합계 금액 표시 ──
        holder.tvTotal.text = "${item.totalAmount}원"

        // ── 5) 푸시 알림 설정 정보 표시 ──
        holder.tvPushInfo.text = item.pushSetting?.relativeMin?.let { min ->
            val h = min / 60  // 시간 단위
            val m = min % 60  // 분 단위
            buildString {
                append("푸시: ")
                if (h > 0) append("${h}시간 ")
                if (m > 0) append("${m}분 ")
                append("전")
            }
        } ?: "푸시: 없음"

        // ── 6) 수정/삭제 버튼 이벤트 연결 ──
        holder.btnEdit.setOnClickListener { listener.onEdit(item) }
        holder.btnDelete.setOnClickListener { listener.onDelete(item) }
    }

    /**
     * Adapter 에 표시할 아이템 총 개수 반환
     */
    override fun getItemCount(): Int = data.size

    /**
     * 데이터 리스트를 새로운 리스트로 교체하고
     * 전체 갱신 처리
     * @param newList 새 예약 데이터 리스트
     */
    fun replace(newList: List<ReservationData>) {
        data.clear()       // 기존 데이터 초기화
        data.addAll(newList) // 새 데이터 추가
        notifyDataSetChanged() // 전체 아이템 갱신
    }
}
