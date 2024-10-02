package com.example.note

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers

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

    override fun onUpgrade(p0: SQLiteDatabase, oldVesion: Int, newVersion: Int) {
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(p0)
    }

    fun insertRecord(record: AccountRecord){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, record.id)
            put(COLUMN_DATE, record.date)
            put(COLUMN_AMOUNT, record.amount)
            put(COLUMN_PURPOSE, record.purpose)
            put(COLUMN_TAG, record.tag)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    private fun queryRecords(): List<AccountRecord>{
        val records = mutableListOf<AccountRecord>()
        val db = this.writableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        cursor.use{
            while(it.moveToNext()){
                val id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID))
                val date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val purpose = it.getString(it.getColumnIndexOrThrow(COLUMN_PURPOSE))
                val tag = it.getString(it.getColumnIndexOrThrow(COLUMN_TAG))
                records.add(AccountRecord(id, date, amount, purpose, tag))
            }
        }
        db.close()
        return records
    }

    fun getAllRecords(): Flow<List<AccountRecord>> = flow<List<AccountRecord>> {
        while(true){
            val records = queryRecords()
            emit(records)
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    fun deleteRecord(record: AccountRecord){
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(record.id))
        db.close()
    }

    fun updateRecord(record: AccountRecord){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, record.date)
            put(COLUMN_AMOUNT, record.amount)
            put(COLUMN_PURPOSE, record.purpose)
            put(COLUMN_TAG, record.tag)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(record.id))
        db.close()
    }

    fun getRecordsByDate(date: String): Flow<List<AccountRecord>> = flow{
        val records = mutableListOf<AccountRecord>()
        val db = this@DatabaseHelper.readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COLUMN_DATE = ?", arrayOf(date), null, null, null)

        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndexOrThrow(COLUMN_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val purpose = getString(getColumnIndexOrThrow(COLUMN_PURPOSE))
                val tag = getString(getColumnIndexOrThrow(COLUMN_TAG))
                records.add(AccountRecord(id, date, amount, purpose, tag))
            }
        }
        cursor.close()
        db.close()
        emit(records)
    }

    fun getTagCounts(): Flow<Map<String, Int>> = flow{
        val tagCounts = mutableMapOf<String, Int>()
        val db = this@DatabaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TAG, COUNT(*) as count FROM $TABLE_NAME GROUP BY $COLUMN_TAG", null)

        with(cursor){
            while(moveToNext()){
                val tag = getString(getColumnIndexOrThrow(COLUMN_TAG))
                val count = getInt(getColumnIndexOrThrow("count"))
                tagCounts[tag] = count
            }
        }
        cursor.close()
        db.close()
        emit(tagCounts)
    }
}