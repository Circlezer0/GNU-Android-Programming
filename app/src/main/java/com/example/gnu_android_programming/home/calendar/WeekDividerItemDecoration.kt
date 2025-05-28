package com.example.gnu_android_programming.home.calendar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class WeekDividerItemDecoration(
    private val color: Int = 0xFFCCCCCC.toInt(),
    private val dividerHeight: Int = 2 // px 단위
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        this.color = this@WeekDividerItemDecoration.color
        style = Paint.Style.FILL
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val spanCount = 7 // 7일씩 한 줄

        for (i in 0 until childCount) {
            val view: View = parent.getChildAt(i)
            val adapterPosition = parent.getChildAdapterPosition(view)

            // "한 줄의 마지막 칸"이면(= 6, 13, 20, ...) (0-indexed)
            if ((adapterPosition + 1) % spanCount == 0) {
                val left = parent.paddingLeft
                val right = parent.width - parent.paddingRight
                val top = view.bottom
                val bottom = top + dividerHeight

                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        val spanCount = 7
        // 마지막 줄이 아닐 때만 아래쪽 여백을 줌
        val total = parent.adapter?.itemCount ?: 0
        val isLastRow = adapterPosition >= total - spanCount
        if (!isLastRow && (adapterPosition + 1) % spanCount == 0) {
            outRect.bottom = dividerHeight
        }
    }
}
