package com.example.gnu_android_programming.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME    = "appdata.db"
        // 기존 버전이 1이었다면, 통합 후에는 onUpgrade가 실행되도록 2로 올립니다.
        const val DB_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1) ledger 테이블
        db.execSQL("""
            CREATE TABLE ledger (
              id             INTEGER PRIMARY KEY AUTOINCREMENT,
              date           TEXT,
              income_expense TEXT,
              category       TEXT,
              amount         INTEGER,
              payment_method TEXT,
              memo           TEXT
            )
        """.trimIndent())

        // 2) reservation 테이블
        db.execSQL("""
            CREATE TABLE reservation (
              id                    INTEGER PRIMARY KEY AUTOINCREMENT,
              customer_name         TEXT,
              customer_contact      TEXT,
              reservation_datetime  TEXT,
              transaction_datetime  TEXT,
              reservation_type      TEXT,
              transaction_location  TEXT,
              total_amount          INTEGER,
              push_relative_min     INTEGER
            )
        """.trimIndent())

        // 3) reservation_item 테이블 (FK: reservation.id)
        db.execSQL("""
            CREATE TABLE reservation_item (
              id             INTEGER PRIMARY KEY AUTOINCREMENT,
              reservation_id INTEGER,
              item_name      TEXT,
              price          INTEGER,
              category       TEXT,
              memo           TEXT,
              FOREIGN KEY(reservation_id)
                REFERENCES reservation(id)
                ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 버전이 올라가면 기존 테이블을 삭제하고 재생성
        db.execSQL("DROP TABLE IF EXISTS reservation_item")
        db.execSQL("DROP TABLE IF EXISTS reservation")
        db.execSQL("DROP TABLE IF EXISTS ledger")
        onCreate(db)
    }
}
