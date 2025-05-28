package com.example.gnu_android_programming.home.list

data class LedgerEntry(
    val id: Long = 0,
    val date: String,         // 형식: "yyyy-MM-dd"
    val incomeExpense: String, // "수익" 또는 "지출"
    val category: String,      // "식물", "화분", "부자재", "운영비"
    val amount: Int,
    val paymentMethod: String, // "카드" 또는 "현금"
    val memo: String
)