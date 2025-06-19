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

class ReservationAdapter(
    private val data: MutableList<ReservationData>,
    private val listener: OnReservationActionListener
) : RecyclerView.Adapter<ReservationAdapter.ResViewHolder>() {

    /** 시그니처: 수정·삭제는 ReservationData 하나만 전달 */
    interface OnReservationActionListener {
        fun onEdit(reservation: ReservationData)
        fun onDelete(reservation: ReservationData)
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ResViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ResViewHolder, pos: Int) {
        val item = data[pos]

        // ── 1) 헤더 ──
        holder.tvCustomerName.text = item.customerName
        holder.tvResDate.text = item.reservationDateTime
        holder.tvResType.text = item.reservationType

        holder.ivToggle.setImageResource(
            if (holder.expanded.visibility == View.VISIBLE)
                R.drawable.ic_arrow_drop_up
            else
                R.drawable.ic_arrow_drop_down
        )
        holder.ivToggle.setOnClickListener {
            if (holder.expanded.visibility == View.VISIBLE) {
                holder.expanded.visibility = View.GONE
                holder.ivToggle.setImageResource(R.drawable.ic_arrow_drop_down)
            } else {
                holder.expanded.visibility = View.VISIBLE
                holder.ivToggle.setImageResource(R.drawable.ic_arrow_drop_up)
            }
        }

        // ── 2) 상세 주소·연락처 ──
        holder.tvTransDate.text = "거래날짜: ${item.transactionDateTime}"
        holder.tvAddress.text = "거래장소: ${item.transactionLocation}"
        holder.tvContact.text = "연락처: ${item.customerContact}"

        holder.ivCall.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${item.customerContact}")
            }
            holder.itemView.context.startActivity(dialIntent)
        }
        // ── 3) 예약 항목 ──
        holder.rvItemList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReservationItemAdapter(item.items)
            isNestedScrollingEnabled = true

            // ↓ 터치 시작부터 내부가 스크롤 될 때까지
            //    부모가 intercept 하지 못하도록 명시적으로 막아줍니다.
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                // false 로 두면 RecyclerView 의 onTouchEvent 도 정상 동작
                false
            }
        }

        // ── 4) 합계 ──
        holder.tvTotal.text = "${item.totalAmount}원"

        // ── 5) 푸시 ──
        holder.tvPushInfo.text = item.pushSetting?.relativeMin?.let { min ->
            val h = min / 60;
            val m = min % 60
            buildString {
                append("푸시: ")
                if (h > 0) append("${h}시간 ")
                if (m > 0) append("${m}분 ")
                append("전")
            }
        } ?: "푸시: 없음"

        // ── 6) 수정/삭제 버튼 ──
        holder.btnEdit.setOnClickListener { listener.onEdit(item) }
        holder.btnDelete.setOnClickListener { listener.onDelete(item) }
    }

    override fun getItemCount(): Int = data.size

    fun replace(newList: List<ReservationData>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}