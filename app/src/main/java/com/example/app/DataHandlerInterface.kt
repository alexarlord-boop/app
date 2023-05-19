package com.example.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface DataHandlerInterface {
    val _listOfRecords: MutableLiveData<List<RecordDto>>
    val listOfRecords: LiveData<List<RecordDto>>

    fun onRecordListChange(newRecords: List<RecordDto>)
    suspend fun getControllers(): List<ServerHandler.Controller>

    suspend fun getStatementsForController(id: String): MutableList<ServerHandler.RecordStatement>

    fun getRecordsForStatement(controllerId: String, statementId: String, context: Context): List<RecordDto>


}