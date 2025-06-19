package com.example.gnu_android_programming.home.list

import java.io.Serializable

data class LedgerData(
    val id: Long = 0,
    val date: String,           // 형식: "yyyy-MM-dd"
    val incomeExpense: String,  // "수익" 또는 "지출"
    val category: String,       // "식물", "화분", "부자재", "운영비"
    val amount: Int,            // 거래 금액 (정수, 0 초과)
    val paymentMethod: String,  // "카드" 또는 "현금"
    val memo: String            // 거래 메모 (선택 사항, 빈 문자열 허용)
): Serializable