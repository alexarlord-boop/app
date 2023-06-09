package com.example.app.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app.AppStrings
import com.example.app.record.RecordDto
import com.example.app.ServerHandler

interface DataHandlerInterface {
    val _listOfRecords: MutableLiveData<List<RecordDto>>
    val listOfRecords: LiveData<List<RecordDto>>

    fun onRecordListChange(newRecords: List<RecordDto>)

    val _area: MutableLiveData<String>
    val area: LiveData<String>

    fun onAreaChange(newArea: String)

    suspend fun getControllers(): List<ServerHandler.Controller>?

    suspend fun getStatementsForController(id: String): MutableList<ServerHandler.RecordStatement>

    fun getRecordsForStatement(controllerId: String, statementId: String, context: Context): List<RecordDto>

    fun reloadRecordsFromFile(controlId: String, stateId: String, context: Context) {
        val path = AppStrings.deviceDirectory + "control-$controlId-$stateId.json"
        val records =
            IOUtils().convertServerListToRecordDtoList(IOUtils().parseRecordsFromJson(IOUtils().readJsonFromFile(path)))
        onRecordListChange(records)
    }

    fun clearRecordList() {
        onRecordListChange(emptyList())
    }

}