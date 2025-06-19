package com.example.gnu_android_programming.home.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R

/**
 * 월별 달력 화면에 일별 수익/지출 데이터를 표시하는 RecyclerView Adapter
 * @param dayDataList 달력에 표시할 DayData 리스트
 */
class CalendarAdapter(
    private val dayDataList: List<DayData>
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    /**
     * 각 날짜 아이템 뷰를 보관하는 ViewHolder
     * @property tvDay 날짜(일) 표시 TextView
     * @property tvRevenue 수익 금액 표시 TextView
     * @property tvExpense 지출 금액 표시 TextView
     */
    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)         // 날짜 텍스트뷰
        val tvRevenue: TextView = itemView.findViewById(R.id.tvRevenue) // 수익 텍스트뷰
        val tvExpense: TextView = itemView.findViewById(R.id.tvExpense) // 지출 텍스트뷰
    }

    /**
     * ViewHolder 생성 및 레이아웃 inflate
     * @param parent 부모 ViewGroup
     * @param viewType 아이템 뷰 타입
     * @return 생성된 DayViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_calendar_day_item, parent, false) // custom_calendar_day_item 레이아웃
        return DayViewHolder(view)
    }

    /**
     * ViewHolder에 데이터 바인딩
     * @param holder 바인딩할 DayViewHolder
     * @param position 현재 아이템 위치
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayData = dayDataList[position] // 해당 위치의 DayData
        if (dayData.day == 0) {
            // day가 0인 경우: 빈 칸 처리
            holder.tvDay.text = ""
            holder.tvRevenue.text = ""
            holder.tvExpense.text = ""
        } else {
            // 실제 날짜인 경우: 일자, 수익(+), 지출(-) 표시
            holder.tvDay.text = dayData.day.toString()
            holder.tvRevenue.text = "+${dayData.revenue}"   // 수익 앞에 + 기호
            holder.tvExpense.text = "-${dayData.expense}"  // 지출 앞에 - 기호
        }
    }

    /**
     * 전체 아이템 개수 반환
     * @return dayDataList 크기
     */
    override fun getItemCount(): Int = dayDataList.size
}
