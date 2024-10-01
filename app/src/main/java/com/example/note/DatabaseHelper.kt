package com.example.note

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_NAME = "AccountBook.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "account_records"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_PURPOSE = "purpose"
        private const val COLUMN_TAG = "tag"
    }

    override fun onCreate(p0: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME(
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_DATE TEXT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_PURPOSE TEXT,
                $COLUMN_TAG TEXT
            )
        """.trimIndent()
        p0.execSQL(createTableQuery)
    }

    override fun onOpen(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}