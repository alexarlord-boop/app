package com.example.app


import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.Global.putInt
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.app.data.Branch
import com.example.app.data.DataHandlerInterface
import com.example.app.data.FileSystemHandler
import com.example.app.data.IOUtils
import com.example.app.navigation.Screen
import com.example.app.record.RecordActivity
import com.example.app.record.RecordDto
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File


var DATA_MODE = SavedStateViewModel.DataMode.SERVER
var LAST_LIST_POSITION = -1

class SavedStateViewModelFactory(private val savedStateRegistryOwner: SavedStateRegistryOwner) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return SavedStateViewModel(handle) as T
    }
}

class MainActivityScreen : AppCompatActivity() {
    var serverHandler = ServerHandler()
    var fsHandler = FileSystemHandler()
    val viewModel: SavedStateViewModel by viewModels { SavedStateViewModelFactory(this) }



    fun createDirectoryIfNotExists(directoryPath: String) {
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
            println("Directory created: $directoryPath")
        } else {
            println("Directory already exists: $directoryPath")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsStorage = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE)
        val requestExternalStorage = 1
        val permission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsStorage, requestExternalStorage)
        }
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        var controllerId: String = ""
        var statementId: String = ""

        with(sharedPref) {
            controllerId = getString("controllerId", "") ?: ""
            statementId = getString("statementId", "") ?: ""
        }
        Log.w("ON_CREATE", "Controller id: $controllerId")
        Log.w("ON_CREATE", "Statement id: $statementId")

        createDirectoryIfNotExists(AppStrings.deviceDirectory)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)


        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            setContent {
                val navController = rememberNavController()
                viewModel.onRecordListChange(serverHandler.reloadRecordsFromFile(controllerId, statementId, this))
                SetupNavGraph(navController = navController, true, serverHandler, viewModel, sharedPref)
            }
        } else {
            Toast.makeText(this, "Нет подключения к сети.", Toast.LENGTH_LONG).show()
            DATA_MODE = SavedStateViewModel.DataMode.FILE
            setContent {
                val navController = rememberNavController()
                viewModel.onRecordListChange(fsHandler.reloadRecordsFromFile(controllerId, statementId, this))
                SetupNavGraph(navController = navController, false, fsHandler, viewModel, sharedPref)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val context = this

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        when (DATA_MODE.id) {
            0 -> {
                Log.w("MODE", "FILESYSTEM")
                try {
                    with(sharedPref) {
                        val controllerId = this.getString("controllerId", "") ?: ""
                        val statementId = this.getString("statementId", "") ?: ""
                        viewModel.onRecordListChange(fsHandler.reloadRecordsFromFile(controllerId, statementId, context))
                    }
                }
                catch (e: Exception) {
                    Log.w("ON_RESUME", e.message.toString())
                    return
                }
            }
            1 -> {
                Log.w("MODE", "SERVER")
                try {
                    with(sharedPref) {
                        val controllerId = this.getString("controllerId", "") ?: ""
                        val statementId = this.getString("statementId", "") ?: ""
                        viewModel.onRecordListChange(serverHandler.reloadRecordsFromFile(controllerId, statementId, context))
                    }

                } catch (e: Exception) {
                    Log.w("ON_RESUME", e.message.toString())
                    return
                }
            }
        }
    }


}

class SavedStateViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    enum class DataMode(val id: Int) {
        FILE(0),
        SERVER(1)
    }

    private val _listOfRecords: MutableLiveData<List<RecordDto>> =
        savedStateHandle.getLiveData("listOfRecords")
    val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    fun onRecordListChange(newRecords: List<RecordDto>) {
        Log.w("RECORDS CHANGES", "$newRecords")
        _listOfRecords.value = newRecords
        if (newRecords.isEmpty()) {
            onAreaChange("Район")
        } else {
            onAreaChange(newRecords[0].area)
        }
    }

    private val _area: MutableLiveData<String> =
        savedStateHandle.getLiveData("area")
    val area: LiveData<String> = _area

    fun onAreaChange(newArea: String) {
        _area.value = newArea
    }

    private val _statementId: MutableLiveData<String> =
        savedStateHandle.getLiveData("statementId", "")
    val statementId: LiveData<String> = _statementId

    private val _position: MutableLiveData<Int> =
        savedStateHandle.getLiveData("position", -1)
    val position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        savedStateHandle.getLiveData("filename", AppStrings.deviceDirectory + "control1.xls")
    val filename: LiveData<String> = _filename

    private val _controllerId: MutableLiveData<String> =
        savedStateHandle.getLiveData("controllerId", "0")
    val controllerId: LiveData<String> = _controllerId

    val defaultOption = "Выбрать контролера"
    private val _selectedOptionText: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedOptionText", defaultOption)
    var selectedOptionText: LiveData<String> = _selectedOptionText

    val defaultBranch = ""
    private val _selectedBranch: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedBranch", defaultBranch)
    var selectedBranch: LiveData<String> = _selectedBranch

    val defaultBranchId = ""
    private val _selectedBranchId: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedBranchId", defaultBranchId)
    var selectedBranchId: LiveData<String> = _selectedBranchId

    private val _controllers: MutableLiveData<List<ServerHandler.Controller>> =
        savedStateHandle.getLiveData("controllers", emptyList())
    var controllers: LiveData<List<ServerHandler.Controller>> = _controllers

    private val _selectedControllerName: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedControllerName", "")
    var selectedControllerName: LiveData<String> = _selectedControllerName

    private val _selectedControllerId: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedControllerId", "")
    var selectedControllerId: LiveData<String> = _selectedControllerId

    private val _selectedControllerCompany: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedControllerCompany", "")
    var selectedControllerCompany: LiveData<String> = _selectedControllerCompany

    private val _selectedRecord: MutableLiveData<RecordDto> =
        savedStateHandle.getLiveData("selectedRecord")
    var selectedRecord: LiveData<RecordDto> = _selectedRecord

    private val _loadedStatements: MutableLiveData<List<ServerHandler.RecordStatement>> =
        savedStateHandle.getLiveData("loadedStatements", emptyList())
    var loadedStatements: LiveData<List<ServerHandler.RecordStatement>> = _loadedStatements

    fun onStatementsChange(statements: List<ServerHandler.RecordStatement>) {
        _loadedStatements.value = statements
    }

    fun onRecordChange(newRecord: RecordDto) {
        _selectedRecord.value = newRecord
    }

    fun onControllerNameChange(controllerName: String) {
        _selectedControllerName.value = controllerName
    }

    fun onControllerCompanyChange(controllerCompany: String) {
        _selectedControllerCompany.value = controllerCompany
    }

    fun onControllerIdChange(controllerId: String) {
        _selectedControllerId.value = controllerId
    }

    fun onControllerListChange(controllers: List<ServerHandler.Controller>) {
        _controllers.value = controllers
    }

    fun onOptionChange(newOption: String) {
        _selectedOptionText.value = newOption
    }

    fun onBranchNameChange(branchName: String) {
        _selectedBranch.value = branchName
    }

    fun onBranchIdChange(newBranchId: String) {
        _selectedBranchId.value = newBranchId
    }

    fun onPositionChange(newPosition: Int) {
        _position.value = newPosition
        LAST_LIST_POSITION = newPosition
    }

    fun onStatementIdChange(newId: String) {
        _statementId.value = newId
    }
}


@Composable
fun MainScreen(
    connected: Boolean,
    dataHandler: DataHandlerInterface,
    viewModel: SavedStateViewModel,
    navController: NavHostController,
    sharedPreferences: SharedPreferences
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val records by viewModel.listOfRecords.observeAsState()
    val selectedControllerId by viewModel.selectedControllerId.observeAsState()
    val lastClicked = viewModel.position.observeAsState(LAST_LIST_POSITION)
    val statementId by viewModel.statementId.observeAsState("0")
    val area by viewModel.area.observeAsState("Район")


    Log.w("MAIN SCREEN", "Records: $records")

    var sortedListToShow =
        records?.sortedBy { record ->
            val houseNumber = record.houseNumber
            val numericPart = houseNumber.split("\\D+".toRegex() )[0].filter { it.isDigit() }
            if (numericPart.isNotEmpty()) {
                numericPart.toInt()
            } else {
                Int.MAX_VALUE
            }
        }

    if ((sortedListToShow ?: emptyList()).isNotEmpty()) {

        var branches = emptyList<Branch>()
        LaunchedEffect(sortedListToShow) {
            coroutineScope.launch {

                withContext(Dispatchers.IO) {
                    val data = dataHandler.getBranchList()
                    branches = data
                }

                withContext(Dispatchers.Main) {
                    with (sharedPreferences) {
                        val branchId = this.getString("branchId", "") ?: ""
                        val branchIdInSelector = this.getInt("branchIdInSelector", 0) ?: 0
                        val controllerId = this.getString("controllerId", "") ?: ""
                        val controllerName = this.getString("controllerName", "") ?: ""
                        val statement = this.getString("statementId", "") ?: ""
                        viewModel.onBranchIdChange(branchId)
                        viewModel.onBranchNameChange(branches[branchIdInSelector].companyName)
                        viewModel.onControllerIdChange(controllerId)
                        viewModel.onControllerNameChange(controllerName)
                        viewModel.onStatementIdChange(statement)

                    }
                }

            }
        }
    }

//    LaunchedEffect(records) {
//        if (records.isEmpty()) {
//            /* preload records if exists */
//            var branches = emptyList<Branch>()
//            coroutineScope.launch {
//                // server request -> branches
//                withContext(Dispatchers.IO) {
//                    val data = dataHandler.getBranchList()
//                    branches = data
//                }
//
//                withContext(Dispatchers.Main) {
//                    with (sharedPreferences) {
//                        val branchId = this.getString("branchId", "") ?: ""
//                        val branchIdInSelector = this.getInt("branchIdInSelector", 0) ?: 0
//                        val controllerId = this.getString("controllerId", "") ?: ""
//                        val controllerName = this.getString("controllerName", "") ?: ""
//                        val statement = this.getString("statementId", "") ?: ""
//                        viewModel.onBranchIdChange(branchId)
//                        viewModel.onBranchNameChange(branches[branchIdInSelector].companyName)
//                        viewModel.onControllerIdChange(controllerId)
//                        viewModel.onControllerNameChange(controllerName)
//                        viewModel.onStatementIdChange(statement)
//
//                    }
//                    dataHandler.onAreaChange(dataHandler.defaultArea)
//                }
//
//            }
//
//
//
//        } else {
//            sortedListToShow =
//                records.sortedBy { record ->
//                    val houseNumber = record.houseNumber
//                    val numericPart = houseNumber.split("\\D+".toRegex())[0].filter { it.isDigit() }
//                    if (numericPart.isNotEmpty()) {
//                        numericPart.toInt()
//                    } else {
//                        Int.MAX_VALUE
//                    }
//                }
//        }
//
//    }


    val showUpButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showLastButton by remember { derivedStateOf { lastClicked.value > 0 } }
    val showUploadButton by remember { derivedStateOf { records?.isNotEmpty() } }
    var isUploadDialogVisible by remember { mutableStateOf(false) }

    @Composable
    fun showUploadDialog() {
        if (!connected) {
            AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
                title = { Text(text = "Выгрузка данных") },
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
                        Text(
                            text = "Выгрузка данных",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Ведомость $statementId", fontSize = 15.sp)
                    }
                },
                text = {
                    Column() {
                        Text(text = "При выгрузке данных, файлы с записями удаляются с устройства.")
                        Text(text = "Вы хотите продолжить?")

                    }
                },
                confirmButton = {
                    Button(onClick = {
                        isUploadDialogVisible = false
                        val id = selectedControllerId
                        val filePath = AppStrings.deviceDirectory + "record-$id-$statementId.json"
                        val json = IOUtils().readJsonFromFile(filePath)
                        coroutineScope.launch {
                            val isSent = (dataHandler as ServerHandler).sendDataToServer(
                                json,
                                filePath,
                                statementId,
                                id.toString(),
                                context
                            )
                            if (isSent) {
                                Log.w("DELETING RECORDS", "after sending")
                                viewModel.onRecordListChange(emptyList())
                                viewModel.onOptionChange(viewModel.defaultOption)
                                viewModel.onStatementIdChange("")
                                with(sharedPreferences.edit()) {
                                    putString("statementId", "")
                                    apply()
                                }
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
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BranchSelector(viewModel, dataHandler, sharedPreferences)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControllerSelector(viewModel, dataHandler, sharedPreferences)
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = area,
                            fontSize = MaterialTheme.typography.h5.fontSize,
                            fontWeight = FontWeight(200)
                        )

                        if (showUploadButton == true) {
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
        LaunchedEffect(records) {
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
            itemsIndexed(sortedListToShow ?: emptyList()) { id, record ->
                RecordItem(id, record, viewModel, dataHandler, navController)
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
                        Icon(
                            Icons.Default.MoveUp,
                            contentDescription = "Перейти к последней просмотренной записи"
                        )
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
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Вернуться к началу списка"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BranchSelector(viewModel: SavedStateViewModel, dataHandler: DataHandlerInterface, sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val selectedBranch = viewModel.selectedBranch.observeAsState(viewModel.defaultBranch)


    var options by remember { mutableStateOf(listOf("филиалы")) } // text names
    var branches by remember { mutableStateOf(emptyList<Branch>()) } // branch objects


    ExposedDropdownMenuBox(
        expanded = true, onExpandedChange = {
            expanded = !expanded
        }, modifier = Modifier.fillMaxWidth()
    ) {

        Button(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, color = Color.Black),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            onClick = {
                coroutineScope.launch {
                    // server request -> branches
                    withContext(Dispatchers.IO) {
                        val data = dataHandler.getBranchList()
                        branches = data
                        options = data.map { it.companyName }
                    }

                }

            }) {
            val header = if (selectedBranch.value != "") selectedBranch.value else  "Филиал"
            Text(header, textAlign = TextAlign.Center)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { index, optionText ->
                DropdownMenuItem(onClick = {
                    viewModel.onControllerNameChange("")
                    with(sharedPreferences.edit()) {
                        putString("controllerName", "")
                        apply()
                    }
                    viewModel.onControllerIdChange("")
                    with(sharedPreferences.edit()) {
                        putString("controllerId", "")
                        apply()
                    }

                    expanded = false

                    Log.w("SELECTOR", "Branch selected: ${branches[index].companyName}")
                    Log.w("SELECTOR", "Branch id: ${branches[index].companyLnk}")
                    viewModel.onBranchNameChange(branches[index].companyName)
                    with(sharedPreferences.edit()) {
                        putString("branchName", branches[index].companyName)
                        apply()
                    }
                    viewModel.onBranchIdChange(branches[index].companyLnk)
                    with(sharedPreferences.edit()) {
                        putString("branchId", branches[index].companyLnk)
                        putInt("branchIdInSelector", index)
                        apply()
                    }

                    Log.w("DELETING RECORDS", "after branch selection")
                    viewModel.onRecordListChange(emptyList())

                }) {
                    Text(text = optionText)
                }
            }
        }

//        if (isDialogVisible && fetchedData.isNotEmpty()) {
//            viewModel.onStateIdChange("")
//            ShowModalDialog() // Show the modal dialog with fetched data
//        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ControllerSelector(viewModel: SavedStateViewModel, dataHandler: DataHandlerInterface, sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    val cs = CoroutineScope(Dispatchers.Main)
    var expanded by remember { mutableStateOf(false) }
    val selectedStatementId = viewModel.statementId.observeAsState("0")
    val selectedControllerName = viewModel.selectedControllerName.observeAsState()
    val selectedControllerId = viewModel.selectedControllerId.observeAsState()
    val selectedControllerCompany = viewModel.selectedControllerCompany.observeAsState()
    var statements = viewModel.loadedStatements.observeAsState(emptyList())
    var isDialogVisible by remember { mutableStateOf(false) }
    var isInfoVisible by remember { mutableStateOf(false) }

    val controllers = viewModel.controllers.observeAsState(listOf(ServerHandler.Controller("-","-", "-")))
    val selectedBranch = viewModel.selectedBranch.observeAsState("")
    val selectedBranchId = viewModel.selectedBranchId.observeAsState("")




    // Function to show the modal dialog with fetched data
    @Composable
    fun ShowModalDialog() {
        Log.w("DATA", statements.toString())
        AlertDialog(
            shape = RoundedCornerShape(15.dp),
            onDismissRequest = { isDialogVisible = false },
            title = { Text(text = "Ведомости") },
            text = {
                Column {
                    statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
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
                                    viewModel.onStatementIdChange(item.listNumber)
                                    with(sharedPreferences.edit()) {
                                        putString("statementId", item.listNumber)
                                        apply()
                                    }

                                    cs.launch {
                                        try {
                                            val data: List<RecordDto>? = selectedControllerId.value?.let { controllerId ->
                                                withContext(Dispatchers.IO) {
                                                    dataHandler.getRecordsForStatement(controllerId, item.listNumber, context)
                                                }
                                            }
                                            data?.let {
                                                viewModel.onRecordListChange(it)
                                            }
                                        } catch (e: Exception) {
                                            println(e.stackTraceToString())
                                        }
                                    }



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

    // Function to show the modal dialog with fetched data
    @Composable
    fun ShowInfoDialog() {
        viewModel.onStatementIdChange("")
        with(sharedPreferences.edit()) {
            putString("statementId", "")
            apply()
        }
        AlertDialog(
            shape = RoundedCornerShape(15.dp),
            onDismissRequest = { isInfoVisible = false },
            title = { Text(text = "Нет ведомостей") },
            text = { Text(text = "Для контролера не найдены ведомости.") },
            confirmButton = {
                Button(onClick = { isInfoVisible = false }) {
                    Text(text = "ОК")
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
            onClick = {
                // Fetching controller lists

                cs.launch {
                    withContext(Dispatchers.IO) {

                        val fetchedControllers =
                            dataHandler.getControllersForBranch(viewModel.selectedBranchId.value ?: "0")

                        withContext(Dispatchers.Main) {
                            // Perform UI-related operations here
                            if (fetchedControllers.isEmpty()) {
                                Toast.makeText(context,"Контролеры не найдены", Toast.LENGTH_SHORT).show()
                                viewModel.onStatementIdChange("")
                                with(sharedPreferences.edit()) {
                                    putString("statementId", "")
                                    apply()
                                }
                            }
                            viewModel.onControllerListChange(fetchedControllers)


                        }
                    }

                }

            }) {
            var header = "Контролер | Ведомость"
            if (selectedControllerName.value != "") {
                header = "${selectedControllerName.value} | Ведомость ${selectedStatementId.value}"
            }
            Text(header)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (controllers.value.isNotEmpty()) {
                controllers.value.forEachIndexed { index, element ->
                    DropdownMenuItem(onClick = {
                        println(selectedControllerName.value)
                        println(element.Staff_Name)
                        if (!selectedControllerName.value.equals(element.Staff_Name)) {
                            viewModel.onRecordListChange(emptyList())
                            viewModel.onStatementIdChange("")
                        }

                        viewModel.onControllerNameChange(element.Staff_Name)
                        with(sharedPreferences.edit()) {
                            putString("controllerName", element.Staff_Name)
                            apply()
                        }
                        viewModel.onControllerIdChange(element.Staff_Lnk)
                        with(sharedPreferences.edit()) {
                            putString("controllerId", element.Staff_Lnk)
                            apply()
                        }
                        viewModel.onControllerNameChange(element.Staff_Name)
                        with(sharedPreferences.edit()) {
                            putString("controllerName", element.Staff_Name)
                            apply()
                        }
                        viewModel.onControllerCompanyChange(element.Company_Lnk)
                        with(sharedPreferences.edit()) {
                            putString("controllerCompany", element.Company_Lnk)
                            apply()
                        }
                        Log.w("SELECTOR", "Controller selected: ${element.Staff_Name}")
                        Log.w("SELECTOR", "Controller id: ${element.Staff_Lnk}")
                        expanded = false



                        // Fetching controller statements
                        cs.launch {
                            try {
                                var data: MutableList<ServerHandler.RecordStatement>
                                withContext(Dispatchers.IO) {
                                    data = dataHandler.getStatementsForController(element.Staff_Lnk, selectedBranchId.value).toMutableList()
                                }
                                viewModel.onStatementsChange(data)
                                if (statements.value.isNotEmpty()) {
                                    isDialogVisible = true // Show the dialog
                                } else {
                                    isInfoVisible = true
                                }
                            } catch (e: Exception) {
                                viewModel.onStatementIdChange("")
                                with(sharedPreferences.edit()) {
                                    putString("statementId", "")
                                    apply()
                                }
                                viewModel.onRecordListChange(emptyList())
                                println("Error occurred: ${e.message}")
                                Toast.makeText(
                                    context,
                                    "Данные не были загружены",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    }) {
                        Text(text = element.Staff_Name)
                    }
                }
            } else {
                DropdownMenuItem(onClick = {
                    expanded = false
                }) {
                    val text = if (selectedBranch.value != "") "нет контролеров" else "выберите филиал"
                    Text(text = text)
                }
            }
        }

        if (isDialogVisible && statements.value.isNotEmpty()) {
//            viewModel.onStateIdChange("")
            ShowModalDialog() // Show the modal dialog with fetched data
        } else if(statements.value.isEmpty()) {
            Log.w("DELETING RECORDS", "after modal dialog")
//            viewModel.onRecordListChange(emptyList())
//            viewModel.onAreaChange("") // TODO
        }
        if (isInfoVisible) {
            ShowInfoDialog()
        }
    }
}


@Composable
fun RecordItem(id: Int, record: RecordDto, viewModel: SavedStateViewModel, dataHandler: DataHandlerInterface, navController: NavHostController) {
    val padding = 5.dp
    val margin = 10.dp
    val context = LocalContext.current
    val lastPosition = viewModel.position.observeAsState(-1)
    val selected = id == lastPosition.value
    val controllerId = viewModel.selectedControllerId.observeAsState()
    val statementId = viewModel.statementId.observeAsState("0").value

    val filename = AppStrings.deviceDirectory + "record-${controllerId}-${statementId}.json"


    val onClick = {
        viewModel.onPositionChange(id)
        viewModel.onRecordChange(record)
        navController.navigate(route = Screen.Record.route)
    }

    Card(

        modifier = Modifier
            .clickable(onClick = onClick),
        backgroundColor = if (selected) Color(0xFFEEECEC) else Color.White,
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(2f)) {
                    Text(
                        text = record.street,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(300)
                    )
                }
                Column(Modifier.weight(2f)) {
                    Text(
                        text = record.name,
                        Modifier.fillMaxWidth(),
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.End
                    )
                }
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




@Composable
fun showUploadDialog() {
    val connected = true
    var isUploadDialogVisible = true
    val stateId = 1
    if (!connected) {
        AlertDialog(onDismissRequest = { isUploadDialogVisible = false },
            title = { Text(text = "Выгрузка данных") },
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
                }
            },
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

//@Preview
//@Composable
//fun showUpload() {
//    showUploadDialog()
//}
