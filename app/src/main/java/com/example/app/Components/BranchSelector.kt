//package com.example.app.Components
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material.Button
//import androidx.compose.material.ButtonDefaults
//import androidx.compose.material.DropdownMenuItem
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.ExposedDropdownMenuBox
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.app.data.Branch
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun BranchSelector() {
//    var expanded by remember { mutableStateOf(false) }
////    val selectedBranch = viewModel.selectedBranch.observeAsState(viewModel.defaultBranch)
//    val selectedBranch = "test branch"
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
//            onClick = {}) {
//            val header = if (selectedBranch !== "") selectedBranch else "Филиал"
//            Text(header, textAlign = TextAlign.Center)
//        }
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            options.forEachIndexed { index, optionText ->
//                DropdownMenuItem(onClick = {}) {
//                    Text(text = optionText)
//                }
//            }
//        }
//    }
//}
//
//@Preview
//@Composable
//fun BranchSelectorPreview() {
//    BranchSelector()
//}
