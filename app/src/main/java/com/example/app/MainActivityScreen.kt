package com.example.app


import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.apache.poi.EmptyFileException
import java.io.FileNotFoundException
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDateTime
import kotlin.reflect.KFunction1

var FILE_NAME = ""
var SOURCE_OPTION = MainViewModel.SourceOption.NONE
var LAST_LIST_POSITION = -1

class MainActivityScreen : AppCompatActivity() {
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var workbookHandler = WorkBookHandler()
    var serverHandler = ServerHandler()
    var viewModel: MainViewModel = MainViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            setContent {
                MainScreen(workbookHandler, serverHandler, viewModel)
            }
        } else {
            // Show a dialog or handle the case when there is no network connectivity
            // You can display an error message or prompt the user to check their internet connection
            Toast.makeText(this, "Нет подключения к сети.", Toast.LENGTH_LONG).show()
            // You can also finish the activity if you don't want to proceed without network connectivity
            // finish()
        }
    }

    override fun onResume() {
        super.onResume()
        when (SOURCE_OPTION.id) {
            0 -> {
                if (FILE_NAME.isNotEmpty()) {
                    workbookHandler.getRecordsFromFile(FILE_NAME)
                }
//                viewModel.onSourceOptionChange(SOURCE_OPTION)
            }
            1 -> {
                serverHandler.reloadRecordsFromFile(viewModel.fileId.value.toString(), viewModel.stateId.value.toString(),this)
                println(serverHandler.listOfRecords.value?.size)
            }
        }
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

    private val _stateId: MutableLiveData<String> = MutableLiveData("0")
    val stateId: LiveData<String> = _stateId

    private var _position: MutableLiveData<Int> = MutableLiveData(-1)
    var position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        MutableLiveData("storage/emulated/0/download/control1.xls")
    val filename: LiveData<String> = _filename

    private val _controllerId: MutableLiveData<String> = MutableLiveData("0")
    var controllerId: LiveData<String> = _controllerId

    fun onSourceOptionChange(newSrcOption: SourceOption) {
        _sourceOption.value = newSrcOption
        SOURCE_OPTION = newSrcOption
    }

    fun onPositionChange(newPosition: Int) {
        _position.value = newPosition
        LAST_LIST_POSITION = newPosition
    }


    fun fileChange() {
        filename.value?.let { name ->
            val parts = name.split("/").toMutableList()
            parts[parts.lastIndex] = "control${_fileId.value}.xls"
            _filename.value = parts.joinToString("/")
            FILE_NAME = _filename.value ?: ""
        }
    }


    fun onIdChange(newId: String) {
        _fileId.value = newId
        fileChange()
    }

    fun onControllerChange(id: String) {
        _controllerId.value = id
    }

    fun onStateIdChange(newId: String) {
        _stateId.value = newId
    }
}

//@Preview
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
    workBookHandler.onRecordListChange(List(10) { index -> record })

}

@Composable
fun MainScreen(
    workBookHandler: WorkBookHandler,
    serverHandler: ServerHandler,
    viewModel: MainViewModel
) {
    val sourceOption = viewModel.sourceOption.observeAsState(SOURCE_OPTION)
    val bookRecords by workBookHandler.listOfRecords.observeAsState(emptyList())
    val serverRecords by serverHandler.listOfRecords.observeAsState(emptyList())
    val lastClicked = viewModel.position.observeAsState(LAST_LIST_POSITION)
    val id by viewModel.fileId.observeAsState(1)
    val stateId by viewModel.stateId.observeAsState("0")

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var sortedListToShow = if (sourceOption.value.id == 0) {
        bookRecords.sortedBy { it ->
            it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt()
        }
    } else {
        serverRecords.sortedBy { it ->
            it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt()
        }
    }
    LaunchedEffect(bookRecords, serverRecords) {
        sortedListToShow = if (sourceOption.value.id == 0) {
            bookRecords.sortedBy { it ->
                it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt()
            }
        } else {
            serverRecords.sortedBy { it ->
                it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt()
            }
        }
        println(sortedListToShow.size)
    }


    if (sourceOption.value.id == 0) {
        serverHandler.clearList()
    } else if (sourceOption.value.id == 1) {
        workBookHandler.clearList()
    }

    val area = sortedListToShow.firstOrNull()?.area ?: "Район"
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showLastButton by remember { derivedStateOf { lastClicked.value > 0 } }

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
//                    AlertDialog(viewModel)

//                    if (sourceOption.value.id == 0) {
//                        FileBtn(
//                            "Из файла",
//                            onClick = workBookHandler::getRecordsFromFile,
//                            viewModel = viewModel
//                        )
//                    } else if (sourceOption.value.id == 1) {
//
//                        Button(onClick = {
//                            serverHandler.getRecordsFromServer(id.toString(), stateId.toString(), context)
//                        }) {
//                            Text("С сервера")
//                        }
//                    }

//                    val showSelector by remember { derivedStateOf { sourceOption.value.id > -1 } }
//                    AnimatedVisibility(visible = showSelector) {
//                        Selector(viewModel, serverHandler)
//                    }
                    Selector(viewModel, serverHandler)
                }

                Column(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = area, // area title
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

            itemsIndexed(sortedListToShow) { id, record ->
                RecordItem(id, record, viewModel)
            }


        }

        AnimatedVisibility(
            visible = showLastButton || showButton,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                AnimatedVisibility(
                    visible = showLastButton, enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                        shape = CircleShape,
                        onClick = {
                            coroutineScope.launch {
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
fun AlertDialog(viewModel: MainViewModel) {

    MaterialTheme {
        Column {
            val openDialog = remember { mutableStateOf(false) }




        }
    }
}


//@Preview
@Composable
fun ShowDialog() {
    AlertDialog(MainViewModel())
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Selector(viewModel: MainViewModel, serverHandler: ServerHandler) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cs = CoroutineScope(Dispatchers.Main)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("id") }
    var fetchedData by remember { mutableStateOf(emptyList<ServerHandler.RecordStatement>()) }
    var isDialogVisible by remember { mutableStateOf(false) }

    val id by viewModel.fileId.observeAsState("1") // TODO:- check the value
    val stateId by viewModel.stateId.observeAsState("0")

    var options by remember { mutableStateOf(listOf("-")) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            options = ServerHandler().gerControllersFromServer().map { it.Staff_Lnk }
        }
    }

    // Function to show the modal dialog with fetched data
    @Composable
    fun ShowModalDialog() {
        AlertDialog(
            onDismissRequest = { isDialogVisible = false },
            title = { Text(text = fetchedData[0].staffName) },
            text = {
                Column {
                    fetchedData.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.listDate,
                                fontSize = MaterialTheme.typography.h6.fontSize,
                                fontWeight = FontWeight(300),
                                modifier = Modifier.padding(12.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.onStateIdChange(item.listNumber)
                                    serverHandler.getRecordsFromServer(id, item.listNumber, context)
                                },
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(12.dp)
                            ) {
                                Text(text = item.listNumber)
                            }


                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { isDialogVisible = false }) {
                    Text(text = "OK")
                }
            }
        )
    }



    ExposedDropdownMenuBox(
        expanded = true, onExpandedChange = {
            expanded = !expanded
        }, modifier = Modifier.fillMaxWidth()
    ) {

        Button(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, color = Color.Black),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            onClick = { /*TODO*/ }) {
            Text(selectedOptionText)
        }

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { optionText ->
                DropdownMenuItem(onClick = {
                    selectedOptionText = optionText
                    viewModel.onIdChange(optionText)
                    expanded = false

                    // Fetching controller lists
                    cs.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                val data = ServerHandler().getListsForController(selectedOptionText)
                                fetchedData = data // Assign fetched data to the variable
                            }
                            isDialogVisible = true // Show the dialog
                        } catch (e: Exception) {
                            println("Error occurred: ${e.message}")
                            Toast.makeText(context, "Не удалось получить ведомости", Toast.LENGTH_LONG).show()
                        }
                    }

                }) {
                    Text(text = optionText)
                }
            }
        }
        if (isDialogVisible && fetchedData.isNotEmpty()) {
            ShowModalDialog() // Show the modal dialog with fetched data
        }
    }
}

@Composable
fun FileBtn(
    title: String,
    viewModel: MainViewModel,
    onClick: KFunction1<String, Unit>,
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
            } catch (ex: EmptyFileException) {
                Toast.makeText(context, "Пустой файл!", Toast.LENGTH_SHORT).show()
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, "Нет файла!", Toast.LENGTH_SHORT).show()
                viewModel.onPositionChange(-1)
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
    val lastPosition = viewModel.position.observeAsState(-1)
    val selected = id == lastPosition.value
    val fid = viewModel.fileId.observeAsState(0).value
    val stateId = viewModel.stateId.observeAsState("0").value

    val sourceOption = viewModel.sourceOption.value?.id
    val filename =
        if (sourceOption == 0) "storage/emulated/0/download/control${fid}.xls" else "storage/emulated/0/download/control-${fid}-${stateId}.json" // TODO:- check files

    Card(

        modifier = Modifier
            .clickable(onClick = {
                viewModel.onPositionChange(id)

                val intent = Intent(context, RecordActivity::class.java)

                intent.putExtra("filename", filename)

                intent.putExtra("position", id)
                intent.putExtra(
                    "lastDate",
                    IOUtils().convertDateToFormattedString(record.lastKoDate)
                )
                intent.putExtra("sourceOption", sourceOption.toString())
                val gson = Gson()
                intent.putExtra("recordData", gson.toJson(record))
                context.startActivity(intent)
            }),
        backgroundColor = if (selected) Color(0xFFEEECEC) else Color.White,
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.street.split(" ")[0],
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight(300)
                )
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
                    Text(
                        modifier = Modifier, text = "д: ",
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(300)
                    )
                    Text(
                        modifier = Modifier, text = record.houseNumber.split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier, text = "кв: ",
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
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_light_mode_24),
                        contentDescription = ""
                    )
                    val color = Color(46, 133, 64, 255)
                    var fieldValue = record.ko_D
                    Text(
                        text = fieldValue.toString().split(".")[0],
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(600),
                        color = if (fieldValue > 0) color else Color.Black
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.baseline_dark_mode_24),
                        contentDescription = ""
                    )
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
    MainScreen(
        workBookHandler = WorkBookHandler(),
        serverHandler = ServerHandler(),
        viewModel = MainViewModel()
    )
}

