package com.example.gnu_android_programming.home.list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R
import java.text.NumberFormat
import java.util.*

class LedgerAdapter(private var items: List<LedgerData> = emptyList()) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    // 아이템 클릭 콜백
    var onItemClick: ((LedgerData) -> Unit)? = null

    fun updateData(newItems: List<LedgerData>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val entry = items[position]
        holder.bind(entry)
        // 만약 dummy 항목이면 클릭 이벤트를 막음
        if (entry.incomeExpense != "-") {
            holder.itemView.setOnClickListener { onItemClick?.invoke(entry) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        private val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)

        fun bind(entry: LedgerData) {
            // 날짜: 만약 dummy 항목이면 그대로, 아니면 "yyyy-MM-dd"에서 일(day)만 추출해 "일" 붙이기
            if (entry.incomeExpense == "-") {
                tvDate.text = entry.date.takeLast(2) + "일"
            } else {
                val parts = entry.date.split("-")
                tvDate.text = if (parts.size >= 3) "${parts[2]}일" else entry.date
            }

            // 수익/지출: 이 열만 색상 적용 (수익=빨간색, 지출=파란색)
            tvType.text = entry.incomeExpense
            if (entry.incomeExpense == "수익") {
                tvType.setTextColor(Color.RED)
            } else if (entry.incomeExpense == "지출") {
                tvType.setTextColor(Color.BLUE)
            } else {
                tvType.setTextColor(Color.BLACK)
            }

            tvCategory.text = entry.category

            // 금액: 3자리마다 쉼표 포맷
            val formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault()).format(entry.amount)
            tvAmount.text = formattedAmount

            tvPaymentMethod.text = entry.paymentMethod
            tvMemo.text = entry.memo
        }
    }
}
