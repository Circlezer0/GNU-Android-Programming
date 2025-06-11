// ReservationAdapter.kt
package com.example.gnu_android_programming.reservation

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        val tvCustomerName: TextView    = v.findViewById(R.id.tvCustomerName)
        val tvResDate     : TextView    = v.findViewById(R.id.tvResDate)
        val tvResType     : TextView    = v.findViewById(R.id.tvResType)
        val ivToggle      : ImageView   = v.findViewById(R.id.ivToggle)
        val expanded      : LinearLayout= v.findViewById(R.id.expandedArea)

        val tvTransDate   : TextView    = v.findViewById(R.id.tvTransDateDetail)
        val tvAddress     : TextView    = v.findViewById(R.id.tvAddress)
        val tvContact     : TextView    = v.findViewById(R.id.tvContact)
        val ivCall        : ImageView   = v.findViewById(R.id.ivCall)
        val itemList      : LinearLayout= v.findViewById(R.id.itemList)
        val tvTotal       : TextView    = v.findViewById(R.id.tvTotal)
        val tvPushInfo    : TextView    = v.findViewById(R.id.tvPushInfo)

        val btnEdit       : Button      = v.findViewById(R.id.btnEdit)
        val btnDelete     : Button      = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ResViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResViewHolder, pos: Int) {
        val item = data[pos]

        // ── 1) 헤더 ──
        holder.tvCustomerName.text = item.customerName
        holder.tvResDate     .text = item.reservationDateTime
        holder.tvResType     .text = item.reservationType

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
        holder.itemList.removeAllViews()
        item.items.forEach { it ->
            // 1) row+memo 통합 컨테이너
            val container = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.item_divider_bottom
                )
                setPadding(0, 8, 0, 8)
            }

            // 2) 한 줄 row: 이름/유형/가격
            val row = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            row.addView(TextView(holder.itemView.context).apply {
                text = it.itemName; textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 3f)
            })
            row.addView(TextView(holder.itemView.context).apply {
                text = it.category; textSize = 16f; gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 2f)
            })
            row.addView(TextView(holder.itemView.context).apply {
                text = "${it.price}원"; textSize = 16f; gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            })
            container.addView(row)

            // 3) 메모 추가 (있을 때만)
            if (it.memo.isNotBlank()) {
                val tvMemo = TextView(holder.itemView.context).apply {
                    text = "메모: ${it.memo}"
                    textSize = 14f
                    setPadding(16, 4, 0, 0)
                    setTypeface(typeface, android.graphics.Typeface.ITALIC)
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                }
                container.addView(tvMemo)
            }

            // 4) 최종 리스트에 붙이기
            holder.itemList.addView(container)
        }

        // ── 4) 합계 ──
        holder.tvTotal.text = "${item.totalAmount}원"

        // ── 5) 푸시 ──
        holder.tvPushInfo.text = item.pushSetting?.relativeMin?.let { min ->
            val h = min / 60; val m = min % 60
            buildString {
                append("푸시: ")
                if (h>0) append("${h}시간 ")
                if (m>0) append("${m}분 ")
                append("전")
            }
        } ?: "푸시: 없음"

        // ── 6) 수정/삭제 버튼 ──
        holder.btnEdit.setOnClickListener   { listener.onEdit(item) }
        holder.btnDelete.setOnClickListener { listener.onDelete(item) }
    }

    override fun getItemCount(): Int = data.size

    fun replace(newList: List<ReservationData>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}
