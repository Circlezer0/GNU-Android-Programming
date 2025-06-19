package com.example.gnu_android_programming.database

import com.example.gnu_android_programming.home.list.LedgerData
import com.example.gnu_android_programming.reservation.ReservationData

data class AppBackup(
    val ledgers: List<LedgerData>,
    val reservations: List<ReservationData>
)