package com.example.app

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.example.app.IOUtils as IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class FileSystemHandler : DataHandlerInterface {

    override val _listOfRecords: MutableLiveData<List<RecordDto>> = MutableLiveData()
    override val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    override fun onRecordListChange(newRecords: List<RecordDto>) {
        _listOfRecords.value = newRecords
    }

    override suspend fun getControllers(): List<ServerHandler.Controller> {
        val pathToControllers = "storage/emulated/0/download/controllers.json"
        try {
            val controllers =
                IO().jsonToControllerListFiltered(IO().readJsonFromFile(pathToControllers))
            println(controllers)
            return controllers
        } catch (e: java.lang.Exception) {
            throw IOException("No controllers fetched")
        }

    }

    override suspend fun getStatementsForController(id: String): MutableList<ServerHandler.RecordStatement> {
        val pathToStatements = "storage/emulated/0/download/statements$id.json"
        val savedStatementIds = IO().getSavedStatementIds()
        try {
            var statements = Gson().fromJson(
                IO().readJsonFromFile(pathToStatements),
                Array<ServerHandler.RecordStatement>::class.java
            ).toMutableList()
            statements =
                statements.filter { s -> savedStatementIds.contains(s.listNumber) }.toMutableList()
            return statements // filter only those, which is present on device (parse filenames...)
        } catch (e: java.lang.Exception) {
            throw IOException("No statements fetched")
        }
    }

    override fun getRecordsForStatement(
        controllerId: String,
        statementId: String,
        context: Context
    ): List<RecordDto> {

        try {
            this.reloadRecordsFromFile(controllerId, statementId, context)
            Toast.makeText(
                context,
                "Получена ведомость $statementId",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: java.lang.Exception) {
            println(e.message)
            Toast.makeText(context, "Сервер недоступен", Toast.LENGTH_SHORT).show()
        }
        return this.listOfRecords.value!!

    }
}