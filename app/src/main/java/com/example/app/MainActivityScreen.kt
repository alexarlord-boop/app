package com.example.app

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.parcel.RawValue
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDateTime

class MainActivityScreen : AppCompatActivity() {
    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var filename = "storage/emulated/0/download/control1.xls"
    var workbookHandler = WorkBookHandler(filename)
    var clickedRecordId = -1
    var clickedControllerId = -1
    lateinit var houseHeader: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {

            MainScreen(workbookHandler)

        }
    }


    fun visualiseData(records: MutableList<RecordDto>) {
        Toast.makeText(this, "${records.toString()}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun FileBtn(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        backgroundColor = Color.LightGray,
        onClick = { onClick() },
        text = { Text("Из файла") }
    )
}

@Composable
fun MainScreen(workbookHandler: WorkBookHandler) {
    val records = workbookHandler.listOfRecords

Column() {


    Row(
        modifier = Modifier
            .weight(1F)
//            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp),
        horizontalArrangement = Arrangement.End
    ) {
        FileBtn(workbookHandler::getRecordsFromFile)
    }


    LazyColumn(
        modifier = Modifier
            .weight(10F)
            .padding(10.dp)
    ) {
        itemsIndexed(records) { id, record ->
            RecordItem(id, record)
        }
    }
}
}

@Composable
fun RecordList(records: List<RecordDto>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
    ) {
        itemsIndexed(records) { id, record ->
            RecordItem(id, record)
        }
    }
}

@Composable
fun RecordItem(id: Int, record: RecordDto) {
    val padding = 5.dp
    val margin = 10.dp
    Surface(

        modifier = Modifier
            .clickable(onClick = {})
            .border(2.dp, Color.LightGray)
            .shadow(9.dp)
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = record.street, fontSize = MaterialTheme.typography.h5.fontSize)
                Text(text = record.name, fontSize = MaterialTheme.typography.h5.fontSize)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(5f), horizontalArrangement = Arrangement.Start) {
                    Text(text = record.houseNumber, fontSize = MaterialTheme.typography.h5.fontSize)
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = record.flatNumber.toString(),
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }
                Row(
                    modifier = Modifier.weight(5f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = record.houseNumber, fontSize = MaterialTheme.typography.h5.fontSize)

                    Text(
                        text = record.flatNumber.toString(),
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }

            }
        }
    }
    Spacer(modifier = Modifier.height(margin))
}

@Preview
@Composable
fun ShowRecord() {

    val record = RecordDto(
        "Батецкий",
        "Звездная",
        "43в",
        12.0,
        1234.0,
        "Иванова М.Ф.",
        "1234567890",
        "12234Ь2344-ывваЦУК 1234",
        LocalDateTime.now(),
        12345.0,
        12345.0,
        0.0,
        0.0,
        "не живут",
        34567.0,
        -1
    )
    RecordItem(id = 1, record = record)
}


