package com.example.gnu_android_programming.home.calendar

data class DayData(
    val day: Int,         // 0이면 빈 셀
    val revenue: Int = 0, // 해당 날짜의 수익
    val expense: Int = 0  // 해당 날짜의 지출
)
