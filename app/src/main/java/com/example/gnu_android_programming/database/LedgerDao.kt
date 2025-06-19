package com.example.gnu_android_programming.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gnu_android_programming.home.list.LedgerData

/**
 * Ledger 전용 DAO 클래스.
 * AppDatabaseHelper 에서 얻은 SQLiteDatabase 를 사용해,
 * 순수하게 CRUD 및 통계 기능만 제공합니다.
 */
class LedgerDao(context: Context) {

    // 통합 DB 헬퍼 인스턴스
    private val dbHelper = AppDatabaseHelper(context.applicationContext)

    // 자주 쓰이는 상수
    companion object {
        const val TABLE = "ledger"
        const val COL_ID   = "id"
        const val COL_DATE = "date"
        const val COL_TYPE = "income_expense"
        const val COL_CATEGORY = "category"
        const val COL_AMOUNT   = "amount"
        const val COL_PAYMENT  = "payment_method"
        const val COL_MEMO     = "memo"
    }

    /** 전체 조회 */
    fun getAll(): List<LedgerData> = getEntriesByMonth(null)

    /** 전체 삭제 */
    fun deleteAll() {
        val db = dbHelper.writableDatabase
        db.delete(TABLE, null, null)
    }

    /**
     * 주어진 "yyyy-MM" 패턴에 맞는 모든 LedgerData 객체를 반환
     */
    fun getEntriesByMonth(yearMonth: String?): List<LedgerData> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<LedgerData>()

        // yearMonth 가 null 혹은 빈 문자열이면 전체 조회
        val (selection, args) = if (!yearMonth.isNullOrEmpty()) {
            "${COL_DATE} LIKE ?" to arrayOf("$yearMonth%")
        } else {
            null to null
        }

        val cursor: Cursor = db.query(
            TABLE,           // "ledger"
            null,            // all columns
            selection,       // "date LIKE ?"
            args,            // arrayOf("2025-06%")
            null, null,
            "$COL_DATE DESC" // order by date DESC
        )

        while (cursor.moveToNext()) {
            list += LedgerData(
                id             = cursor.getLong  (cursor.getColumnIndexOrThrow(COL_ID)),
                date           = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                incomeExpense  = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                category       = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                amount         = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                paymentMethod  = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT)),
                memo           = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEMO))
            )
        }
        cursor.close()
        return list
    }

    /** 삽입 **/
    fun insert(entry: LedgerData): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(COL_DATE, entry.date)
            put(COL_TYPE, entry.incomeExpense)
            put(COL_CATEGORY, entry.category)
            put(COL_AMOUNT, entry.amount)
            put(COL_PAYMENT, entry.paymentMethod)
            put(COL_MEMO, entry.memo)
        }
        return db.insert(TABLE, null, cv)
    }

    /** 수정 **/
    fun update(entry: LedgerData): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(COL_DATE, entry.date)
            put(COL_TYPE, entry.incomeExpense)
            put(COL_CATEGORY, entry.category)
            put(COL_AMOUNT, entry.amount)
            put(COL_PAYMENT, entry.paymentMethod)
            put(COL_MEMO, entry.memo)
        }
        return db.update(TABLE, cv, "$COL_ID = ?", arrayOf(entry.id.toString()))
    }

    /** 삭제 **/
    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
    }

    /** 월간 통계 **/
    fun getMonthlySummary(yearMonth: String): Triple<Int,Int,Int> {
        val db = dbHelper.readableDatabase

        fun sumByType(type: String): Int {
            val c = db.rawQuery(
                "SELECT SUM($COL_AMOUNT) FROM $TABLE WHERE $COL_TYPE = ? AND $COL_DATE LIKE ?",
                arrayOf(type, "$yearMonth%")
            )
            val v = if (c.moveToFirst()) c.getInt(0) else 0
            c.close()
            return v
        }

        val income  = sumByType("수익")
        val expense = sumByType("지출")
        return Triple(income, expense, income - expense)
    }

    /** 일간 통계 **/
    fun getDailySummary(date: String): Pair<Int,Int> {
        val db = dbHelper.readableDatabase

        fun sumByType(type: String): Int {
            val c = db.rawQuery(
                "SELECT SUM($COL_AMOUNT) FROM $TABLE WHERE $COL_TYPE = ? AND $COL_DATE = ?",
                arrayOf(type, date)
            )
            val v = if (c.moveToFirst()) c.getInt(0) else 0
            c.close()
            return v
        }

        val income  = sumByType("수익")
        val expense = sumByType("지출")
        return Pair(income, expense)
    }
}
