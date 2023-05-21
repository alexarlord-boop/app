package com.example.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ServerHandler : DataHandlerInterface {


    private val viewModelScope = CoroutineScope(Dispatchers.Main)
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

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("Coroutine", "Caught an exception: $exception")
    }

    suspend fun fetchDataFromServer(urlString: String): String {
        val url = URL(urlString)
        Log.w("SERVER", "Request to: $url")
        val conn = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw IOException("HTTP response code: $responseCode")
        }
    }


    override fun getRecordsForStatement(
        controllerId: String,
        statementId: String,
        context: Context
    ): List<RecordDto> {
        var records = emptyList<RecordDto>()
        val path = "storage/emulated/0/download/control-$controllerId-$statementId.json"
        val urlString =
            "https://indman.nokes.ru/engine/IndManDataByListNumber.php?listnumber=$statementId"
        if (File(path).exists()) {
            this.reloadRecordsFromFile(controllerId, statementId, context)
        } else {
            viewModelScope.launch(exceptionHandler) {
                try {
                    val prettyJson = withContext(Dispatchers.IO) {
                        fetchDataFromServer(urlString)
                    }
                    records = IOUtils().convertServerListToRecordDtoList(
                        IOUtils().parseRecordsFromJson(prettyJson)
                    )
                    onRecordListChange(records)
                    IOUtils().saveJsonToFile(prettyJson, path)
                    Toast.makeText(
                        context,
                        "Получена ведомость $statementId",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: java.lang.Exception) {
                    println(e.message)
                    Toast.makeText(context, "Сервер недоступен", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return records
    }

    data class Controller(val Staff_Lnk: String, val Staff_Name: String) {}

    override suspend fun getControllers(): List<Controller> {
        val urlString = "https://indman.nokes.ru/engine/IndManListsStaffOnly.php"
        val pathToControllers = "storage/emulated/0/download/controllers.json"
        try {
            val controllers = withContext(Dispatchers.IO) {
                fetchDataFromServer(urlString)
            }
            IOUtils().saveJsonToFile(controllers, pathToControllers)
            return IOUtils().jsonToControllerListFiltered(controllers)
        } catch (e: IOException) {
            return listOf(Controller("-", "-"))
        }
    }

    data class RecordStatement(
        @SerializedName("ListNumber") val listNumber: String,
        @SerializedName("ListDate") val listDate: String,
        @SerializedName("Source") val source: String,
        @SerializedName("Staff_Lnk") val staffLink: String,
        @SerializedName("Staff_Name") val staffName: String,
        @SerializedName("Processed") val processed: String
    )

    override suspend fun getStatementsForController(id: String): MutableList<RecordStatement> {
        val gson = Gson()
        val urlString = "https://indman.nokes.ru/engine/IndManListsByStaff_Lnk.php?Staff_Lnk=$id"
        val pathToStatements = "storage/emulated/0/download/statements$id.json"
        try {
            val statements = withContext(Dispatchers.IO) {
                fetchDataFromServer(urlString).trimIndent()
            }

            val jsonObject = gson.fromJson(statements, JsonObject::class.java)
            val statementList = jsonObject.keySet()
                .map { key -> gson.fromJson(jsonObject[key], RecordStatement::class.java) }
                .toMutableList().sortedBy { it.listNumber }.toMutableList()
            IOUtils().saveJsonToFile(gson.toJson(statementList), pathToStatements)
            return statementList

        } catch (e: IOException) {
            throw IOException("No Statements")
        }
    }



}

data class ServerRecord(
    val ListNumber: String,
    val ListDate: String,
    val Source: String,
    val Staff_Lnk: String,
    val Staff_Name: String,
    val AccountUnit_Lnk: String,
    val AU_number: String,
    val AU_type: String,
    val Area_name: String,
    val Street_name: String,
    val House_name: String,
    val Flat_number: String,
    val Person_name: String,
    var Comments: String,
    val LastDate: String,
    var NewDate: String,
    val LastDay_value: String,
    val LastNight_value: String,
    var NewDay_value: String,
    var NewNight_value: String

) {

    override fun toString(): String {
        return "ServerRecord(ListNumber='$ListNumber', ListDate='$ListDate', Source='$Source', Staff_Lnk='$Staff_Lnk', Staff_Name='$Staff_Name', AccountUnit_Lnk='$AccountUnit_Lnk', AU_number='$AU_number', AU_type='$AU_type', Area_name='$Area_name', Street_name='$Street_name', House_name='$House_name', Flat_number='$Flat_number', Person_name='$Person_name', Comments='$Comments', LastDate='$LastDate', NewDate='$NewDate', LastDay_value='$LastDay_value', LastNight_value='$LastNight_value', NewDay_value='$NewDay_value', NewNight_value='$NewNight_value')"
    }
}
