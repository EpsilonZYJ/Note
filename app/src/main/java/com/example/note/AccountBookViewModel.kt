package com.example.note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountBookViewModel(application: Application) : AndroidViewModel(application){
    private val dbHelper = DatabaseHelper(application)

    val records: StateFlow<List<AccountRecord>> = dbHelper.getAllRecords().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addRecord(record: AccountRecord){
        viewModelScope.launch(Dispatchers.IO){
            dbHelper.insertRecord(record)
        }
    }

    fun deleteRecord(record: AccountRecord){
        viewModelScope.launch(Dispatchers.IO){
            dbHelper.deleteRecord(record)
        }
    }

    fun updateRecord(updatedRecord: AccountRecord){
        viewModelScope.launch(Dispatchers.IO){
            dbHelper.updateRecord(updatedRecord)
        }
    }

    fun getRecordsByDate(date: String): StateFlow<List<AccountRecord>>{
        return dbHelper.getRecordsByDate(date).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    val tagCounts: StateFlow<Map<String, Int>> = dbHelper.getTagCounts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )
}