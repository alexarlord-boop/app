package com.example.app


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.app.data.*
import com.example.app.navigation.Screen
import com.example.app.record.RecordDto
import kotlinx.coroutines.*
import java.io.File
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import com.example.app.data.*
import kotlinx.coroutines.*

@Composable
fun StatementItem(
    id: Int,
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
            horizontalArrangement = Arrangement.SpaceBetween, // Align items to the start and end
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.listDate,
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight(300),
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f)) // Spacer to push the button to the right

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
//
//@Composable
//fun AlertDialogContent(
//    statements: State<List<RecordStatement>>,
//    onStatementSelected: (String) -> Unit
//) {
////    Column {
////        statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
////            StatementItem(item = item, onStatementSelected = onStatementSelected)
////        }
////    }
//
//    val sortedStatements = statements.value.sortedBy { it.listNumber.toInt() }
//    LazyColumn (modifier = Modifier) {
////        item {
////            Text(text = "Ведомости", fontWeight = FontWeight.Bold, fontSize = 20.sp)
////        }
//        itemsIndexed(sortedStatements) { id, item ->
//            StatementItem(id, item = item, onStatementSelected = onStatementSelected)
//        }
//    }
//
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .verticalScroll(rememberScrollState())
////    ) {
////        Column {
////            statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
////                StatementItem(item = item, onStatementSelected = onStatementSelected)
////            }
////        }
////    }
//}

//
//@Composable
//fun StatementDialog(
//    statements: State<List<RecordStatement>>,
//    isDialogVisible: Boolean,
//    onDismiss: () -> Unit,
//    onStatementSelected: (String) -> Unit
//) {
//    AlertDialog(
//        shape = RoundedCornerShape(15.dp),
//        onDismissRequest = { onDismiss() },
//        title = {Text(text = "Ведомости", fontWeight = FontWeight.Bold, fontSize = 20.sp)},
//        text = {
//            Column (modifier = Modifier.padding(vertical = 15.dp)) {
//                AlertDialogContent(
//                    statements = statements,
//                    onStatementSelected = onStatementSelected
//                )
//            }
//        },
//        confirmButton = {
//            Button(onClick = { onDismiss() }) {
//                Text(text = "Закрыть")
//            }
//        },
//
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(500.dp)
//    )
//}

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
        title = { Text(text = "Ведомости", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            ComposeView {
                AlertDialogContent(
                    statements = statements,
                    onStatementSelected = onStatementSelected
                )
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Закрыть")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(700.dp)
    )
}

@Composable
fun AlertDialogContent(
    statements: State<List<RecordStatement>>,
    onStatementSelected: (String) -> Unit
) {
    val sortedStatements = statements.value.sortedBy { it.listNumber.toInt() }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(sortedStatements) { id, item ->
            StatementItem(id, item = item, onStatementSelected = onStatementSelected)
        }
    }
}

@Composable
fun ComposeView(content: @Composable (Modifier) -> Unit) {
    AndroidView(
        modifier = Modifier.height(550.dp),
        factory = { context ->
            androidx.compose.ui.platform.ComposeView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.setContent {
                content(Modifier.fillMaxSize())
            }
        }
    )
}

@Preview
@Composable
fun StatementDialogPreview() {
    val statements = remember {
        mutableStateOf(listOf(
            RecordStatement("1", "2024-02-06", "Source1", "StaffLink1", "StaffName1", "CompanyLink1", "Address1"),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
            RecordStatement("2", "2024-02-07", "Source2", "StaffLink2", "StaffName2", "CompanyLink2", null),
        ))
    }

    StatementDialog(
        statements = statements,
        isDialogVisible = true,
        onDismiss = {},
        onStatementSelected = {}
    )
}