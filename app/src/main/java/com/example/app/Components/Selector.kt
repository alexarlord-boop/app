package com.example.app.Components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Selector(
    selectedValue: T?,
    label: String,
    options: List<T>?,
    getLabel: (T) -> String,
    onValueSelected: (T) -> Unit,
    ctaText: String,
    nullText: String,
    initialExpanded: Boolean = false,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    ExposedDropdownMenuBox(
        expanded = true,
        onExpandedChange = {
            expanded = !expanded
        },
    ) {

        Button(
            modifier = modifier,
            border = BorderStroke(1.dp, color = Color.Black),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            onClick = {})
        {

            Text(text = selectedValue?.let { getLabel(it) } ?: label, textAlign = TextAlign.Center)

        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (options == null) {
                DropdownMenuItem(onClick = {}) {
                    Text(text = nullText)
                }
            } else {
                if (options.isEmpty()) {
                    DropdownMenuItem(onClick = {}) {
                        Text(text = ctaText)
                    }
                } else {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(onClick = {
                            onValueSelected(option)
                            expanded = false
                        }) {
                            Text(text = getLabel(option))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BranchSelectorPreview() {
    Selector(
        selectedValue = null,
        label = "Some init label",
        options = null,
        onValueSelected = { /* Handle selected value */ },
        getLabel = { it },
        ctaText = "Choose something",
        nullText = "No list",
        initialExpanded = false,
        modifier = Modifier
    )
}