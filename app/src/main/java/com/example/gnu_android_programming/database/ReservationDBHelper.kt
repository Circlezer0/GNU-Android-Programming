package com.example.gnu_android_programming.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gnu_android_programming.reservation.PushSettingData
import com.example.gnu_android_programming.reservation.ReservationData
import com.example.gnu_android_programming.reservation.ReservationItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservationDBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "reservation.db"
        private const val DB_VERSION = 1

        // reservation 테이블
        const val TABLE_RESERVATION = "reservation"
        private const val COL_RES_ID = "id"
        private const val COL_CUSTOMER_NAME = "customer_name"
        private const val COL_CUSTOMER_CONTACT = "customer_contact"
        private const val COL_RES_DATETIME = "reservation_datetime"
        private const val COL_TRANS_DATETIME = "transaction_datetime"
        private const val COL_RES_TYPE = "reservation_type"
        private const val COL_TRANS_LOCATION = "transaction_location"
        private const val COL_TOTAL_AMOUNT = "total_amount"
        private const val COL_PUSH_REL_MIN = "push_relative_min"

        // reservation_item 테이블
        const val TABLE_ITEM = "reservation_item"
        private const val COL_ITEM_ID = "id"
        private const val COL_ITEM_RES_ID = "reservation_id"
        private const val COL_ITEM_NAME = "item_name"
        private const val COL_ITEM_PRICE = "price"
        private const val COL_ITEM_CATEGORY = "category"
        private const val COL_ITEM_MEMO = "memo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 예약 테이블 생성
        db.execSQL("""
            CREATE TABLE $TABLE_RESERVATION (
                $COL_RES_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CUSTOMER_NAME TEXT,
                $COL_CUSTOMER_CONTACT TEXT,
                $COL_RES_DATETIME TEXT,
                $COL_TRANS_DATETIME TEXT,
                $COL_RES_TYPE TEXT,
                $COL_TRANS_LOCATION TEXT,
                $COL_TOTAL_AMOUNT INTEGER,
                $COL_PUSH_REL_MIN INTEGER
            )
        """.trimIndent())

        // 예약 항목 테이블 생성
        db.execSQL("""
            CREATE TABLE $TABLE_ITEM (
                $COL_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ITEM_RES_ID INTEGER,
                $COL_ITEM_NAME TEXT,
                $COL_ITEM_PRICE INTEGER,
                $COL_ITEM_CATEGORY TEXT,
                $COL_ITEM_MEMO TEXT,
                FOREIGN KEY($COL_ITEM_RES_ID) REFERENCES $TABLE_RESERVATION($COL_RES_ID) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATION")
        onCreate(db)
    }

    /**
     * 새로운 예약과 그 항목들을 삽입합니다.
     * @return 생성된 reservation ID
     */
    fun insertReservation(reservation: ReservationData): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val resId = db.insert(TABLE_RESERVATION, null, ContentValues().apply {
                put(COL_CUSTOMER_NAME, reservation.customerName)
                put(COL_CUSTOMER_CONTACT, reservation.customerContact)
                put(COL_RES_DATETIME, reservation.reservationDateTime)
                put(COL_TRANS_DATETIME, reservation.transactionDateTime)
                put(COL_RES_TYPE, reservation.reservationType)
                put(COL_TRANS_LOCATION, reservation.transactionLocation)
                put(COL_TOTAL_AMOUNT, reservation.totalAmount)
                put(COL_PUSH_REL_MIN, reservation.pushSetting?.relativeMin)
            })
            reservation.id = resId            // ★ id 세팅

            reservation.items.forEach { item ->
                val itemId = db.insert(TABLE_ITEM, null, ContentValues().apply {
                    put(COL_ITEM_RES_ID, resId)
                    put(COL_ITEM_NAME, item.itemName)
                    put(COL_ITEM_PRICE, item.price)
                    put(COL_ITEM_CATEGORY, item.category)
                    put(COL_ITEM_MEMO, item.memo)
                })
                item.id = itemId              // ★ id 세팅
            }
            db.setTransactionSuccessful()
            return resId
        } finally { db.endTransaction() }
    }

    /* ---------- SELECT ---------- */
    fun getReservationsOfMonth(year: Int, month0Based: Int): List<ReservationData> {
        val calStart = Calendar.getInstance().apply { set(year, month0Based, 1, 0, 0, 0) }
        val calEnd   = Calendar.getInstance().apply {
            set(year, month0Based, 1, 23, 59, 59)
            add(Calendar.MONTH, 1); add(Calendar.SECOND, -1)
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return getReservationsBetween(fmt.format(calStart.time), fmt.format(calEnd.time))
    }

    fun getReservationsBetween(from: String, to: String): List<ReservationData> {
        val db = readableDatabase
        val list = mutableListOf<ReservationData>()
        val c = db.rawQuery(
            """
            SELECT * FROM $TABLE_RESERVATION
            WHERE $COL_RES_DATETIME BETWEEN ? AND ?
            ORDER BY $COL_RES_DATETIME DESC
            """, arrayOf(from, to)
        )
        while (c.moveToNext()) list += cursorToReservation(db, c)
        c.close()
        return list
    }

    private fun cursorToReservation(db: SQLiteDatabase, cur: Cursor): ReservationData {
        val resId = cur.getLong(cur.getColumnIndexOrThrow(COL_RES_ID))
        val items = mutableListOf<ReservationItemData>()
        val ic = db.query(
            TABLE_ITEM, null, "$COL_ITEM_RES_ID=?", arrayOf(resId.toString()),
            null, null, null
        )
        while (ic.moveToNext()) items.add(
            ReservationItemData(
                id        = ic.getLong(ic.getColumnIndexOrThrow(COL_ITEM_ID)),
                itemName  = ic.getString(ic.getColumnIndexOrThrow(COL_ITEM_NAME)),
                price     = ic.getInt   (ic.getColumnIndexOrThrow(COL_ITEM_PRICE)),
                category  = ic.getString(ic.getColumnIndexOrThrow(COL_ITEM_CATEGORY)),
                memo      = ic.getString(ic.getColumnIndexOrThrow(COL_ITEM_MEMO))
            )
        )
        ic.close()

        return ReservationData(
            id                = resId,
            customerName      = cur.getString(cur.getColumnIndexOrThrow(COL_CUSTOMER_NAME)),
            customerContact   = cur.getString(cur.getColumnIndexOrThrow(COL_CUSTOMER_CONTACT)),
            reservationDateTime = cur.getString(cur.getColumnIndexOrThrow(COL_RES_DATETIME)),
            transactionDateTime = cur.getString(cur.getColumnIndexOrThrow(COL_TRANS_DATETIME)),
            reservationType   = cur.getString(cur.getColumnIndexOrThrow(COL_RES_TYPE)),
            transactionLocation = cur.getString(cur.getColumnIndexOrThrow(COL_TRANS_LOCATION)),
            items             = items,
            totalAmount       = cur.getInt(cur.getColumnIndexOrThrow(COL_TOTAL_AMOUNT)),
            pushSetting       = cur.takeUnless { it.isNull(it.getColumnIndexOrThrow(COL_PUSH_REL_MIN)) }
                ?.getInt(cur.getColumnIndexOrThrow(COL_PUSH_REL_MIN))
                ?.let { PushSettingData(it) }
        )
    }

    fun updateReservation(reservation: ReservationData): Int {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // 1) reservation 테이블 업데이트
            val cv = ContentValues().apply {
                put(COL_CUSTOMER_NAME, reservation.customerName)
                put(COL_CUSTOMER_CONTACT, reservation.customerContact)
                put(COL_RES_DATETIME, reservation.reservationDateTime)
                put(COL_TRANS_DATETIME, reservation.transactionDateTime)
                put(COL_RES_TYPE, reservation.reservationType)
                put(COL_TRANS_LOCATION, reservation.transactionLocation)
                put(COL_TOTAL_AMOUNT, reservation.totalAmount)
                put(COL_PUSH_REL_MIN, reservation.pushSetting?.relativeMin)
            }
            val updated = db.update(
                TABLE_RESERVATION,
                cv,
                "$COL_RES_ID = ?",
                arrayOf(reservation.id.toString())
            )

            // 2) 기존 항목 삭제
            db.delete(TABLE_ITEM, "$COL_ITEM_RES_ID = ?", arrayOf(reservation.id.toString()))

            // 3) 새로운 항목 삽입
            reservation.items.forEach { item ->
                db.insert(TABLE_ITEM, null, ContentValues().apply {
                    put(COL_ITEM_RES_ID, reservation.id)
                    put(COL_ITEM_NAME, item.itemName)
                    put(COL_ITEM_PRICE, item.price)
                    put(COL_ITEM_CATEGORY, item.category)
                    put(COL_ITEM_MEMO, item.memo)
                })
            }

            db.setTransactionSuccessful()
            return updated
        } finally {
            db.endTransaction()
        }
    }

    fun deleteReservation(resId: Long): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_RESERVATION,
            "$COL_RES_ID = ?",
            arrayOf(resId.toString())
        )
    }

    fun getReservationById(resId: Long): ReservationData? {
        val db = readableDatabase
        val c = db.query(
            TABLE_RESERVATION, null,
            "$COL_RES_ID=?", arrayOf(resId.toString()),
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

}