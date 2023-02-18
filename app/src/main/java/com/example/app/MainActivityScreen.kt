package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoveUp

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.apache.poi.EmptyFileException
import java.io.FileNotFoundException
import java.time.LocalDateTime

var FILE_NAME = ""
var SOURCE_OPTION = MainViewModel.SourceOption.NONE
var LAST_LIST_POSITION = 0

class MainActivityScreen : AppCompatActivity() {
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var workbookHandler = WorkBookHandler()
    var viewModel: MainViewModel = MainViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MainScreen(workbookHandler, viewModel)

        }
    }

    override fun onResume() {
        super.onResume()
        if (FILE_NAME.isNotEmpty()) {
            workbookHandler.getRecordsFromFile(FILE_NAME)
        }
        viewModel.onSourceOptionChange(SOURCE_OPTION)
    }


}
class MainViewModel : ViewModel() {
    enum class SourceOption(val id: Int) {
        NONE(-1),
        FILE(0),
        SERVER(1)
    }

    private val _sourceOption: MutableLiveData<SourceOption> = MutableLiveData(SourceOption.NONE)
    val sourceOption: LiveData<SourceOption> = _sourceOption

    private val _fileId: MutableLiveData<String> = MutableLiveData("1")
    val fileId: LiveData<String> = _fileId

    private var _position: MutableLiveData<Int> = MutableLiveData(0)
    var position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        MutableLiveData("storage/emulated/0/download/control1.xls")
    val filename: LiveData<String> = _filename

    fun onSourceOptionChange(newSrcOption: SourceOption) {
        _sourceOption.value = newSrcOption
        SOURCE_OPTION = newSrcOption
    }

    fun onPositionChange(newPosition: Int) {
        _position.value = newPosition
        LAST_LIST_POSITION = newPosition
    }

    fun fileChange() {
        _filename.value = filename.value?.split("/")?.toMutableList()?.also {
            it[it.lastIndex] = "control${_fileId.value}.xls"
        }?.joinToString("/")
        FILE_NAME = filename.value.toString()
    }

    fun onIdChange(newId: String) {
        _fileId.value = newId
        fileChange()
    }
}

@Preview
@Composable
fun showMain() {
    val record = RecordDto(
        "Район Интересный",
        "Сонная",
        "2",
        1.0,
        1234.0,
        "Обломов И.И.",
        "1234567890",
        "12234Ь2344-ывваЦУК 1234",
        LocalDateTime.now(),
        12345.0,
        12345.0,
        10.0,
        220.0,
        "отдыхают",
        34567.0,
        -1
    )
    val workBookHandler = WorkBookHandler()
    workBookHandler.onRecordListChange(List(10){ index -> record})
    MainScreen(workBookHandler = workBookHandler, MainViewModel())
}

@Composable
fun MainScreen(workBookHandler: WorkBookHandler, viewModel: MainViewModel) {
    val sourceOption = viewModel.sourceOption.observeAsState(SOURCE_OPTION)
    val records = workBookHandler.listOfRecords.observeAsState(emptyList())
    val lastClicked = viewModel.position.observeAsState(LAST_LIST_POSITION)

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {

        Surface(
            modifier = Modifier
                .height(200.dp)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlertDialog(viewModel)

                    if (sourceOption.value.id == 0) {
                        FileBtn(
                            "Из файла",
                            onClick = workBookHandler::getRecordsFromFile,
                            viewModel = viewModel
                        )
                    } else if (sourceOption.value.id == 1) {
                        FileBtn(
                            "С сервера",
                            onClick = workBookHandler::getRecordsFromServer,
                            viewModel = viewModel
                        )
                    }


                    Selector(viewModel)
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (records.value.isNotEmpty()) records.value[0].area else "Район",
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
            itemsIndexed(records.value) { id, record ->
                RecordItem(id, record, viewModel)
            }
        }
        val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
        val showLastButton by remember { derivedStateOf { lastClicked.value > 0 } }
        AnimatedVisibility(
            visible = showLastButton || showButton,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                AnimatedVisibility(visible = showLastButton, enter = fadeIn(),
                    exit = fadeOut(),) {
                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                        shape = CircleShape,
                        onClick = {
                            coroutineScope.launch {
                                // Animate scroll to the 10th item
                                listState.animateScrollToItem(index = LAST_LIST_POSITION)
                            }
                        }
                    ) {
                        Icon(Icons.Default.MoveUp, contentDescription = null)
                    }
                }

                AnimatedVisibility(visible = showButton) {
                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                        shape = CircleShape,
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 0)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertDialog(viewModel: MainViewModel){
    MaterialTheme {
        Column {
            val openDialog = remember { mutableStateOf(false)  }

            Button(shape = RoundedCornerShape(10.dp),
                onClick = {
                openDialog.value = true
            }) {
                Text("Иcточник")
            }

            if (openDialog.value) {

                AlertDialog(
                    onDismissRequest = {
                        // Dismiss the dialog when the user clicks outside the dialog or on the back
                        // button. If you want to disable that functionality, simply use an empty
                        // onCloseRequest.
                        openDialog.value = false
                    },
                    title = {
                        Text(text = "Источник данных")
                    },
                    text = {
                        Text("Выберите, откуда ходите получить записи обхода")
                    },
                    confirmButton = {
                        Button(

                            onClick = {
                                openDialog.value = false
                                viewModel.onSourceOptionChange(MainViewModel.SourceOption.FILE)
                            }) {
                            Text("Зарузить из файла")
                        }
                    },
                    dismissButton = {
                        Button(

                            onClick = {
                                openDialog.value = false
                                viewModel.onSourceOptionChange(MainViewModel.SourceOption.SERVER)
                            }) {
                            Text("Скачать с сервера")
                        }
                    }
                )
            }
        }

    }
}
//@Preview
@Composable
fun ShowDialog(){
    AlertDialog(MainViewModel())
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
    Button(
        modifier = Modifier.padding(10.dp),
        shape = RoundedCornerShape(10.dp),
        onClick = {
            try {
                onClick(filename)
                FILE_NAME = filename
            }
            catch (ex: EmptyFileException) {
                Toast.makeText(context, "Пустой файл!", Toast.LENGTH_SHORT).show()
            }
            catch (ex: FileNotFoundException) {
                Toast.makeText(context, "Нет файла!", Toast.LENGTH_SHORT).show()
                viewModel.onPositionChange(0)
            }
        }
    ) {
        Text(title)
    }
}

@Composable
fun RecordItem(id: Int, record: RecordDto, viewModel: MainViewModel) {
    val padding = 5.dp
    val margin = 10.dp
    val context = LocalContext.current
    val filename = viewModel.filename.observeAsState("storage/emulated/0/download/control1.xls")
    val lastPosition = viewModel.position.observeAsState(-1)
    val selected = id == lastPosition.value

    Card(

        modifier = Modifier
            .clickable(onClick = {
                viewModel.onPositionChange(id)

                val intent = Intent(context, RecordActivity::class.java)
                intent.putExtra("filename", filename.value)
                intent.putExtra("position", id)
                intent.putExtra(
                    "lastDate",
                    WorkBookHandler().convertDateToFormattedString(record.lastKoDate)
                )
                val gson = Gson()
                intent.putExtra("recordData", gson.toJson(record))
                context.startActivity(intent)
            }),
        backgroundColor = if (selected) Color.LightGray else Color.White,
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = record.street.split(" ")[0],
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight(300))
                Text(
                    text = record.name,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight(500)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(modifier = Modifier, horizontalArrangement = Arrangement.Start) {
                    Text(modifier = Modifier, text = "д: ",
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(300)
                    )
                    Text(
                        modifier = Modifier, text = record.houseNumber.split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(modifier = Modifier, text = "кв: ",
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(300)
                    )
                    Text(
                        modifier = Modifier,
                        text = record.flatNumber.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )
                }
                Row(
                    modifier = Modifier.weight(5f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.baseline_light_mode_24), contentDescription = "")
                    val color = Color(46, 133, 64, 255)
                    var fieldValue = record.ko_D
                    Text(
                        text = fieldValue.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(600),
                        color = if (fieldValue > 0) color else Color.Black
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(painter = painterResource(id = R.drawable.baseline_dark_mode_24), contentDescription = "")
                    fieldValue = record.ko_N
                    Text(
                        color = if (fieldValue > 0) color else Color.Black,
                        text = record.ko_N.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(600),
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
    MainScreen(workBookHandler = WorkBookHandler(), viewModel = MainViewModel())
}

@Composable
fun UpButton() {
    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
        shape = CircleShape,
        onClick = {
//            coroutineScope.launch {
//                // Animate scroll to the 10th item
//                listState.animateScrollToItem(index = 0)
//            }
        }
    ) {
        Icon(Icons.Default.ArrowUpward, contentDescription = null)
    }
}

//@Preview
@Composable
fun showUpButton() {
    UpButton()
}

//@Preview
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
    RecordItem(id = 1, record = record, MainViewModel())
}


