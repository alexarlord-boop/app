//package com.example.app.ModalsWithoutMethodsPreviewOnly
//
//import android.util.Log
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.AlertDialog
//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.app.data.RecordStatement
//import com.example.app.record.RecordDto
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//
//val statements = mutableListOf(
//    RecordStatement(
//        listNumber = "40",
//        listDate = "26.10.2023",
//        source = "Test",
//        staffLink = "54",
//        staffName = "Быкова Н.Н.",
//        companyLnk = "3",
//        firstAddress = "Колхозный переулок, 10"
//    ),
//    RecordStatement(
//        listNumber = "41",
//        listDate = "26.10.2023",
//        source = "Test",
//        staffLink = "54",
//        staffName = "Быкова Н.Н.",
//        companyLnk = "3",
//        firstAddress = "Торговый переулок, 11"
//    ),
//    RecordStatement(
//        listNumber = "42",
//        listDate = "26.10.2023",
//        source = "Test",
//        staffLink = "54",
//        staffName = "Быкова Н.Н.",
//        companyLnk = "3",
//        firstAddress = "Тихий переулок, 1"
//    )
//)
//
//
//
//// function with commented methods
////@Preview
////@Composable
////fun ShowStatementsDialog() {
////    Log.w("DATA", statements.toString())
////    AlertDialog(
////        shape = RoundedCornerShape(15.dp),
//////        onDismissRequest = { isDialogVisible = false },
////        onDismissRequest = {  },
////        title = { Text(text = "Ведомости") },
////        text = {
////            Column {
////
//////                statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
////                statements.sortedBy { it.listNumber.toInt() }.forEach { item ->
////
////
////                    Column(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .padding(bottom = 10.dp)
////                            .border(
////                                2.dp,
////                                color = Color.LightGray,
////                                shape = RoundedCornerShape(10.dp)
////                            ),
////                        horizontalAlignment = Alignment.Start
////                    ) {
////                        Row(
////                            modifier = Modifier
////                                .fillMaxWidth()
////                                .padding(vertical = 0.dp)
////                                .height(55.dp),
////                            horizontalArrangement = Arrangement.Start,
////                            verticalAlignment = Alignment.CenterVertically
////                        ) {
////                            Text(
////                                text = item.listDate,
////                                fontSize = MaterialTheme.typography.h6.fontSize,
////                                fontWeight = FontWeight(300),
////                                modifier = Modifier.padding(horizontal = 10.dp)
////                            )
////
////                            Button(
////                                onClick = {
//////                                    viewModel.onStatementIdChange(item.listNumber)
//////                                    with(sharedPreferences.edit()) {
//////                                        putString("statementId", item.listNumber)
//////                                        apply()
//////                                    }
//////
//////                                    cs.launch {
//////                                        try {
//////                                            selectedControllerId.value?.let { controllerId ->
//////                                                withContext(Dispatchers.IO) {
//////                                                    val data: List<RecordDto> =
//////                                                        dataHandler.getRecordsForStatement(
//////                                                            controllerId,
//////                                                            item.listNumber,
//////                                                            context
//////                                                        )
//////                                                    withContext(Dispatchers.Main) {
//////                                                        viewModel.onRecordListChange(data)
//////                                                    }
//////                                                }
//////                                            }
//////
//////
//////                                        } catch (e: Exception) {
//////                                            println(e.stackTraceToString())
//////                                        }
//////                                    }
//////
//////                                    viewModel.onPositionChange(-1)
//////                                    isDialogVisible = false
////                                },
////                                contentPadding = PaddingValues(),
////                                modifier = Modifier
////                                    .width(200.dp)
////                            ) {
////                                Text(text = item.listNumber, fontSize = MaterialTheme.typography.h6.fontSize)
////                            }
////                        }
////                        Text(
////                            text = item.firstAddress.split(", ")[0],
////                            fontSize = MaterialTheme.typography.h6.fontSize,
////                            fontWeight = FontWeight(300),
////                            modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 5.dp)
////                        )
////                    }
////                }
////            }
////        },
////        confirmButton = {
//////            Button(onClick = { isDialogVisible = false }) {
////            Button(onClick = {  }) {
////                Text(text = "Закрыть")
////            }
////        }
////    )
////}
//
//
//@Preview
//@Composable
//fun ShowStatementsDialog() {
//    Log.w("DATA", com.example.app.ModalsWithoutMethodsPreviewOnly.statements.toString())
//    AlertDialog(
//        shape = RoundedCornerShape(15.dp),
////        onDismissRequest = { isDialogVisible = false },
//        onDismissRequest = {  },
//        title = { Text(text = "Ведомости") },
//        text = {
//            Column {
////                statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
//                statements.sortedBy { it.listNumber.toInt() }.forEach { item ->
//                    com.example.app.ModalsWithoutMethodsPreviewOnly.statements.sortedBy { it.listNumber.toInt() }.forEach { item ->
//
//
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 10.dp)
//                                .border(
//                                    2.dp,
//                                    color = Color.LightGray,
//                                    shape = RoundedCornerShape(10.dp)
//                                ),
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 0.dp)
//                                    .height(55.dp),
//                                horizontalArrangement = Arrangement.Start,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = item.listDate,
//                                    fontSize = MaterialTheme.typography.h6.fontSize,
//                                    fontWeight = FontWeight(300),
//                                    modifier = Modifier.padding(horizontal = 10.dp)
//                                )
////
//                                Button(
////                                    onClick = {
////                                        viewModel.onStatementIdChange(item.listNumber)
////                                        with(sharedPreferences.edit()) {
////                                            putString("statementId", item.listNumber)
////                                            apply()
////                                        }
////
////                                        cs.launch {
////                                            try {
////                                                selectedControllerId.value?.let { controllerId ->
////                                                    withContext(Dispatchers.IO) {
////                                                        val data: List<RecordDto> =
////                                                            dataHandler.getRecordsForStatement(
////                                                                controllerId,
////                                                                item.listNumber,
////                                                                context
////                                                            )
////                                                        withContext(Dispatchers.Main) {
////                                                            viewModel.onRecordListChange(data)
////                                                        }
////                                                    }
////                                                }
////
////
////                                            } catch (e: Exception) {
////                                                println(e.stackTraceToString())
////                                            }
////                                        }
////
////                                        viewModel.onPositionChange(-1)
////                                        isDialogVisible = false
////                                    },
//                                    onClick = {},
//                                    contentPadding = PaddingValues(),
//                                    modifier = Modifier
//                                        .width(200.dp)
//                                )
//                                {
//                                    Text(text = item.listNumber, fontSize = MaterialTheme.typography.h6.fontSize)
//                                }
//                            }
//                            Text(
//                                text = item.firstAddress.split(", ")[0],
//                                fontSize = MaterialTheme.typography.h6.fontSize,
//                                fontWeight = FontWeight(300),
//                                modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 5.dp)
//                            )
//                        }
//                    }
//                }
//
//            }},
//        confirmButton = {
////            Button(onClick = { isDialogVisible = false }) {
//                Button(onClick = {  }) {
//                Text(text = "Закрыть")
//            }
//        }
//    )
//}