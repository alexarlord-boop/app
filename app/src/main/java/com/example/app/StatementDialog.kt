package com.example.app


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import com.example.app.data.*
import kotlinx.coroutines.*

@Composable
fun StatementItem(
    item: RecordStatement,
    onStatementSelected: (String) -> Unit
) {
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
                .padding(vertical = 0.dp)
                .height(55.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.listDate,
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight(300),
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            StatementButton(item = item, onStatementSelected = onStatementSelected)
        }
        Text(
            text = item.firstAddress?.split(", ")?.getOrNull(0) ?: "нет адреса",
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight(300),
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(bottom = 5.dp)
        )
    }
}

@Composable
fun StatementButton(item: RecordStatement, onStatementSelected: (String) -> Unit) {
    Button(
        onClick = {
            onStatementSelected(item.listNumber)
            // ... rest of your button logic
        },
        contentPadding = PaddingValues(),
        modifier = Modifier.width(200.dp)
    ) {
        Text(text = item.listNumber, fontSize = MaterialTheme.typography.h6.fontSize)
    }
}

@Composable
fun AlertDialogContent(
    statements: State<List<RecordStatement>>,
    onStatementSelected: (String) -> Unit
) {
    Column {
        statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
            StatementItem(item = item, onStatementSelected = onStatementSelected)
        }
    }
}


@Composable
fun StatementDialog(
    statements: State<List<RecordStatement>>,
    isDialogVisible: Boolean,
    onDismiss: () -> Unit,
    onStatementSelected: (String) -> Unit
) {
    AlertDialog(
        shape = RoundedCornerShape(15.dp),
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Ведомости") },
        text = { AlertDialogContent(statements = statements, onStatementSelected = onStatementSelected) },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Закрыть")
            }
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}






@Preview
@Composable
fun StatementDialogPreview() {
    val statements = remember {
     mutableStateOf(listOf(
        RecordStatement("1", "2024-02-06", "Source1", "StaffLink1", "StaffName1", "CompanyLink1", "Address1"),
        RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null)
    ))
    }

    StatementDialog(
        statements = statements,
        isDialogVisible = true,
        onDismiss = {},
        onStatementSelected = {}
    )
}