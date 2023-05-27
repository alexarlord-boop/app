package com.example.app.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app.AppStrings
import com.example.app.record.RecordDto
import com.example.app.ServerHandler
import com.google.gson.Gson
import com.example.app.data.IOUtils as IO
import java.io.IOException

class FileSystemHandler : DataHandlerInterface {

    override val _listOfRecords: MutableLiveData<List<RecordDto>> = MutableLiveData()
    override val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    override fun onRecordListChange(newRecords: List<RecordDto>) {
        _listOfRecords.value = newRecords
        onAreaChange(newRecords[0].area)
    }

    override val _area: MutableLiveData<String> = MutableLiveData()
    override val area: LiveData<String> = _area

    override fun onAreaChange(newArea: String) {
        _area.value = newArea
    }

    override suspend fun getControllers(): List<ServerHandler.Controller> {
        val pathToControllers = AppStrings.deviceDirectory + "controllers.json"
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
        val pathToStatements = AppStrings.deviceDirectory + "statements$id.json"
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