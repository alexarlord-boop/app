package com.example.app.data

import android.content.Context
import android.util.Log
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
        if (newRecords.isNotEmpty()) {
            onAreaChange(newRecords[0].area)
        }
    }

    override val _area: MutableLiveData<String> = MutableLiveData()
    override val area: LiveData<String> = _area

    override fun onAreaChange(newArea: String) {
        _area.value = newArea
    }

    override suspend fun getControllers(): List<ServerHandler.Controller>? {
        val pathToControllers = AppStrings.deviceDirectory + "controllers.json"
        try {
            val controllers =
                IO().jsonToControllerListFiltered(IO().readJsonFromFile(pathToControllers))
            println(controllers)
            return controllers
        } catch (e: java.lang.Exception) {
            Log.e("FILESYSTEM", e.stackTraceToString())
            return null
        }

    }

    override suspend fun getStatementsForController(controllerId: String, branchId: String): MutableList<ServerHandler.RecordStatement> {
        val pathToStatements = AppStrings.deviceDirectory + "statements-$controllerId.json"
        val savedStatementIds = IO().getSavedStatementIds()
        println(savedStatementIds)
        try {
            var statements = Gson().fromJson(
                IO().readJsonFromFile(pathToStatements),
                Array<ServerHandler.RecordStatement>::class.java
            ).toMutableList()
            Log.w("FILESYSTEM", "Statements from file: $statements")
            statements =
                statements.filter { s -> savedStatementIds.contains(s.listNumber) }.toMutableList()
            Log.w("FILESYSTEM", "Statements Filtered: $statements")
            return statements // filter only those, which is present on device (parse filenames...)
        } catch (e: java.lang.Exception) {
            Log.e("FILESYSTEM", e.stackTraceToString())
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
            Toast.makeText(context, "Загружено с устройства", Toast.LENGTH_SHORT).show()
//            Toast.makeText(
//                context,
//                "Получена ведомость $statementId",
//                Toast.LENGTH_SHORT
//            ).show()

        } catch (e: java.lang.Exception) {
            println(e.message)
            Toast.makeText(context, "Сервер недоступен", Toast.LENGTH_SHORT).show()
        }
        return this.listOfRecords.value!!

    }

    override suspend fun getBranchList(): List<Branch> {
        val filePath = AppStrings.branchesFS

        return try {
            val json = IO().readJsonFromFile(filePath)
            IO().getBranchListFromJson(json)
        } catch (e: java.lang.Exception) {
            Log.e("FILESYSTEM", e.stackTraceToString())
            emptyList()
        }

    }

    override suspend fun getControllersForBranch(branchId: String): List<ServerHandler.Controller> {
        val filePath = AppStrings.deviceDirectory + "controllers-${branchId}.json"

        return try {
            val json = IO().readJsonFromFile(filePath)
            IO().jsonToControllerListFiltered(json)
        } catch (e: java.lang.Exception) {
            Log.e("FILESYSTEM", e.stackTraceToString())
            return emptyList()
        }
    }
}