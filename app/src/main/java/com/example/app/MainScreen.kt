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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.Components.Selector
import org.mockito.kotlin.mock
import java.time.LocalDateTime


@Composable
fun MainScreen(
    connected: Boolean,
    viewModel: SavedStateViewModel,
    getBranchList: suspend() -> List<Branch>
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val records by viewModel.listOfRecords.observeAsState()
    val selectedBranch by viewModel.selectedBranch.observeAsState()
    val selectedControllerId by viewModel.selectedControllerId.observeAsState()
    val lastClicked by viewModel.position.observeAsState(LAST_LIST_POSITION)
    val statementId by viewModel.statementId.observeAsState("0")
    val area by viewModel.area.observeAsState("Район")
    val filename by viewModel.filename.observeAsState("")
    val showDeleteBtn by remember { derivedStateOf { !records.isNullOrEmpty() } }


    val sortedListToShow: List<RecordDto> = if (records !== null) {
        sortRecordsByHouseNumber(records!!) { it.houseNumber }
    } else {
        emptyList()
    }

    val showUpButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showLastButton by remember { derivedStateOf { lastClicked > 0 } }
    val showUploadButton by remember { derivedStateOf { records?.isNotEmpty() ?: false } }
    var isUploadDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    // HEADER SELECTORS START
    var branches by remember { mutableStateOf(emptyList<Branch>()) } // branch objects

    // HEADER SELECTORS END

    // LOGIC START
    LaunchedEffect(sortedListToShow) {
        coroutineScope.launch {

            withContext(Dispatchers.IO) {
                val data = getBranchList()
                branches = data
            }
        }
    }

    // LOGIC END



    Column(modifier = Modifier.fillMaxHeight()) {
        // Separated composable functions
        MainScreenHeader(
            viewModel = viewModel,
            area = area,
            selectedBranch = selectedBranch,
            branchOptions = branches,
            showUploadButton = showUploadButton,
            showDeleteBtn = showDeleteBtn,
            onUploadButtonClick = { isUploadDialogVisible = true },
            onDeleteButtonClick = { isDeleteDialogVisible = true }
        )

        MainScreenRecordList(
            sortedListToShow = sortedListToShow,
            listState = listState,
//        navigateToRecord = { navController.navigate(route = Screen.Record.route) },
            navigateToRecord = { },
        )
    }
        MainScreenButtons(
            showLastButton = showLastButton,
            showUpButton = showUpButton,
            onLastButtonClick = { coroutineScope.launch { listState.animateScrollToItem(index = LAST_LIST_POSITION) } },
            onUpButtonClick = { coroutineScope.launch { listState.animateScrollToItem(index = 0) } }
        )

}

@Composable
fun MainScreenHeader(
    viewModel: SavedStateViewModel,
    area: String,
    selectedBranch: Branch?,
    branchOptions: List<Branch>,
    showUploadButton: Boolean,
    showDeleteBtn: Boolean,
    onUploadButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(200.dp)
            .shadow(5.dp)
    ) {
        Column(
            modifier = Modifier
//                .weight(2F)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                BranchSelector(viewModel, dataHandler, sharedPreferences)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    Selector<Branch>(
                        selectedValue = selectedBranch,
                        options = branchOptions,
                        getLabel = {it.companyName},
                        onValueSelected = {viewModel.onBranchChange(it)},
                        initialExpanded = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                ) {
                    Selector(
                        selectedValue = "controller",
                        options = listOf("Option 1", "Option 2", "Option 3"),
                        getLabel = {it},
                        onValueSelected = {},
                        initialExpanded = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Selector(
                        selectedValue = "statement",
                        options = listOf("Option A", "Option B", "Option C"),
                        getLabel = {it},
                        onValueSelected = {},
                        initialExpanded = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Area text with horizontal scroll
                    Text(
                        text = area,
                        fontSize = MaterialTheme.typography.h5.fontSize,
                        fontWeight = FontWeight(200),

                        modifier = Modifier
                            .weight(1f) // Takes the available space
                            .horizontalScroll(rememberScrollState())
                    )


                    val iconSize = 20.dp
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 16.dp) // Add padding for better spacing
                            .padding(start = 16.dp)
                    ) {
                        if (showUploadButton == true) {
                            Button(
                                shape = CircleShape,
                                onClick = onUploadButtonClick
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_cloud_upload_24),
                                    contentDescription = "",
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        if (showDeleteBtn) {
                            Button(
                                shape = CircleShape,
                                onClick = onDeleteButtonClick
                            )
                            {

                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete record file",
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MainScreenRecordList(
    sortedListToShow: List<RecordDto>,
    listState: LazyListState,
    navigateToRecord: () -> Unit,
) {
    // Existing record list composable...
    Box(
        modifier = Modifier
//            .weight(10f)
            .padding(10.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(sortedListToShow) { id, record ->
                RecordItem(id, record, 1, {}, {}, navigateToRecord)
            }
        }
    }
}

@Composable
fun MainScreenButtons(
    showLastButton: Boolean,
    showUpButton: Boolean,
    onLastButtonClick: () -> Unit,
    onUpButtonClick: () -> Unit
) {
    // Existing button composable...

    AnimatedVisibility(
        visible = showLastButton || showUpButton,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            AnimatedVisibility(
                visible = showLastButton, enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Button(
                    modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                    shape = CircleShape,
                    onClick = onLastButtonClick
                ) {
                    Icon(
                        Icons.Default.MoveUp,
                        contentDescription = "Перейти к последней просмотренной записи"
                    )
                }
            }

            AnimatedVisibility(visible = showUpButton) {
                Button(
                    modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                    shape = CircleShape,
                    onClick = onUpButtonClick
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

val fakeSavedStateHandle = SavedStateHandle()
val fakeViewModel = SavedStateViewModel(fakeSavedStateHandle)
@Preview
@Composable
fun mainScreenPreview() {

    MainScreen(
        connected = true,
        viewModel = fakeViewModel,
        getBranchList = { emptyList() }
    )
}


@Preview(
    widthDp = 360,
//    widthDp = 720,
)
@Composable
fun headerPreview() {
    MainScreenHeader(
        viewModel = fakeViewModel,
        area = "Великий Новгород Новгород",
        selectedBranch = null,
        branchOptions = emptyList(),
        showUploadButton = true,
        showDeleteBtn = true,
        onUploadButtonClick = { },
        onDeleteButtonClick = { }
    )
}

@Preview
@Composable
fun RecordListPreview() {
    // Mock data for your MainScreenRecordList
    val sortedListToShow = listOf(
        RecordDto(
            "sample area",
            "street",
            "24",
            100.0,
            10000.0,
            "Name Surname",
            "123456",
            "111-TYPE",
            LocalDateTime.now(),
            200.0,
            400.0,
            4000.0,
            2000.0,
            "no comments",
            1234.0,
            1
        ),
        RecordDto(
            "sample area",
            "street",
            "24",
            100.0,
            10000.0,
            "Name Surname",
            "123456",
            "111-TYPE",
            LocalDateTime.now(),
            200.0,
            400.0,
            4000.0,
            2000.0,
            "no comments",
            1234.0,
            1
        ),

        )

    val listState = rememberLazyListState()

    MainScreenRecordList(
        sortedListToShow = sortedListToShow,
        listState = listState,
        navigateToRecord = {}
    )
}

@Preview
@Composable
fun buttonsPreview() {
    MainScreenButtons(showLastButton = true,
        showUpButton = true,
        onLastButtonClick = { /*TODO*/ }) {
    }
}


val records = listOf(
    RecordDto(
        "sample area",
        "street",
        "24",
        100.0,
        10000.0,
        "Name Surname",
        "123456",
        "111-TYPE",
        LocalDateTime.now(),
        200.0,
        400.0,
        4000.0,
        2000.0,
        "no comments",
        1234.0,
        1
    ),
    RecordDto(
        "sample area",
        "street",
        "24",
        100.0,
        10000.0,
        "Name Surname",
        "123456",
        "111-TYPE",
        LocalDateTime.now(),
        200.0,
        400.0,
        4000.0,
        2000.0,
        "no comments",
        1234.0,
        1
    ),

    )