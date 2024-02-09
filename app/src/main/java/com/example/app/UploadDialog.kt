package com.example.app

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


interface DialogStrings {
    val title: String
    val text: String
    val confirm: String?
    val dismiss: String?
}

data class DefaultUploadDialogStrings(
    override val title: String = "Выгрузка данных",
    override val text: String = "Выгрузка данных",
    override val confirm: String? = "Да",
    override val dismiss: String? = "Нет"
) : DialogStrings

data class DisconnectedUploadDialogStrings(
    override var title: String = "Выгрузка данных",
    override val text: String = "Нет подключения к серверу. Выгрузка недоступна.",
    override val confirm: String? = null,
    override val dismiss: String? = "Закрыть"
) : DialogStrings

data class ConnectedUploadDialogStrings(
    override var title: String = "Выгрузка данных",
    override val text: String = "При выгрузке данных файлы с записями удаляются с устройства. Продолжить?",
    override val confirm: String? = "Да",
    override val dismiss: String? = "Нет"
) : DialogStrings

@Composable
fun UploadDialog(
    onDismissRequest: () -> Unit,
    dialogStrings: DialogStrings,
    onConfirm: (() -> Unit)?,
    onDismiss: (() -> Unit)? = onDismissRequest
) {
    AlertDialog(onDismissRequest = { onDismissRequest() },
        title = { Text(text = dialogStrings.title) },
        text = { Text(text = dialogStrings.text) },
        confirmButton = {
            if (onConfirm != null) {
                Button(onClick = { onConfirm() }) { dialogStrings.confirm?.let { Text(text = it) } }
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                Button(onClick = { onDismiss() }) { dialogStrings.dismiss?.let { Text(text = it) } }
            }

        }

    )
}

@Preview
@Composable
fun previewUploadDialog() {
    UploadDialog(
        dialogStrings = DisconnectedUploadDialogStrings(),
        onDismissRequest = {},
        onConfirm = null,
        onDismiss = {}
    )
}
