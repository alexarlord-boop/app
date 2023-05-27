package com.example.app


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.time.LocalDateTime

var FILE_NAME = ""
var DATA_MODE = MainViewModel.DataMode.SERVER
var LAST_LIST_POSITION = -1

class MainActivityScreen : AppCompatActivity() {
    lateinit var area: TextView
    lateinit var fioHeader: TextView
//    var workbookHandler = WorkBookHandler()
    var serverHandler = ServerHandler()
    var fsHandler = FileSystemHandler()
    var viewModel: MainViewModel = MainViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)


        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            setContent {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController, true, serverHandler, viewModel)
//                MainScreen(connected = true, serverHandler, viewModel)
            }
        } else {
            // Show a dialog or handle the case when there is no network connectivity
            // Setting up filesystem handler instead of server handler, to load data from downloaded files
            Toast.makeText(this, "Нет подключения к сети.", Toast.LENGTH_LONG).show()
            DATA_MODE = MainViewModel.DataMode.FILE
            setContent {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController, false, fsHandler, viewModel)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (DATA_MODE.id) {
            0 -> {
                try {
                    fsHandler.reloadRecordsFromFile( viewModel.fileId.value.toString(),
                        viewModel.stateId.value.toString(),
                        this
                    )
                } catch (e: Exception) { Log.w("LIFECYCLE", e.message.toString())}
            }
            1 -> {
                try {
                    serverHandler.reloadRecordsFromFile(
                        viewModel.fileId.value.toString(),
                        viewModel.stateId.value.toString(),
                        this
                    )
                } catch (e: Exception) { Log.w("LIFECYCLE", e.message.toString())}
            }
        }
    }


}

class MainViewModel : ViewModel() {
    enum class DataMode(val id: Int) {
        FILE(0),
        SERVER(1)
    }

    private val _sourceOption: MutableLiveData<DataMode> = MutableLiveData(DataMode.SERVER)
    val sourceOption: LiveData<DataMode> = _sourceOption

    private val _fileId: MutableLiveData<String> = MutableLiveData("1")
    val fileId: LiveData<String> = _fileId

    private val _stateId: MutableLiveData<String> = MutableLiveData("")
    val stateId: LiveData<String> = _stateId

    private var _position: MutableLiveData<Int> = MutableLiveData(-1)
    var position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        MutableLiveData("storage/emulated/0/download/control1.xls")
    val filename: LiveData<String> = _filename

    private val _controllerId: MutableLiveData<String> = MutableLiveData("0")
    var controllerId: LiveData<String> = _controllerId

    val defaultOption = "Выбрать контролера"
    private val _selectedOptionText: MutableLiveData<String> = MutableLiveData(defaultOption)
    var selectedOptionText: LiveData<String> = _selectedOptionText

    fun onOptionChange(newOption: String) {
        _selectedOptionText.value = newOption
    }


    fun onSourceOptionChange(newSrcOption: DataMode) {
        _sourceOption.value = newSrcOption
        DATA_MODE = newSrcOption
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


@Composable
fun MainScreen(
    connected: Boolean,
    dataHandler: DataHandlerInterface,
    viewModel: MainViewModel
) {
    val records by dataHandler.listOfRecords.observeAsState(emptyList())
    val lastClicked = viewModel.position.observeAsState(LAST_LIST_POSITION)
    val id by viewModel.fileId.observeAsState(1)
    val stateId by viewModel.stateId.observeAsState("0")
    val area by dataHandler.area.observeAsState("Район")

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var sortedListToShow = records.sortedBy { it -> it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt() }
    LaunchedEffect(records) {
        sortedListToShow =
            records.sortedBy { it ->
                it.houseNumber.split("/")[0].filter { it.isDigit() }.toInt()
            }

    }


    val showUpButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showLastButton by remember { derivedStateOf { lastClicked.value > 0 } }
    val showUploadButton by remember { derivedStateOf { records.isNotEmpty() } }
    var isUploadDialogVisible by remember { mutableStateOf(false) }

    @Composable
    fun showUploadDialog() {
        if (!connected) {
            AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
                title = {Text(text ="Выгрузка данных")},
                text = { Text(text = "Нет подключения к серверу. Выгрузка недоступна.") },
                confirmButton = {
                    Button(onClick = { isUploadDialogVisible = false }) {
                        Text(text = "Закрыть")
                    }
                })
        } else {
            AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
                shape = RoundedCornerShape(15.dp),
                title = {
                    Column() {
                        Text(text = "Выгрузка данных", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Ведомость $stateId", fontSize = 15.sp)
                    } },
                text = {
                    Column() {
                        Text(text = "При выгрузке данных, файлы с записями удаляются с устройства.")
                        Text(text = "Вы хотите продолжить?")

                    }
                       },
                confirmButton = {
                    Button(onClick = {
                        isUploadDialogVisible = false
                        val filePath = "storage/emulated/0/download/control-$id-$stateId.json"
                        val json = IOUtils().readJsonFromFile(filePath)
                        coroutineScope.launch {
                            val isSent = (dataHandler as ServerHandler).sendDataToServer(json, filePath, stateId, id.toString(), context)
                            if (isSent) {
                                dataHandler.clearRecordList()
                                viewModel.onOptionChange(viewModel.defaultOption)
                                viewModel.onStateIdChange("")
                                viewModel.onPositionChange(-1)
                            }
                        }
                    }) {
                        Text(text = "Да")
                    }
                },
                dismissButton = {
                    Button(onClick = { isUploadDialogVisible = false }) {
                        Text(text = "Нет")
                    }
                })
        }
    }



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
                    Selector(viewModel, dataHandler)
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = area,
                            fontSize = MaterialTheme.typography.h5.fontSize,
                            fontWeight = FontWeight(200)
                        )

                        if (showUploadButton) {
                            Button(shape = CircleShape,
                                onClick = {
                                    isUploadDialogVisible = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_cloud_upload_24),
                                    contentDescription = "",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                if (isUploadDialogVisible) {
                    showUploadDialog()
                }
            }
        }

        // when record list is updated, it triggers launch -> scroll
        LaunchedEffect(sortedListToShow) {
            if (LAST_LIST_POSITION != -1) {
                listState.animateScrollToItem(index = LAST_LIST_POSITION)
            } else {
                listState.animateScrollToItem(index = 0)
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
            visible = showLastButton || showUpButton,
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
                        Icon(Icons.Default.MoveUp, contentDescription = "Перейти к последней просмотренной записи")
                    }
                }

                AnimatedVisibility(visible = showUpButton) {
                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                        shape = CircleShape,
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 0)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Вернуться к началу списка")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Selector(viewModel: MainViewModel, dataHandler: DataHandlerInterface) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cs = CoroutineScope(Dispatchers.Main)
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionText = viewModel.selectedOptionText.observeAsState(viewModel.defaultOption)
    val selectedStatementId = viewModel.stateId.observeAsState("")
    var fetchedData by remember { mutableStateOf(emptyList<ServerHandler.RecordStatement>()) }
    var isDialogVisible by remember { mutableStateOf(false) }

    val id by viewModel.fileId.observeAsState("1") // TODO:- check the value

    var options by remember { mutableStateOf(listOf("-")) }
    var names by remember { mutableStateOf(listOf("-")) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val controllers = dataHandler.getControllers()
            options = controllers.map { it.Staff_Lnk }
            names = controllers.map { it.Staff_Name }
        }
    }

    // Function to show the modal dialog with fetched data
    @Composable
    fun ShowModalDialog() {
        AlertDialog(
            shape = RoundedCornerShape(15.dp),
            onDismissRequest = { isDialogVisible = false },
            title = { Text(text = "Ведомости") },
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
                                    dataHandler.getRecordsForStatement(
                                        id,
                                        item.listNumber,
                                        context
                                    )
                                    viewModel.onStateIdChange(item.listNumber)
                                    viewModel.onPositionChange(-1)
                                    isDialogVisible = false
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
                    Text(text = "Закрыть")
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
            Text("${selectedOptionText.value} | Ведомость ${selectedStatementId.value}")
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { index, optionText ->
                DropdownMenuItem(onClick = {
                    viewModel.onOptionChange(names[index])
                    viewModel.onIdChange(optionText)
                    expanded = false

                    // Fetching controller lists
                    cs.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                val data = dataHandler.getStatementsForController(optionText)
                                fetchedData = data // Assign fetched data to the variable
                            }
                            isDialogVisible = true // Show the dialog
                        } catch (e: Exception) {
                            println("Error occurred: ${e.message}")
                            Toast.makeText(
                                context,
                                "Не удалось получить ведомости",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }) {
                    Text(text = names[index])
                }
            }
        }
        if (isDialogVisible && fetchedData.isNotEmpty()) {
            ShowModalDialog() // Show the modal dialog with fetched data
        }
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
                intent.putExtra("dataMode", sourceOption.toString())
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
fun showMainScreen() {
    var fsHandler = FileSystemHandler()
    var viewModel: MainViewModel = MainViewModel()
    MainScreen(connected = true, dataHandler = fsHandler, viewModel = viewModel)
}



@Composable
fun showUploadDialog() {
    val connected = true
    var isUploadDialogVisible = true
    val stateId = 1
    if (!connected) {
        AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
            title = {Text(text ="Выгрузка данных")},
            text = { Text(text = "Нет подключения к серверу. Выгрузка недоступна.") },
            confirmButton = {
                Button(onClick = { isUploadDialogVisible = false }) {
                    Text(text = "Закрыть")
                }
            })
    } else {
        AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
            shape = RoundedCornerShape(15.dp),
            title = {
                Column() {
                    Text(text = "Выгрузка данных", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Ведомость $stateId", fontSize = 15.sp)
                } },
            text = { Text(text = "При выгрузке данных, файлы с записями удаляются с устройства. Вы хотите продолжить?") },
            confirmButton = {
                Button(onClick = { isUploadDialogVisible = false }) {
                    Text(text = "Да")
                }
            },
            dismissButton = {
                Button(onClick = { isUploadDialogVisible = false }) {
                    Text(text = "Нет")
                }
            })
    }
}

@Preview
@Composable
fun showUpload() {
    showUploadDialog()
}