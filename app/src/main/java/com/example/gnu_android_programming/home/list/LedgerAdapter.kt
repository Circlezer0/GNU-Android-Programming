package com.example.gnu_android_programming.home.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R
import java.text.NumberFormat
import java.util.*

/**
 * 가계부 항목 목록을 RecyclerView에 표시하기 위한 어댑터
 * @param items 표시할 LedgerData 리스트, 기본값은 빈 리스트
 */
class LedgerAdapter(
    private var items: List<LedgerData> = emptyList()
) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    /**
     * 항목 클릭 시 호출되는 콜백 함수
     * @property onItemClick 클릭된 LedgerData 전달
     */
    var onItemClick: ((LedgerData) -> Unit)? = null

    /**
     * 어댑터에 새로운 데이터 리스트를 설정하고 화면 갱신
     * @param newItems 갱신할 LedgerData 리스트
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<LedgerData>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * ViewHolder 생성 및 레이아웃 inflate
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ledger, parent, false)
        return LedgerViewHolder(view)
    }

    /**
     * ViewHolder에 데이터 바인딩 및 클릭 이벤트 설정
     */
    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val entry = items[position]
        holder.bind(entry)
        // 더미 항목일 경우 클릭 비활성화
        if (entry.incomeExpense != "-") {
            holder.itemView.setOnClickListener { onItemClick?.invoke(entry) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    /**
     * 전체 아이템 개수 반환
     */
    override fun getItemCount(): Int = items.size

    /**
     * 가계부 항목 뷰를 보유하는 ViewHolder
     */
    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 거래 일(day) 표시 TextView
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        // 수익/지출 타입 표시 TextView
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        // 카테고리 표시 TextView
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        // 금액 표시 TextView
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        // 결제 수단 표시 TextView
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        // 메모 표시 TextView
        private val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)

        /**
         * LedgerData 항목을 각 뷰에 바인딩
         * @param entry 바인딩할 LedgerData 객체
         */
        fun bind(entry: LedgerData) {
            // 날짜: 더미 항목이면 저장된 문자열 그대로, 아니면 "yyyy-MM-dd"에서 일(day)만 추출
            if (entry.incomeExpense == "-") {
                tvDate.text = entry.date.takeLast(2) + "일"
            } else {
                val parts = entry.date.split("-")
                tvDate.text = if (parts.size >= 3) "${parts[2]}일" else entry.date
            }

            // 수익/지출 타입 표시 및 색상 적용 (수익=빨강, 지출=파랑)
            tvType.text = entry.incomeExpense
            when (entry.incomeExpense) {
                "수익" -> tvType.setTextColor(Color.RED)
                "지출" -> tvType.setTextColor(Color.BLUE)
                else -> tvType.setTextColor(Color.BLACK)
            }

            tvCategory.text = entry.category

            // 금액: 3자리마다 쉼표 포맷 적용
            val formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault())
                .format(entry.amount)
            tvAmount.text = formattedAmount

            tvPaymentMethod.text = entry.paymentMethod
            tvMemo.text = entry.memo
        }
    }
}
