package com.example.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.note.ui.theme.NoteTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteTheme {
                Surface (
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ){
                    AccountBookApp()
                }
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

@Composable
fun AddRecordDialog(onAddRecord: (AccountRecord) -> Unit, onDismiss: () -> Unit){
    var date by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("日期(yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("用途") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("标签") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.isNotBlank() && amount.isNotBlank() && purpose.isNotBlank() && tag.isNotBlank()) {
                        onAddRecord(AccountRecord(UUID.randomUUID().toString(), date, amount.toDouble(), purpose, tag))
                    }
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun RecordList(
    records: List<AccountRecord>,
    onDeleteRecord: (AccountRecord) -> Unit,
    onUpdateRecord: (AccountRecord) -> Unit
){
    LazyColumn {
        items(records){ record ->
            RecordItem(
                record = record,
                onDeleteRecord = onDeleteRecord,
                onUpdateRecord = onUpdateRecord
            )
        }
    }
}

@Composable
fun RecordItem(
    record: AccountRecord,
    onDeleteRecord: (AccountRecord) -> Unit,
    onUpdateRecord: (AccountRecord) -> Unit
){
    var isEditing by remember { mutableStateOf(false) }
    var editedAmount by remember { mutableStateOf(record.amount.toString()) }
    var editedPurpose by remember { mutableStateOf(record.purpose) }
    var editedTag by remember { mutableStateOf(record.tag) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)){
            Text("日期：${record.date}")
            if(isEditing){
                OutlinedTextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editedPurpose,
                    onValueChange = { editedPurpose = it },
                    label = { Text("用途") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editedTag,
                    onValueChange = { editedTag = it },
                    label = { Text("标签") },
                    modifier = Modifier.fillMaxWidth()
                )
            }else{
                Text("金额: ${record.amount}")
                Text("用途: ${record.purpose}")
                Text("标签: ${record.tag}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                TextButton(onClick = { onDeleteRecord(record) }) {
                    Text("删除")
                }
                if(isEditing){
                    TextButton(onClick = {
                        onUpdateRecord(record.copy(
                            amount = editedAmount.toDoubleOrNull() ?: record.amount,
                            purpose = editedPurpose,
                            tag = editedTag
                        ))
                        isEditing = false
                    }) {
                        Text("保存")
                    }
                }else {
                    TextButton(onClick = { isEditing = true }) {
                        Text("编辑")
                    }
                }
            }
        }
    }
}

@Composable
fun TagStatsDialog(tagCounts: Map<String, Int>, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("标签统计") },
        text = {
            LazyColumn {
                items(tagCounts.entries.toList()) { (tag, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tag)
                        Text(count.toString())
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}