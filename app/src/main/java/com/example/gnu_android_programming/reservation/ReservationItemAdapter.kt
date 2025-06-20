package com.example.gnu_android_programming.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R

/**
 * 예약 상세 항목(ReservationItemData)을 리스트 형태로 표시하는 RecyclerView Adapter
 * @param items 표시할 예약 항목 데이터 리스트
 */
class ReservationItemAdapter(
    private val items: List<ReservationItemData>  // 예약 항목 데이터 리스트
) : RecyclerView.Adapter<ReservationItemAdapter.VH>() {

    /**
     * 각 예약 항목 뷰의 UI 컴포넌트를 보관하는 ViewHolder
     * @property name 항목명(TextView)
     * @property category 카테고리(TextView)
     * @property price 가격(TextView)
     * @property memo 메모(TextView)
     */
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvItemName)           // 아이템명 표시
        val category: TextView = v.findViewById(R.id.tvItemCategory)   // 카테고리 표시
        val price: TextView = v.findViewById(R.id.tvItemPrice)         // 가격 표시
        val memo: TextView = v.findViewById(R.id.tvItemMemo)           // 메모 표시
    }

    /**
     * ViewHolder 생성 및 레이아웃 inflate
     * @param parent 부모 ViewGroup
     * @param viewType 뷰 타입 (사용하지 않음)
     * @return 새로 생성된 VH
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reservation_detail, parent, false) // 상세 아이템 레이아웃
        )

    /**
     * ViewHolder에 데이터 바인딩
     * @param holder 바인딩 대상 ViewHolder
     * @param position 리스트 내 아이템 위치
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]  // 해당 위치의 데이터
        holder.name.text = item.itemName        // 아이템명 설정
        holder.category.text = item.category    // 카테고리 설정
        holder.price.text = "${item.price}원"  // 가격에 원 단위 추가

        if (item.memo.isNotBlank()) {
            // 메모가 있는 경우 표시
            holder.memo.visibility = View.VISIBLE
            holder.memo.text = "메모: ${item.memo}"
        } else {
            // 메모 없으면 숨김
            holder.memo.visibility = View.GONE
        }
    }

    /**
     * 전체 아이템 개수 반환
     */
    override fun getItemCount(): Int = items.size
}
