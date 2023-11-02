package com.example.app.Modals

import android.util.Log
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ServerHandler
import com.example.app.record.RecordDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val statements = mutableListOf(
    ServerHandler.RecordStatement(
        listNumber = "40",
        listDate = "26.10.2023",
        source = "Test",
        staffLink = "54",
        staffName = "Быкова Н.Н.",
        companyLnk = "3",
        firstAddress = "Колхозный переулок, 10"
    ), ServerHandler.RecordStatement(
        listNumber = "41",
        listDate = "26.10.2023",
        source = "Test",
        staffLink = "54",
        staffName = "Быкова Н.Н.",
        companyLnk = "3",
        firstAddress = "Торговый переулок, 11"
    ),
    ServerHandler.RecordStatement(
        listNumber = "42",
        listDate = "26.10.2023",
        source = "Test",
        staffLink = "54",
        staffName = "Быкова Н.Н.",
        companyLnk = "3",
        firstAddress = "Тихий переулок, 1"
    )
)

@Preview
@Composable
fun ShowModalDialog() {
    AlertDialog(
        shape = RoundedCornerShape(15.dp),
        onDismissRequest = { },
        title = { Text(text = "Ведомости") },
        text = {
            Column {

                statements.sortedBy { it.listNumber.toInt() }.forEach { item ->

                    // TODO:- add 1st list address for each row(=list)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(
                                2.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.listDate,
                                fontSize = MaterialTheme.typography.h6.fontSize,
                                fontWeight = FontWeight(300),
                                modifier = Modifier.padding(10.dp)
                            )

                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(12.dp)
                            ) {
                                Text(text = item.listNumber)
                            }
                        }
                        Text(
                            text = item.firstAddress.split(", ")[0],
                            fontSize = MaterialTheme.typography.h6.fontSize,
                            fontWeight = FontWeight(300),
                            modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 5.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { }) {
                Text(text = "Закрыть")
            }
        }
    )
}
