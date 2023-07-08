import android.util.Log
import android.view.WindowInsets.Side
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.app.R
import com.example.app.SavedStateViewModel
import com.example.app.ServerHandler
import com.example.app.data.IOUtils
import com.example.app.record.RecordDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecordScreen(viewModel: SavedStateViewModel, navController: NavHostController) {
    val context = LocalContext.current


    BackHandler(true) {

        Log.d("OnBackPressed", "Controller: ${viewModel.selectedControllerName}. Statement: ${viewModel.statementId.value}")
        navController.popBackStack()
    }

    val record = viewModel.selectedRecord.value!!
    val recordId = viewModel.position.value!!
    val filename = viewModel.filename.value!!

    var dayValue by remember { mutableStateOf(record.ko_D.toString().split(".")[0]) }
    var isDayValid by remember { mutableStateOf(true) }
    var nightValue by remember { mutableStateOf(record.ko_N.toString().split(".")[0]) }
    var isNightValid by remember { mutableStateOf(true) }
    var comments by remember { mutableStateOf(record.comments) }

    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(dayValue) {
        if (dayValue === "") {
            isDayValid = false
            errorMessage = "Заполните поле или оставьте значение 0"
            return@LaunchedEffect
        }
        val d = dayValue.toDouble()
        if (d == 0.0) {
            isDayValid = true
            errorMessage = ""
            return@LaunchedEffect
        } else isDayValid = d >= record.lastKo_D
        errorMessage = if (!isDayValid) {
            "Значение должно быть не меньше предыдущего"
        } else {
            ""
        }
    }
    LaunchedEffect(nightValue) {
        if (nightValue === "") {
            isNightValid = false
            errorMessage = "Заполните поле или оставьте значение 0"
            return@LaunchedEffect
        }
        val n = nightValue.toDouble()
        if (n == 0.0) {
            isNightValid = true
            errorMessage = ""
            return@LaunchedEffect
        } else isNightValid = n >= record.lastKo_N
        errorMessage = if (!isNightValid) {
            "Значение должно быть не меньше предыдущего"
        } else {
            ""
        }
    }


    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 30.dp)
            .background(Color.White)
            .pointerInput(Unit) {}
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                keyboardController?.hide()
            },

        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // Record Name
            Column(Modifier.weight(2f)) {
                Text(
                    text = record.name,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = record.lastKoDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        .toString(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight(800),
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Save Button
            Column(Modifier.weight(1f)) {
                Button(
                    onClick = {
                        if (isDayValid && isNightValid) {
                            record?.let { record ->
                                record.ko_D = dayValue.toDoubleOrNull() ?: 0.0
                                record.ko_N = nightValue.toDoubleOrNull() ?: 0.0
                                record.comments = comments

                                IOUtils().updateRowData(recordId, record, filename)
                                viewModel.onPositionChange(-1)
                                Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()

                            }
                        }
                    },
                    modifier = Modifier
                        .align(CenterHorizontally)
                ) {
                    Text(text = "Сохранить")
                }
            }
        }


        // PU Type and Number
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            Row(
            ) {
                Text(
                    text = record.puType,
                    modifier = Modifier
                        .fillMaxWidth(),
                    fontSize = 17.sp
                )
            }
            Row(
            ) {

                Text(
                    text = record.puNumber,
                    modifier = Modifier
                        .fillMaxWidth(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight(800)
                )

            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Last KO Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Последний КО",
                fontSize = 20.sp,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
            )
        }
        // Last Check
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),

                    ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.lastKo_D.toString().split(".")[0],
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Sunny Icon",
                            tint = Black
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.lastKo_N.toString().split(".")[0],
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_dark_mode_24),
                            contentDescription = "Night Icon",
                            tint = Black
                        )
                    }
                }
            }
        }


        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        // Current KO Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Текущий КО",
                fontSize = 20.sp,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
        }

        // Current KO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(
                    value = dayValue,
                    onValueChange = { dayValue = parseNumberInput(it) },
                    isError = !isDayValid,

                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .border(
                            width = 1.dp,
                            color = if (isDayValid) MaterialTheme.colors.primary else MaterialTheme.colors.error,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Icon(Icons.Filled.WbSunny, "", tint = MaterialTheme.colors.primary)
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nightValue,
                    onValueChange = { nightValue = parseNumberInput(it) },
                    isError = !isNightValid,
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .border(
                            width = 1.dp,
                            color = if (isNightValid) MaterialTheme.colors.primary else MaterialTheme.colors.error,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_dark_mode_24),
                            "",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (errorMessage != "") {
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Red
                )
            }
        }

        // Comments Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Примечания",
                style = MaterialTheme.typography.subtitle1,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Comments
        TextField(
            value = comments,
            placeholder = { Text(text = "Введите комментарий") },
            onValueChange = { comments = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 15.dp, vertical = 0.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(10.dp)
                ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

fun parseNumberInput(value: String): String {
    return value.replace(Regex("[^0-9]"), "")
}

//@Preview
//@Composable
//fun PreviewRecordScreen() {
//    val viewModel = MainViewModel()
//    viewModel.onRecordChange(
//        RecordDto(
//            "-",
//            "-",
//            "-",
//            0.0,
//            464985.0,
//            "Алексеева А. И.",
//            "04185523",
//            "Меркурий 230 ART-01 CLN",
//            LocalDateTime.now(),
//            17864.0,
//            0.0,
//            0.0,
//            0.0,
//            "628-294",
//            0.0,
//            0
//        )
//    )
//    RecordScreen(viewModel = viewModel, rememberNavController())
//}
