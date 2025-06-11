package com.example.gnu_android_programming.reservation

import java.io.Serializable

data class ReservationItemData(
    var id: Long? = null,
    val itemName: String,
    val price: Int,
    val category: String,
    val memo: String
) : Serializable