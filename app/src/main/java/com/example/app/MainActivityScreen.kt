package com.example.app

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.parcel.RawValue
import java.io.File
import java.io.FileNotFoundException

class MainActivityScreen : AppCompatActivity() {
    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var filename = "storage/emulated/0/download/control1.xls"
    lateinit var workbookHandler: WorkBookHandler
    var clickedRecordId = -1
    var clickedControllerId = -1
    lateinit var houseHeader: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val records = remember { mutableStateListOf<RecordDto>() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FileBtn(::reloadData)
            }
            RecordList(records = records)
        }
    }


    private fun reloadData() {
        workbookHandler = WorkBookHandler(filename)
        filename.let {
            val file = it.split('/').last()
            try {
                workbookHandler = WorkBookHandler(it)
                workbookHandler.readWorkBookFromFile()
                visualiseData(workbookHandler.records)
                Toast.makeText(this, "Загружено из: $file", Toast.LENGTH_SHORT).show()
            } catch (ex: FileNotFoundException) {
                visualiseData(mutableListOf())
                workbookHandler.clearRecords()

                Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            }
            Log.i("MyLog", "RELOADED")
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
fun RecordList(records: MutableList<RecordDto>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
    ) {
        itemsIndexed(records) {
                id, record -> RecordItem(id, record)
        }
    }
}

@Composable
fun RecordItem(id: Int, record: RecordDto) {
    Text(text = id.toString(), color = Color.Red)
}



