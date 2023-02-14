package com.example.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.parcel.RawValue
import java.io.File
import java.io.FileNotFoundException
import java.nio.channels.Selector
import java.time.LocalDateTime

class MainActivityScreen : AppCompatActivity() {
    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var workbookHandler = WorkBookHandler()
    var clickedRecordId = -1
    var clickedControllerId = -1
    lateinit var houseHeader: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {

            MainScreen(workbookHandler)

        }
    }


}

class MainViewModel : ViewModel() {

    private val _fileId: MutableLiveData<String> = MutableLiveData("1")
    val fileId: LiveData<String> = _fileId

    private val _filename: MutableLiveData<String> = MutableLiveData("storage/emulated/0/download/control1.xls")
    val filename: LiveData<String> = _filename

    fun fileChange() {
        _filename.value = filename.value?.split("/")?.toMutableList()?.also {
            it[it.lastIndex] = "control${_fileId.value}.xls"
        }?.joinToString("/")
    }

    fun onIdChange(newId: String) {
        _fileId.value = newId
        fileChange()
    }
}


@Composable
fun MainScreen(workBookHandler: WorkBookHandler, viewModel: MainViewModel = MainViewModel()) {
    val records = workBookHandler.listOfRecords


    Column() {
        Spacer(modifier = Modifier.height(50.dp))
        Column(
            modifier = Modifier
                .weight(2F)
                .fillMaxWidth()
                .height(100.dp)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FileBtn("Из файла", workBookHandler = workBookHandler, viewModel = viewModel)
                FileBtn("С сервера", workBookHandler = workBookHandler, viewModel = viewModel)
                Selector(viewModel)
            }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Selector(viewModel: MainViewModel) {
    val options = listOf("1", "2", "3", "4", "5")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(
        expanded = true, onExpandedChange = {
            expanded = !expanded
        }, modifier = Modifier
            .width(100.dp)
            .padding(10.dp)
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(25.dp))
    ) {

        ExtendedFloatingActionButton(text = { Text(selectedOptionText) },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            onClick = { /*TODO*/ })

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { optionText ->
                DropdownMenuItem(onClick = {
                    selectedOptionText = optionText
                    viewModel.onIdChange(optionText)
                    expanded = false
                }) {
                    Text(text = optionText)
                }
            }
        }
    }
}

@Composable
fun FileBtn(
    title: String,
    viewModel: MainViewModel,
    workBookHandler: WorkBookHandler
) {

    val context = LocalContext.current
    val fileId by viewModel.fileId.observeAsState("1")
    val filename by viewModel.filename.observeAsState("storage/emulated/0/download/control1.xls")
    ExtendedFloatingActionButton(
        modifier = Modifier.padding(10.dp),
        backgroundColor = Color.LightGray,
        onClick = {

            try {
                Log.i("MyLog", filename)
                workBookHandler.getRecordsFromFile(filename)
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, "Нет файла!", Toast.LENGTH_SHORT).show()
            }
        },
        text = { Text(title) }
    )
}

@Composable
fun RecordItem(id: Int, record: RecordDto) {
    val padding = 5.dp
    val margin = 10.dp
    Surface(

        modifier = Modifier
            .clickable(onClick = {})
            .border(2.dp, Color.LightGray)
            .shadow(5.dp)
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = record.street, fontSize = MaterialTheme.typography.h6.fontSize)
                Text(text = record.name, fontSize = MaterialTheme.typography.h6 .fontSize, fontStyle = MaterialTheme.typography.h6.fontStyle)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(5f), horizontalArrangement = Arrangement.Start) {
                    Text(modifier = Modifier.weight(5F), text = record.houseNumber.split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize)
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(modifier = Modifier.weight(5F),
                        text = record.flatNumber.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }
                Row(
                    modifier = Modifier.weight(5f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = record.ko_D .toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize)

                    Text(
                        text = record.ko_N.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }

            }
        }
    }
    Spacer(modifier = Modifier.height(margin))
}

//@Preview
@Composable
fun ShowMainScreen() {
    MainScreen(workBookHandler = WorkBookHandler())
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


