package com.example.gnu_android_programming.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.gnu_android_programming.reservation.PushSettingData
import com.example.gnu_android_programming.reservation.ReservationData
import com.example.gnu_android_programming.reservation.ReservationItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Reservation 전용 DAO
 * AppDatabaseHelper 로부터 읽기/쓰기 가능한 SQLiteDatabase 를 받아,
 * CRUD와 조회 기능만 제공합니다.
 */
class ReservationDao(context: Context) {

    private val dbHelper = AppDatabaseHelper(context.applicationContext)

    companion object {
        const val TABLE_RES      = "reservation"
        const val COL_RES_ID     = "id"
        const val COL_C_NAME     = "customer_name"
        const val COL_C_CONTACT  = "customer_contact"
        const val COL_RES_DT     = "reservation_datetime"
        const val COL_TRANS_DT   = "transaction_datetime"
        const val COL_RES_TYPE   = "reservation_type"
        const val COL_TRANS_LOC  = "transaction_location"
        const val COL_TOTAL      = "total_amount"
        const val COL_PUSH_REL   = "push_relative_min"

        const val TABLE_ITEM     = "reservation_item"
        const val COL_ITEM_ID    = "id"
        const val COL_ITEM_RESID = "reservation_id"
        const val COL_ITEM_NAME  = "item_name"
        const val COL_PRICE      = "price"
        const val COL_CATEGORY   = "category"
        const val COL_MEMO       = "memo"
    }

    /** 전체 조회 */
    fun getAll(): List<ReservationData> = getBetween(
        "0000-01-01 00:00",
        "9999-12-31 23:59"
    )

    /** 전체 삭제 (item 포함) */
    fun deleteAll() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_ITEM, null, null)
            db.delete(TABLE_RES,  null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** 삽입 **/
    fun insert(res: ReservationData): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val resId = db.insert(TABLE_RES, null, ContentValues().apply {
                put(COL_C_NAME,    res.customerName)
                put(COL_C_CONTACT, res.customerContact)
                put(COL_RES_DT,    res.reservationDateTime)
                put(COL_TRANS_DT,  res.transactionDateTime)
                put(COL_RES_TYPE,  res.reservationType)
                put(COL_TRANS_LOC, res.transactionLocation)
                put(COL_TOTAL,     res.totalAmount)
                put(COL_PUSH_REL,  res.pushSetting?.relativeMin)
            })
            res.id = resId

            res.items.forEach { item ->
                val itemId = db.insert(TABLE_ITEM, null, ContentValues().apply {
                    put(COL_ITEM_RESID, resId)
                    put(COL_ITEM_NAME,  item.itemName)
                    put(COL_PRICE,      item.price)
                    put(COL_CATEGORY,   item.category)
                    put(COL_MEMO,       item.memo)
                })
                item.id = itemId
            }

            db.setTransactionSuccessful()
            return resId
        } finally {
            db.endTransaction()
        }
    }

    /** 삭제 **/
    fun delete(resId: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(TABLE_RES, "$COL_RES_ID = ?", arrayOf(resId.toString()))
    }

    /** 수정 **/
    fun update(res: ReservationData): Int {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(COL_C_NAME,    res.customerName)
                put(COL_C_CONTACT, res.customerContact)
                put(COL_RES_DT,    res.reservationDateTime)
                put(COL_TRANS_DT,  res.transactionDateTime)
                put(COL_RES_TYPE,  res.reservationType)
                put(COL_TRANS_LOC, res.transactionLocation)
                put(COL_TOTAL,     res.totalAmount)
                put(COL_PUSH_REL,  res.pushSetting?.relativeMin)
            }
            val count = db.update(TABLE_RES, cv, "$COL_RES_ID = ?", arrayOf(res.id.toString()))

            // 기존 아이템 전부 삭제 후 다시 삽입
            db.delete(TABLE_ITEM, "$COL_ITEM_RESID = ?", arrayOf(res.id.toString()))
            res.items.forEach { item ->
                db.insert(TABLE_ITEM, null, ContentValues().apply {
                    put(COL_ITEM_RESID, res.id)
                    put(COL_ITEM_NAME,  item.itemName)
                    put(COL_PRICE,      item.price)
                    put(COL_CATEGORY,   item.category)
                    put(COL_MEMO,       item.memo)
                })
            }

            db.setTransactionSuccessful()
            return count
        } finally {
            db.endTransaction()
        }
    }

    /** ID로 조회 **/
    fun getById(resId: Long): ReservationData? {
        val db = dbHelper.readableDatabase
        val c = db.query(
            TABLE_RES, null,
            "$COL_RES_ID = ?", arrayOf(resId.toString()),
            null, null, null
        )
        return if (c.moveToFirst()) {
            val res = cursorToReservation(db, c)
            c.close()
            res
        } else {
            c.close()
            null
        }
    }

    /** 기간 조회 **/
    fun getBetween(from: String, to: String): List<ReservationData> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<ReservationData>()
        val cursor = db.rawQuery(
            """
            SELECT * FROM $TABLE_RES
            WHERE $COL_RES_DT BETWEEN ? AND ?
            ORDER BY $COL_RES_DT DESC
            """.trimIndent(),
            arrayOf(from, to)
        )
        while (cursor.moveToNext()) {
            list += cursorToReservation(db, cursor)
        }
        cursor.close()
        return list
    }

    /** 월별 조회 **/
    fun getOfMonth(year: Int, month0: Int): List<ReservationData> {
        val start = Calendar.getInstance().apply {
            set(year, month0, 1, 0, 0, 0)
        }
        val end = Calendar.getInstance().apply {
            set(year, month0, 1, 23, 59, 59)
            add(Calendar.MONTH, 1)
            add(Calendar.SECOND, -1)
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return getBetween(fmt.format(start.time), fmt.format(end.time))
    }

    /** Cursor → ReservationData 변환 **/
    private fun cursorToReservation(db: SQLiteDatabase, cur: Cursor): ReservationData {
        val resId = cur.getLong(cur.getColumnIndexOrThrow(COL_RES_ID))
        val items = mutableListOf<ReservationItemData>()

        val ic = db.query(
            TABLE_ITEM, null,
            "$COL_ITEM_RESID = ?", arrayOf(resId.toString()),
            null, null, null
        )
        while (ic.moveToNext()) {
            items += ReservationItemData(
                id       = ic.getLong(ic.getColumnIndexOrThrow(COL_ITEM_ID)),
                itemName = ic.getString(ic.getColumnIndexOrThrow(COL_ITEM_NAME)),
                price    = ic.getInt(ic.getColumnIndexOrThrow(COL_PRICE)),
                category = ic.getString(ic.getColumnIndexOrThrow(COL_CATEGORY)),
                memo     = ic.getString(ic.getColumnIndexOrThrow(COL_MEMO))
            )
        }
        ic.close()

        val pushIdx = cur.getColumnIndexOrThrow(COL_PUSH_REL)
        val push    = if (!cur.isNull(pushIdx))
            PushSettingData(cur.getInt(pushIdx))
        else null

        return ReservationData(
            id                   = resId,
            customerName         = cur.getString(cur.getColumnIndexOrThrow(COL_C_NAME)),
            customerContact      = cur.getString(cur.getColumnIndexOrThrow(COL_C_CONTACT)),
            reservationDateTime  = cur.getString(cur.getColumnIndexOrThrow(COL_RES_DT)),
            transactionDateTime  = cur.getString(cur.getColumnIndexOrThrow(COL_TRANS_DT)),
            reservationType      = cur.getString(cur.getColumnIndexOrThrow(COL_RES_TYPE)),
            transactionLocation  = cur.getString(cur.getColumnIndexOrThrow(COL_TRANS_LOC)),
            items                = items,
            totalAmount          = cur.getInt(cur.getColumnIndexOrThrow(COL_TOTAL)),
            pushSetting          = push
        )
    }
}
