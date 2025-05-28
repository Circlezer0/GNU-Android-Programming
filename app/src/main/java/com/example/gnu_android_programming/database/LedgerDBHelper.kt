package com.example.gnu_android_programming.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gnu_android_programming.home.list.LedgerEntry
import org.json.JSONArray
import org.json.JSONObject

class LedgerDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "ledger.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "ledger"
        const val COL_ID = "id"
        const val COL_DATE = "date"
        const val COL_TYPE = "income_expense"
        const val COL_CATEGORY = "category"
        const val COL_AMOUNT = "amount"
        const val COL_PAYMENT_METHOD = "payment_method"
        const val COL_MEMO = "memo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE TEXT,
                $COL_TYPE TEXT,
                $COL_CATEGORY TEXT,
                $COL_AMOUNT INTEGER,
                $COL_PAYMENT_METHOD TEXT,
                $COL_MEMO TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 데이터 삽입 함수
    fun insertEntry(entry: LedgerEntry): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DATE, entry.date)
            put(COL_TYPE, entry.incomeExpense)
            put(COL_CATEGORY, entry.category)
            put(COL_AMOUNT, entry.amount)
            put(COL_PAYMENT_METHOD, entry.paymentMethod)
            put(COL_MEMO, entry.memo)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 월간 통계: 해당 년월(예:"2025-04")에 대한 수익, 지출, 순수익
    fun getMonthlySummary(yearMonth: String): Triple<Int, Int, Int> {
        val db = readableDatabase
        // 수익 합계
        val cursorIncome = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = ? AND $COL_DATE LIKE ?",
            arrayOf("수익", "$yearMonth%")
        )
        var income = 0
        if (cursorIncome.moveToFirst()) {
            income = cursorIncome.getInt(0)
        }
        cursorIncome.close()

        // 지출 합계
        val cursorExpense = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = ? AND $COL_DATE LIKE ?",
            arrayOf("지출", "$yearMonth%")
        )
        var expense = 0
        if (cursorExpense.moveToFirst()) {
            expense = cursorExpense.getInt(0)
        }
        cursorExpense.close()

        return Triple(income, expense, income - expense)
    }

    // 특정 날짜("yyyy-MM-dd")의 수익과 지출 합계 반환
    fun getDailySummary(date: String): Pair<Int, Int> {
        val db = readableDatabase
        var revenue = 0
        var expense = 0

        val cursorRevenue = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = ? AND $COL_DATE = ?",
            arrayOf("수익", date)
        )
        if (cursorRevenue.moveToFirst()) {
            revenue = cursorRevenue.getInt(0)
        }
        cursorRevenue.close()

        val cursorExpense = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = ? AND $COL_DATE = ?",
            arrayOf("지출", date)
        )
        if (cursorExpense.moveToFirst()) {
            expense = cursorExpense.getInt(0)
        }
        cursorExpense.close()

        return Pair(revenue, expense)
    }

    fun updateEntry(entry: LedgerEntry): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DATE, entry.date)
            put(COL_TYPE, entry.incomeExpense)
            put(COL_CATEGORY, entry.category)
            put(COL_AMOUNT, entry.amount)
            put(COL_PAYMENT_METHOD, entry.paymentMethod)
            put(COL_MEMO, entry.memo)
        }
        // update() 함수는 업데이트된 행의 수를 반환합니다.
        return db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(entry.id.toString()))
    }

    fun deleteEntry(id: Long): Int {
        val db = writableDatabase
        // delete() 함수는 삭제된 행의 수를 반환합니다.
        return db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
    }

    // LedgerDBHelper 클래스 내부
    fun exportToJson(): String {
        val jsonArray = JSONArray()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val obj = JSONObject()
            obj.put("id", cursor.getLong(cursor.getColumnIndexOrThrow("id")))
            obj.put("rec_date", cursor.getString(cursor.getColumnIndexOrThrow("rec_date")))
            obj.put("type", cursor.getString(cursor.getColumnIndexOrThrow("type")))
            obj.put("cost", cursor.getInt(cursor.getColumnIndexOrThrow("cost")))
            // memo가 null인 경우 빈 문자열로 처리
            obj.put("memo", if (cursor.isNull(cursor.getColumnIndexOrThrow("memo"))) "" else cursor.getString(cursor.getColumnIndexOrThrow("memo")))
            obj.put("pay_type", cursor.getString(cursor.getColumnIndexOrThrow("pay_type")))
            jsonArray.put(obj)
        }
        cursor.close()
        return jsonArray.toString()
    }

    fun importFromJson(jsonString: String) {
        // JSON 문자열을 파싱하여 데이터베이스에 삽입
        val db = writableDatabase
        // 기존 테이블을 비우고 새로 삽입
        db.execSQL("DELETE FROM $TABLE_NAME")

        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val values = android.content.ContentValues().apply {
                put("rec_date", obj.getString("rec_date"))
                put("type", obj.getString("type"))
                put("cost", obj.getInt("cost"))
                put("memo", obj.optString("memo", ""))
                put("pay_type", obj.getString("pay_type"))
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

}