package com.example.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app.data.DataHandlerInterface
import com.example.app.data.IOUtils
import com.example.app.record.RecordDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class ServerHandler : DataHandlerInterface {


    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    override val _listOfRecords: MutableLiveData<List<RecordDto>> = MutableLiveData()
    override val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    override fun onRecordListChange(newRecords: List<RecordDto>) {
        _listOfRecords.value = newRecords
        if (newRecords.isEmpty()) {
            onAreaChange("Район")
        } else {
            onAreaChange(newRecords[0].area)
        }
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

    suspend fun sendPostRequest(urlString: String, parameters: Map<String, String>): String {
        val url = URL(urlString)
        val conn = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true

        val postData = StringBuilder()
        for ((key, value) in parameters) {
            if (postData.isNotEmpty()) {
                postData.append('&')
            }
            postData.append(withContext(Dispatchers.IO) {
                URLEncoder.encode(key, "UTF-8")
            })
            postData.append('=')
            postData.append(withContext(Dispatchers.IO) {
                URLEncoder.encode(value, "UTF-8")
            })
        }

        val outputStream = OutputStreamWriter(conn.outputStream)
        withContext(Dispatchers.IO) {
            outputStream.write(postData.toString())
            outputStream.flush()
        }

        val responseCode = conn.responseCode
        println("HTTP response code: $responseCode")
        val response = if (responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw Exception("HTTP response code: $responseCode")
        }

        withContext(Dispatchers.IO) {
            outputStream.close()
        }
        conn.disconnect()

        return response
    }

    suspend fun sendDataToServer(jsonString: String, filePath: String, statementId: String, controllerId: String, context: Context): Boolean {
        val urlString = AppStrings.updateData
        val statementPath = AppStrings.deviceDirectory + "statements$controllerId.json"
        val parameters = mapOf(
            "ourJSON" to jsonString
        )
        var isSent = false

        try {

            val response = withContext(Dispatchers.IO) {
                sendPostRequest(urlString, parameters)
            }
            println("Response data: $response")
            val cleanedString = response.replace(Regex("[^\\d.]"), "")
            val isSuccessful = cleanedString.toInt() > 0
            println("Successful: $isSuccessful")

            if (isSuccessful) {
                val isDeleted = IOUtils().deleteFile(filePath)
                if (isDeleted) {
                    val json = IOUtils().readJsonFromFile(statementPath)
                    val statements = IOUtils().getStatementsFromJson(json).filter { it.ListNumber != statementId }
                    IOUtils().saveJsonToFile(Gson().toJson(statements), statementPath)
                    println(statements)
                    println(statementPath)
                    Toast.makeText(context, "Данные успешно отправлены", Toast.LENGTH_LONG).show()
                    isSent = true
                }
            } else {
                Toast.makeText(context, "Данные уже отправлены", Toast.LENGTH_LONG).show()
                isSent = false
            }


        } catch (e: java.lang.Exception) {
            println(filePath)
            println(jsonString)
            Log.e("SERVER", e.stackTraceToString())
            Toast.makeText(context, "Данные не отправлены", Toast.LENGTH_LONG).show()
            isSent = false
        }
        return isSent
    }


    override fun getRecordsForStatement(
        controllerId: String,
        statementId: String,
        context: Context
    ): List<RecordDto> {
        var records = emptyList<RecordDto>()
        val path = AppStrings.deviceDirectory + "control-$controllerId-$statementId.json"
        val urlString = AppStrings.recordsByListId + "?listnumber=$statementId"
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
        val urlString = AppStrings.controllers
        val pathToControllers = AppStrings.deviceDirectory + "controllers.json"
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
//        @SerializedName("Processed") val processed: String  // removed from API
    )

    override suspend fun getStatementsForController(id: String): MutableList<RecordStatement> {
        val gson = Gson()
        val urlString = AppStrings.statementsByControllerId + "?Staff_Lnk=$id"
        val pathToStatements = AppStrings.deviceDirectory + "statements$id.json"
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
