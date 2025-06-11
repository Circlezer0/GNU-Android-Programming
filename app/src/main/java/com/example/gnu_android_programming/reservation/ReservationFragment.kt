// ReservationFragment.kt
package com.example.gnu_android_programming.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.gnu_android_programming.R
import com.example.gnu_android_programming.cancelReservationAlarm
import com.example.gnu_android_programming.database.ReservationDBHelper
import java.util.*

class ReservationFragment : Fragment() {

    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var tvMonthYear: TextView
    private lateinit var fabAddReservation: FloatingActionButton
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private lateinit var dbHelper: ReservationDBHelper

    private val current = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_reservation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = ReservationDBHelper(requireContext())

        btnPrevMonth      = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth      = view.findViewById(R.id.btnNextMonth)
        tvMonthYear       = view.findViewById(R.id.tvMonthYear)
        fabAddReservation = view.findViewById(R.id.fabAddReservation)
        rv                = view.findViewById(R.id.rvReservations)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservationAdapter(mutableListOf(),
            object: ReservationAdapter.OnReservationActionListener {
                override fun onEdit(res: ReservationData) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ReservationEditFragment.newInstance(res))
                        .addToBackStack(null)
                        .commit()
                }
                override fun onDelete(res: ReservationData) {
                    cancelReservationAlarm(requireContext(), res.id!!)
                    dbHelper.deleteReservation(res.id ?: return)
                    refreshList()
                }
            }
        )
        rv.adapter = adapter

        btnPrevMonth.setOnClickListener { moveMonth(-1) }
        btnNextMonth.setOnClickListener { moveMonth(+1) }
        fabAddReservation.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReservationAddFragment())
                .addToBackStack(null)
                .commit()
        }

        refreshList()
    }

    private fun moveMonth(delta: Int) {
        current.add(Calendar.MONTH, delta)
        refreshList()
    }

    private fun refreshList() {
        val year  = current.get(Calendar.YEAR)
        val month = current.get(Calendar.MONTH)+1
        tvMonthYear.text = "${year}년 ${"%02d".format(month)}월"
        adapter.replace(dbHelper.getReservationsOfMonth(year, current.get(Calendar.MONTH)))
    }
}
