package com.example.gnu_android_programming.home.calendar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 요일(Week) 단위로 RecyclerView 항목 사이에 구분선을 그려주는 ItemDecoration 클래스
 * @param color 구분선 색상 (기본: 연회색)
 * @param dividerHeight 구분선 높이(px 단위, 기본: 2px)
 */
class WeekDividerItemDecoration(
    private val color: Int = 0xFFCCCCCC.toInt(), // 구분선 색상
    private val dividerHeight: Int = 2 // 구분선 높이(px)
) : RecyclerView.ItemDecoration() {

    // 구분선 그리기용 Paint 객체
    private val paint = Paint().apply {
        this.color = this@WeekDividerItemDecoration.color
        style = Paint.Style.FILL
    }

    /**
     * RecyclerView 위에 구분선을 직접 그리는 콜백
     * @param c Canvas 객체
     * @param parent RecyclerView
     * @param state RecyclerView 상태 정보
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount // 화면에 보이는 항목 수
        val spanCount = 7 // 한 줄에 7일 표시

        // 각 자식 뷰를 순회하며 마지막 칸인지 확인 후 구분선 그리기
        for (i in 0 until childCount) {
            val view: View = parent.getChildAt(i)
            val adapterPosition = parent.getChildAdapterPosition(view)

            // "한 줄의 마지막 칸"이면 (인덱스 6, 13, 20...)
            if ((adapterPosition + 1) % spanCount == 0) {
                val left = parent.paddingLeft // RecyclerView 왼쪽 패딩 위치
                val right = parent.width - parent.paddingRight // 오른쪽 패딩 위치
                val top = view.bottom // 뷰 바로 아래
                val bottom = top + dividerHeight // 구분선 아래 경계

                // 사각 영역으로 구분선 그리기
                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
    }

    /**
     * 각 아이템의 아웃셋(빈 공간)을 설정하여 구분선 공간 확보
     * @param outRect 계산된 아웃셋(Rect)
     * @param view 해당 아이템 뷰
     * @param parent RecyclerView
     * @param state RecyclerView 상태
     */
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val adapterPosition = parent.getChildAdapterPosition(view) // 항목 위치
        val spanCount = 7 // 주 단위 항목 수
        val total = parent.adapter?.itemCount ?: 0 // 전체 아이템 수
        // 마지막 줄인지 확인 (예: 총 항목 수 - spanCount 이후)
        val isLastRow = adapterPosition >= total - spanCount

        // 마지막 줄이 아니고, 한 줄의 마지막 칸인 경우에만 아래쪽 여백 설정
        if (!isLastRow && (adapterPosition + 1) % spanCount == 0) {
            outRect.bottom = dividerHeight // 아래쪽 여백(px)
        }
    }
}
