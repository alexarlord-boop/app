package com.example.app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import kotlin.math.roundToInt

class MainActivityScreen : AppCompatActivity() {
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var workbookHandler = WorkBookHandler()
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

    private var _position: MutableLiveData<Int> = MutableLiveData(0)
    var position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        MutableLiveData("storage/emulated/0/download/control1.xls")
    val filename: LiveData<String> = _filename

    fun onPositionChange(newPosition: Int) {
        _position.value = newPosition
    }

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
    val lastClickedRecord = viewModel.position.observeAsState(0)

    Column {

        Surface(
            modifier = Modifier
                .height(250.dp)
                .shadow(5.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(2F)
                    .fillMaxWidth()

                    .padding(10.dp)
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FileBtn(
                        "Из файла",
                        onClick = workBookHandler::getRecordsFromFile,

                        viewModel = viewModel
                    )
                    FileBtn(
                        "С сервера",
                        onClick = workBookHandler::getRecordsFromServer,

                        viewModel = viewModel
                    )
                    Selector(viewModel)
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (records.size != 0) records[0].area else "Район",
                        fontSize = MaterialTheme.typography.h5.fontSize,
                        fontWeight = FontWeight(200)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(10F)
                .padding(10.dp)
        ) {
            itemsIndexed(records) { id, record ->
                RecordItem(id, record, viewModel)
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
    onClick: (String) -> Unit,
) {
    val filename by viewModel.filename.observeAsState("storage/emulated/0/download/control1.xls")
    val context = LocalContext.current
    ExtendedFloatingActionButton(
        modifier = Modifier.padding(10.dp),
        backgroundColor = Color.LightGray,
        text = { Text(title) },
        onClick = {
            try {
                onClick(filename)
                viewModel.onPositionChange(0)
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, "Нет файла!", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
fun RecordItem(id: Int, record: RecordDto, viewModel: MainViewModel) {
    val padding = 5.dp
    val margin = 10.dp
    Surface(

        modifier = Modifier
            .clickable(onClick = { viewModel.onPositionChange(id) })
            .border(2.dp, Color.LightGray)
            .shadow(5.dp)
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = record.street, fontSize = MaterialTheme.typography.h6.fontSize)
                Text(
                    text = record.name,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontStyle = MaterialTheme.typography.h6.fontStyle
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(5f), horizontalArrangement = Arrangement.Start) {
                    Text(
                        modifier = Modifier.weight(5F), text = record.houseNumber.split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        modifier = Modifier.weight(5F),
                        text = record.flatNumber.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }
                Row(
                    modifier = Modifier.weight(5f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = record.ko_D.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )

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

@Preview
@Composable
fun ShowMainScreen() {
    MainScreen(workBookHandler = WorkBookHandler())
}

//@Preview
//@Composable
//fun ShowRecord() {
//
//    val record = RecordDto(
//        "Батецкий",
//        "Звездная",
//        "43в",
//        12.0,
//        1234.0,
//        "Иванова М.Ф.",
//        "1234567890",
//        "12234Ь2344-ывваЦУК 1234",
//        LocalDateTime.now(),
//        12345.0,
//        12345.0,
//        0.0,
//        0.0,
//        "не живут",
//        34567.0,
//        -1
//    )
//    RecordItem(id = 1, record = record)
//}


