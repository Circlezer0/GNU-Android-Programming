package com.example.gnu_android_programming.home.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R

class CalendarAdapter(private val dayDataList: List<DayData>) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvRevenue: TextView = itemView.findViewById(R.id.tvRevenue)
        val tvExpense: TextView = itemView.findViewById(R.id.tvExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_calendar_day_item, parent, false)
        return DayViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayData = dayDataList[position]
        if (dayData.day == 0) {
            // 빈 셀: 아무 텍스트도 표시하지 않음
            holder.tvDay.text = ""
            holder.tvRevenue.text = ""
            holder.tvExpense.text = ""
        } else {
            holder.tvDay.text = dayData.day.toString()
            holder.tvRevenue.text = "+${dayData.revenue}"
            holder.tvExpense.text = "-${dayData.expense}"
        }
    }

    override fun getItemCount(): Int = dayDataList.size
}
