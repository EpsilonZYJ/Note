package com.example.note

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_NAME = "AccountBook.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "account_records"
    }
}