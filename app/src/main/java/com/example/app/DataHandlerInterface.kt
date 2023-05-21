package com.example.app

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface DataHandlerInterface {
    val _listOfRecords: MutableLiveData<List<RecordDto>>
    val listOfRecords: LiveData<List<RecordDto>>

    fun onRecordListChange(newRecords: List<RecordDto>)

    val _area: MutableLiveData<String>
    val area: LiveData<String>

    fun onAreaChange(newArea: String)

    suspend fun getControllers(): List<ServerHandler.Controller>

    suspend fun getStatementsForController(id: String): MutableList<ServerHandler.RecordStatement>

    fun getRecordsForStatement(controllerId: String, statementId: String, context: Context): List<RecordDto>

    fun reloadRecordsFromFile(controlId: String, stateId: String, context: Context) {
        val path = "storage/emulated/0/download/control-$controlId-$stateId.json"
        val records =
            IOUtils().convertServerListToRecordDtoList(IOUtils().parseRecordsFromJson(IOUtils().readJsonFromFile(path)))
        onRecordListChange(records)
//        Toast.makeText(context, "Загружена ведомость $stateId", Toast.LENGTH_LONG).show()
    }

}