package com.example.app.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app.AppStrings
import com.example.app.record.RecordDto
import com.example.app.ServerHandler

// TODO:- refactor method names
// TODO:- refactor data classes -> move here

interface DataHandlerInterface {

    suspend fun getControllers(): List<Controller>?

    suspend fun getStatementsForController(
        controllerId: String,
        branchId: String
    ): List<RecordStatement>

    fun getRecordsForStatement(
        controllerId: String,
        statementId: String,
        context: Context
    ): List<RecordDto>

    // TODO:- remove to IO ?
    fun reloadRecordsFromFile(
        controlId: String,
        stateId: String,
        context: Context
    ): List<RecordDto> {
        if (controlId.isNotEmpty() && stateId.isNotEmpty()) {
            Log.w("RELOAD", "Controller: $controlId, Statement: $stateId")
            val path = AppStrings.deviceDirectory + "record-$controlId-$stateId.json"
            val records = IOUtils().convertServerListToRecordDtoList(
                IOUtils().parseRecordsFromJson(
                    IOUtils().readJsonFromFile(path)
                )
            )
            Log.w("RELOAD", "RecordList size: ${records?.size}")
            return records
        }
        return emptyList()
    }


    suspend fun getBranchList(): List<Branch>

    suspend fun getControllersForBranch(branchId: String): List<Controller>

}

class Branch(val companyLnk: String, val companyName: String)
