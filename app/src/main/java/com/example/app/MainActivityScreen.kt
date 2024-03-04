package com.example.app


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.app.Components.StatementDialog
import com.example.app.data.*
import com.example.app.navigation.Screen
import com.example.app.record.RecordDto
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime


var DATA_MODE = SavedStateViewModel.DataMode.SERVER
var LAST_LIST_POSITION = -1

class SavedStateViewModelFactory(private val savedStateRegistryOwner: SavedStateRegistryOwner) :
    AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SavedStateViewModel(handle) as T
    }
}

class MainActivityScreen : AppCompatActivity() {

    private val fsHandler = FileSystemHandler()
    private lateinit var serverHandler: DataHandlerInterface
    private val viewModel: SavedStateViewModel by viewModels { SavedStateViewModelFactory(this) }

    private fun createDirectoryIfNotExists(directoryPath: String) {
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
            println("Directory created: $directoryPath")
        } else {
            println("Directory already exists: $directoryPath")
        }
    }

    private fun checkStoragePermission() {
        val permissionsStorage = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        val requestExternalStorage = 1
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsStorage, requestExternalStorage)
        }
    }

    private fun handleInternetConnection() {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connectivityManager.activeNetworkInfo // deprecated

        if (isInternetAvailable(context = this)) {
            handleOnlineMode()
        } else {
            handleNoInternet()
        }
    }

    private fun setContentWithNavController(isServerMode: Boolean) {
        setContent {
            val navController = rememberNavController()
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val dataHandler = if (isServerMode) serverHandler else fsHandler

            SetupNavGraph(
                navController = navController,
                connected = isServerMode,
                dataHandler = dataHandler,
                viewModel = viewModel,
                sharedPreferences = sharedPref
            )
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        connectivityManager?.let {
            val network = it.activeNetwork
            val capabilities = it.getNetworkCapabilities(network)

            // Check if the device has a network connection and internet is available
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }

        return false
    }

    private fun handleNoInternet() {
        Toast.makeText(this, AppUIResponses.noInternetConnection, Toast.LENGTH_LONG).show()
        handleOfflineMode()
    }

    private fun handleNoNetwork() {
        Toast.makeText(this, AppUIResponses.noNetworkAvailable, Toast.LENGTH_LONG).show()
        handleOfflineMode()
    }

    private fun handleOnlineMode() {
        Toast.makeText(this, AppUIResponses.internetConnection, Toast.LENGTH_LONG).show()
        setContentWithNavController(true)
    }

    private fun handleOfflineMode() {
        DATA_MODE = SavedStateViewModel.DataMode.FILE
        setContentWithNavController(false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverHandler = ServerHandler(viewModel, DefaultServerHandlerDelegate(this))

        checkStoragePermission()
        createDirectoryIfNotExists(AppStrings.deviceDirectory)

        handleInternetConnection()
    }

    override fun onResume() {
        super.onResume()
        when (DATA_MODE.id) {
            0 -> handleOfflineMode()
            1 -> setContentWithNavController(true)
        }
    }
}

class SavedStateViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

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

    private fun onAreaChange(newArea: String) {
        _area.value = newArea
    }

    private val _statementId: MutableLiveData<String> =
        savedStateHandle.getLiveData("statementId", "")
    val statementId: LiveData<String> = _statementId

    private val _position: MutableLiveData<Int> =
        savedStateHandle.getLiveData("position", -1)
    val position: LiveData<Int> = _position

    private val _filename: MutableLiveData<String> =
        savedStateHandle.getLiveData("filename", "")
    val filename: LiveData<String> = _filename


    val defaultOption = "Выбрать контролера"
    private val _selectedOptionText: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedOptionText", defaultOption)
    var selectedOptionText: LiveData<String> = _selectedOptionText

    val defaultBranch = Branch("", "")
    private val _selectedBranch: MutableLiveData<Branch> =
        savedStateHandle.getLiveData("selectedBranch", defaultBranch)
    var selectedBranch: LiveData<Branch> = _selectedBranch



    private val defaultBranchId = ""
    private val _selectedBranchId: MutableLiveData<String> =
        savedStateHandle.getLiveData("selectedBranchId", defaultBranchId)
    var selectedBranchId: LiveData<String> = _selectedBranchId

    private val _controllers: MutableLiveData<List<Controller>> =
        savedStateHandle.getLiveData("controllers", emptyList())
    var controllers: LiveData<List<Controller>> = _controllers

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

    private val _loadedStatements: MutableLiveData<List<RecordStatement>> =
        savedStateHandle.getLiveData("loadedStatements", emptyList())
    var loadedStatements: LiveData<List<RecordStatement>> = _loadedStatements

    fun onFileNameChange(filename: String) {
        _filename.value = filename
    }

    fun onStatementsChange(statements: List<RecordStatement>) {
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

    fun onControllerListChange(controllers: List<Controller>) {
        _controllers.value = controllers
    }

    fun onBranchChange(newBranch: Branch) {
        _selectedBranch.value = newBranch
    }

    fun onOptionChange(newOption: String) {
        _selectedOptionText.value = newOption
    }

//    fun onBranchNameChange(branchName: String) {
//        _selectedBranch.value = branchName
//    }

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


//@Composable
//fun SetMainScreen() {
//    MainScreen(
//        connected = true,
//        records = records,
//        selectedControllerId = "1",
//        lastClicked = 1,
//        statementId = "statementID",
//        area = "Великий Новгород длинное название",
//        filename = "some file name"
//    )
//}


//@Composable
//fun MainScreen(
//    connected: Boolean,
//    dataHandler: DataHandlerInterface,
//    viewModel: SavedStateViewModel,
//    navController: NavHostController,
//    sharedPreferences: SharedPreferences
//) {
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    val records by viewModel.listOfRecords.observeAsState()
//    val selectedControllerId by viewModel.selectedControllerId.observeAsState()
//    val lastClicked = viewModel.position.observeAsState(LAST_LIST_POSITION)
//    val statementId by viewModel.statementId.observeAsState("0")
//    val area by viewModel.area.observeAsState("Район")
//    val filename by viewModel.filename.observeAsState("")
//    val showDeleteBtn by remember { derivedStateOf { (records != null && records!!.isNotEmpty()) } }
//
//
//    Log.w("MAIN SCREEN", "Records: $records")
//
//    val sortedListToShow: List<RecordDto> = if (records !== null) {
//        sortRecordsByHouseNumber(records!!) { it.houseNumber }
//    } else {
//        emptyList()
//    }
//
//
//
//    if ((sortedListToShow).isNotEmpty()) {
//
//        var branches: List<Branch>
//        LaunchedEffect(sortedListToShow) {
//            coroutineScope.launch {
//
//                withContext(Dispatchers.IO) {
//                    val data = dataHandler.getBranchList()
//                    branches = data
//                }
//
//                withContext(Dispatchers.Main) {
//                    with(sharedPreferences) {
//                        val branchId = this.getString("branchId", "") ?: ""
//                        val branchIdInSelector = this.getInt("branchIdInSelector", 0)
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
//                }
//
//            }
//        }
//    }
//
//
//    val showUpButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
//    val showLastButton by remember { derivedStateOf { lastClicked.value > 0 } }
//    val showUploadButton by remember { derivedStateOf { records?.isNotEmpty() } }
//    var isUploadDialogVisible by remember { mutableStateOf(false) }
//    var isDeleteDialogVisible by remember { mutableStateOf(false) }
//
//    @Composable
//    fun showUploadDialog() {
//        if (!connected) {
//            //TODO check buttons in all cases
//            UploadDialog(
//                dialogStrings = DisconnectedUploadDialogStrings(),
//                onDismissRequest = { isUploadDialogVisible = false },
//                onConfirm = null
//            )
//        } else {
//            val currentDialogStrings = ConnectedUploadDialogStrings().apply { title += ". Ведомость $statementId" }
//
//            UploadDialog(dialogStrings = currentDialogStrings,
//                onDismissRequest = { isUploadDialogVisible = false },
//                onConfirm = {
//                    isUploadDialogVisible = false
//                    val id = selectedControllerId
//
//                    val json = IOUtils().readJsonFromFile(filename)
//                    coroutineScope.launch {
//                        val isSent = (dataHandler as ServerHandler).sendDataToServer(
//                            json,
//                            filename,
//                            statementId,
//                            id.toString()
//                        )
//                        if (isSent) {
//                            Log.w("DELETING RECORDS", "after sending")
//                            viewModel.onRecordListChange(emptyList())
//                            viewModel.onOptionChange(viewModel.defaultOption)
//                            viewModel.onStatementIdChange("")
//                            with(sharedPreferences.edit()) {
//                                putString("statementId", "")
//                                apply()
//                            }
//                            viewModel.onPositionChange(-1)
//                        }
//                    }
//                },
//
//            )
//
//        }
//    }
//
//    @Composable
//    fun showDeleteDialog() {
//        AlertDialog(onDismissRequest = { isDeleteDialogVisible = false },
//            shape = RoundedCornerShape(15.dp),
//            title = {
//                Column {
//                    Text(
//                        text = "Удаление ведомости",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
////                        Text(text = "Ведомость $statementId", fontSize = 15.sp)
//                }
//            },
//            text = {
//                Column {
//                    Text(text = "При удалении вы теряете файл и все изменения в нем.")
//                    Text(text = "Продолжить?")
//                }
//            },
//            confirmButton = {
//                Button(onClick = {
//                    val isDelete = IOUtils().deleteFile(filename)
//                    if (isDelete) {
//                        Log.w("FILESYSTEM", "Deleted $filename")
//                        viewModel.onRecordListChange(emptyList())
//                        viewModel.onFileNameChange("")
//                        viewModel.onStatementIdChange("")
//                    }
//                    isDeleteDialogVisible = false
//                }) {
//                    Text(text = "Да")
//                }
//            },
//            dismissButton = {
//                Button(onClick = { isDeleteDialogVisible = false }) {
//                    Text(text = "Нет")
//                }
//            })
//
//    }
//
//
//
//    Column {
//
//        Surface(
//            modifier = Modifier
//                .height(200.dp)
//                .shadow(5.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .weight(2F)
//                    .fillMaxWidth()
//                    .padding(10.dp)
//            ) {
//                Spacer(modifier = Modifier.height(20.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    BranchSelector(viewModel, dataHandler, sharedPreferences)
//                }
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    ControllerSelector(viewModel, dataHandler, sharedPreferences)
//                }
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .fillMaxWidth(),
//                    verticalArrangement = Arrangement.Bottom
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            text = area,
//                            fontSize = MaterialTheme.typography.h5.fontSize,
//                            fontWeight = FontWeight(200)
//                        )
//
//                        if (showUploadButton == true) {
//                            Button(shape = CircleShape,
//                                onClick = {
//                                    isUploadDialogVisible = true
//                                }
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.baseline_cloud_upload_24),
//                                    contentDescription = "",
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
//                        if (showDeleteBtn) {
//                            Button(
//                                shape = CircleShape,
//                                onClick = {
//                                    isDeleteDialogVisible = true
//                                })
//                            {
//
//                                Icon(
//                                    Icons.Default.Delete,
//                                    contentDescription = "Delete record file",
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
//
//                    }
//                }
//                if (isUploadDialogVisible) {
//                    showUploadDialog()
//                }
//                if (isDeleteDialogVisible) {
//                    showDeleteDialog()
//                }
//            }
//        }
//
//        // when record list is updated, it triggers launch -> scroll
//        LaunchedEffect(sortedListToShow) {
//            println("Scroll to: ${viewModel.position.value}")
//            println("Scroll to: ${LAST_LIST_POSITION}")
//            if (LAST_LIST_POSITION != -1) {
//                listState.animateScrollToItem(index = LAST_LIST_POSITION)
//            } else {
//                listState.animateScrollToItem(index = 0)
//            }
//        }
//
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .weight(10F)
//                .padding(10.dp)
//        ) {
//            itemsIndexed(sortedListToShow) { id, record ->
//                RecordItem(id, record, viewModel, dataHandler, navController, sharedPreferences)
//            }
//        }
//
//
//        AnimatedVisibility(
//            visible = showLastButton || showUpButton,
//            enter = fadeIn(),
//            exit = fadeOut(),
//        ) {
//
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//                AnimatedVisibility(
//                    visible = showLastButton, enter = fadeIn(),
//                    exit = fadeOut(),
//                ) {
//                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
//                        shape = CircleShape,
//                        onClick = {
//                            coroutineScope.launch {
//                                listState.animateScrollToItem(index = LAST_LIST_POSITION)
//                            }
//                        }
//                    ) {
//                        Icon(
//                            Icons.Default.MoveUp,
//                            contentDescription = "Перейти к последней просмотренной записи"
//                        )
//                    }
//                }
//
//                AnimatedVisibility(visible = showUpButton) {
//                    Button(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
//                        shape = CircleShape,
//                        onClick = {
//                            coroutineScope.launch {
//                                listState.animateScrollToItem(index = 0)
//                            }
//                        }
//                    ) {
//                        Icon(
//                            Icons.Default.ArrowUpward,
//                            contentDescription = "Вернуться к началу списка"
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun BranchSelector(
//    viewModel: SavedStateViewModel,
//    dataHandler: DataHandlerInterface,
//    sharedPreferences: SharedPreferences
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    var expanded by remember { mutableStateOf(false) }
//    val selectedBranch = viewModel.selectedBranch.observeAsState(viewModel.defaultBranch)
//
//
//    var options by remember { mutableStateOf(listOf("филиалы")) } // text names
//    var branches by remember { mutableStateOf(emptyList<Branch>()) } // branch objects
//
//
//    ExposedDropdownMenuBox(
//        expanded = true, onExpandedChange = {
//            expanded = !expanded
//        }, modifier = Modifier.fillMaxWidth()
//    ) {
//
//        Button(
//            modifier = Modifier.fillMaxWidth(),
//            border = BorderStroke(1.dp, color = Color.Black),
//            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
//            onClick = {
//                coroutineScope.launch {
//                    // server request -> branches
//                    withContext(Dispatchers.IO) {
//                        val data = dataHandler.getBranchList()
//                        branches = data
//                        options = data.map { it.companyName }
//                    }
//
//                }
//
//            }) {
//            val header = if (selectedBranch.value != "") selectedBranch.value else "Филиал"
//            Text(header, textAlign = TextAlign.Center)
//        }
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            options.forEachIndexed { index, optionText ->
//                DropdownMenuItem(onClick = {
//                    viewModel.onControllerNameChange("")
//                    with(sharedPreferences.edit()) {
//                        putString("controllerName", "")
//                        apply()
//                    }
//                    viewModel.onControllerIdChange("")
//                    with(sharedPreferences.edit()) {
//                        putString("controllerId", "")
//                        apply()
//                    }
//
//                    expanded = false
//
//                    Log.w("SELECTOR", "Branch selected: ${branches[index].companyName}")
//                    Log.w("SELECTOR", "Branch id: ${branches[index].companyLnk}")
//                    viewModel.onBranchNameChange(branches[index].companyName)
//                    with(sharedPreferences.edit()) {
//                        putString("branchName", branches[index].companyName)
//                        apply()
//                    }
//                    viewModel.onBranchIdChange(branches[index].companyLnk)
//                    with(sharedPreferences.edit()) {
//                        putString("branchId", branches[index].companyLnk)
//                        putInt("branchIdInSelector", index)
//                        apply()
//                    }
//
//                    Log.w("DELETING RECORDS", "after branch selection")
//                    viewModel.onRecordListChange(emptyList())
//
//                }) {
//                    Text(text = optionText)
//                }
//            }
//        }
//
////        if (isDialogVisible && fetchedData.isNotEmpty()) {
////            viewModel.onStateIdChange("")
////            ShowModalDialog() // Show the modal dialog with fetched data
////        }
//    }
//}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ControllerSelector(
    viewModel: SavedStateViewModel,
    dataHandler: DataHandlerInterface,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current
    val cs = CoroutineScope(Dispatchers.Main)
    var expanded by remember { mutableStateOf(false) }
    val selectedStatementId = viewModel.statementId.observeAsState("0")
    val selectedControllerName = viewModel.selectedControllerName.observeAsState()
    val selectedControllerId = viewModel.selectedControllerId.observeAsState()
    val selectedControllerCompany = viewModel.selectedControllerCompany.observeAsState()
    val controllerId = viewModel.selectedControllerId.observeAsState().value
    val statementId = viewModel.statementId.observeAsState("0").value

    val statements = viewModel.loadedStatements.observeAsState(emptyList())
    var isDialogVisible by remember { mutableStateOf(false) }
    var isInfoVisible by remember { mutableStateOf(false) }

    LaunchedEffect(statementId) {
        val filename = AppStrings.deviceDirectory + "record-${controllerId}-${statementId}.json"
        viewModel.onFileNameChange(filename)
    }

    val controllers =
        viewModel.controllers.observeAsState(listOf(Controller("-", "-", "-")))
    val selectedBranch = viewModel.selectedBranch.observeAsState("")
    val selectedBranchId = viewModel.selectedBranchId.observeAsState("")


    // Function to show the modal dialog with all controller's lists
    @Composable
    fun ShowStatementsDialog() {
        Log.w("DATA", statements.toString())
        StatementDialog(
            statements = statements,
            isDialogVisible = true,
            onDismiss = { isDialogVisible = false },
            onStatementSelected = {
                viewModel.onStatementIdChange(it)
                with(sharedPreferences.edit()) {
                    putString("statementId", it)
                    apply()
                }

                cs.launch {
                    try {
                        selectedControllerId.value?.let { controllerId ->
                            withContext(Dispatchers.IO) {
                                val data: List<RecordDto> =
                                    dataHandler.getRecordsForStatement(
                                        controllerId,
                                        it,
                                        context
                                    )
                                withContext(Dispatchers.Main) {
                                    viewModel.onRecordListChange(data)
                                }
                            }
                        }


                    } catch (e: Exception) {
                        println(e.stackTraceToString())
                    }
                }

                viewModel.onPositionChange(-1)
                isDialogVisible = false
            }
        )


//        AlertDialog(
//            shape = RoundedCornerShape(15.dp),
//            onDismissRequest = { isDialogVisible = false },
//            //onDismissRequest = {  },
//            title = { Text(text = "Ведомости") },
//            text = {
//                Column {
//                statements.value.sortedBy { it.listNumber.toInt() }.forEach { item ->
//
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 10.dp)
//                                .border(
//                                    2.dp,
//                                    color = Color.LightGray,
//                                    shape = RoundedCornerShape(10.dp)
//                                ),
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 0.dp)
//                                    .height(55.dp),
//                                horizontalArrangement = Arrangement.Start,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = item.listDate,
//                                    fontSize = MaterialTheme.typography.h6.fontSize,
//                                    fontWeight = FontWeight(300),
//                                    modifier = Modifier.padding(horizontal = 10.dp)
//                                )
//
//                                Button(
//                                    onClick = {
//                                    viewModel.onStatementIdChange(item.listNumber)
//                                    with(sharedPreferences.edit()) {
//                                        putString("statementId", item.listNumber)
//                                        apply()
//                                    }
//
//                                    cs.launch {
//                                        try {
//                                            selectedControllerId.value?.let { controllerId ->
//                                                withContext(Dispatchers.IO) {
//                                                    val data: List<RecordDto> =
//                                                        dataHandler.getRecordsForStatement(
//                                                            controllerId,
//                                                            item.listNumber,
//                                                            context
//                                                        )
//                                                    withContext(Dispatchers.Main) {
//                                                        viewModel.onRecordListChange(data)
//                                                    }
//                                                }
//                                            }
//
//
//                                        } catch (e: Exception) {
//                                            println(e.stackTraceToString())
//                                        }
//                                    }
//
//                                    viewModel.onPositionChange(-1)
//                                    isDialogVisible = false
//                                    },
//                                    contentPadding = PaddingValues(),
//                                    modifier = Modifier
//                                        .width(200.dp)
//                                ) {
//                                    Text(text = item.listNumber, fontSize = MaterialTheme.typography.h6.fontSize)
//                                }
//                            }
//                            Text(
//                                text = item.firstAddress?.split(", ")?.getOrNull(0) ?: "N/A",
//                                fontSize = MaterialTheme.typography.h6.fontSize,
//                                fontWeight = FontWeight(300),
//                                modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 5.dp)
//                            )
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//            Button(onClick = { isDialogVisible = false }) {
//                //Button(onClick = {  }) {
//                    Text(text = "Закрыть")
//                }
//            }
//        )
    }


    // Function to show the warning dialog (empty list)
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
                            dataHandler.getControllersForBranch(
                                viewModel.selectedBranchId.value ?: "0"
                            )

                        withContext(Dispatchers.Main) {
                            // Perform UI-related operations here
                            if (fetchedControllers.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Контролеры не найдены",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
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
                header =
                    "${selectedControllerName.value} | Ведомость ${selectedStatementId.value}"
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
                                var data: MutableList<RecordStatement>
                                withContext(Dispatchers.IO) {
                                    data = dataHandler.getStatementsForController(
                                        element.Staff_Lnk,
                                        selectedBranchId.value
                                    ).toMutableList()
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
                    val text =
                        if (selectedBranch.value != "") "нет контролеров" else "выберите филиал"
                    Text(text = text)
                }
            }
        }

        if (isDialogVisible && statements.value.isNotEmpty()) {
//            viewModel.onStateIdChange("")
            ShowStatementsDialog() // Show the modal dialog with fetched data
        } else if (statements.value.isEmpty()) {
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
fun RecordItem(
    id: Int,
    record: RecordDto,
//    viewModel: SavedStateViewModel,
    lastPosition: Int,
    onPositionChange: (Int) -> Unit,
    onRecordChange: (RecordDto) -> Unit,
    navigateToRecord: () -> Unit,
//    sharedPreferences: SharedPreferences
) {
    val padding = 5.dp
    val margin = 10.dp
    val context = LocalContext.current
//    val lastPosition = viewModel.position.observeAsState(-1)
    val selected = id == lastPosition


    val onClick = {
//        viewModel.onPositionChange(id)
        onPositionChange(id)
//        with(sharedPreferences.edit()) {
//            this.putInt("positionCLicked", id)
//            apply()
//        }

//        viewModel.onRecordChange(record)
        onRecordChange(record)
        navigateToRecord()
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
                        // text = record.name,
                        //TODO:- check this changes
                        text = record.puNumber,
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

