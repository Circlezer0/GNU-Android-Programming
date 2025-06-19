package com.example.gnu_android_programming.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gnu_android_programming.R

class ReservationItemAdapter(
    private val items: List<ReservationItemData>
): RecyclerView.Adapter<ReservationItemAdapter.VH>() {

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        val name = v.findViewById<TextView>(R.id.tvItemName)
        val category = v.findViewById<TextView>(R.id.tvItemCategory)
        val price = v.findViewById<TextView>(R.id.tvItemPrice)
        val memo = v.findViewById<TextView>(R.id.tvItemMemo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_detail, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        holder.name.text = it.itemName
        holder.category.text = it.category
        holder.price.text = "${it.price}원"

        if (it.memo.isNotBlank()) {
            holder.memo.visibility = View.VISIBLE
            holder.memo.text = "메모: ${it.memo}"
        } else {
            holder.memo.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}