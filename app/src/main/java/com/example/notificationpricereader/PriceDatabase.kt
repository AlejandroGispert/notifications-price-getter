// PriceDataBase.kt
package com.example.notificationpricereader

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PriceEntry(
    val id: Long,
    val price: Double,
    val source: String,
    val timestamp: Long,
    val originalText: String
)

class PriceDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                price REAL NOT NULL,
                source TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                original_text TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addPrice(price: Double, source: String, text: String) {
        val values = ContentValues().apply {
            put("price", price)
            put("source", source)
            put("timestamp", System.currentTimeMillis())
            put("original_text", text)
        }
        writableDatabase.use { db ->
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun getAllPrices(): List<PriceEntry> {
        val prices = mutableListOf<PriceEntry>()
        readableDatabase.use { db ->
            db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "timestamp DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    prices.add(
                        PriceEntry(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                            source = cursor.getString(cursor.getColumnIndexOrThrow("source")),
                            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                            originalText = cursor.getString(cursor.getColumnIndexOrThrow("original_text"))
                        )
                    )
                }
            }
        }
        return prices
    }

    companion object {
        private const val DATABASE_NAME = "prices.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "prices"
    }
}