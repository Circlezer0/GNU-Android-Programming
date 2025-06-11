package com.example.gnu_android_programming.reservation

import java.io.Serializable

data class ReservationData(
    var id: Long? = null,
    var customerName: String,
    var customerContact: String,
    var reservationDateTime: String,
    var transactionDateTime: String,
    var reservationType: String,     // "픽업" 또는 "배달"
    var transactionLocation: String,
    var items: List<ReservationItemData>,
    var totalAmount: Int,
    var pushSetting: PushSettingData?    // null이면 알림 없음
) : Serializable

