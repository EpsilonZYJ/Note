package com.example.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.note.ui.theme.NoteTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteTheme {

            }
        }
    }
}

@Composable
fun AccountBookApp(viewModel: AccountBookViewModel = viewModel()){
    val records by viewModel.records.collectAsState()
    var showAddForm by remember {mutableStateOf(false)}
    var showTagStats by remember { mutableStateOf(false) }
    var searchDate by remember { mutableStateOf("")}
    val filteredRecords by if (searchDate.isBlank()){
        viewModel.records.collectAsState()
    }else {
        viewModel.getRecordsByDate(searchDate).collectAsState()
    }

    Column(modifier = Modifier.padding(16.dp)){
        Text(
            text = "记账本",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Button(onClick = { showAddForm = true }){
                Text("添加记录")
            }
            Button(onClick = { showTagStats = true}){
                Text("标签统计")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RecordList(
            records = filteredRecords,
            onDeleteRecord = viewModel::deleteRecord,
            onUpdateRecord = viewModel::updateRecord
        )
    }

    if(showAddForm) {
        AddRecordDialog(
            onAddRecord = { record ->
                viewModel.addRecord(record)
                showAddForm = false
            },
            onDismiss = { showAddForm = false}
        )
    }

    if(showTagStats) {
        val tagCounts by viewModel.tagCounts.collectAsState()
        TagStatsDialog(
            tagCounts = tagCounts,
            onDismiss = { showTagStats = false }
        )
    }
}

