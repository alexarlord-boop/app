package com.example.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app.data.Branch
import com.example.app.data.DataHandlerInterface
import com.example.app.data.IOUtils
import com.example.app.record.RecordDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.reflect.typeOf


class ServerHandler : DataHandlerInterface {


    private val viewModelScope = CoroutineScope(Dispatchers.Main)


    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("Coroutine", "Caught an exception: $exception")
    }

    suspend fun fetchDataFromServer(urlString: String): String {
        val url = URL(urlString)
        Log.w("SERVER", "Request to: $url")

        try {
            val conn = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                Log.i("SERVER RESPONSE", response)
                return if (response.length > 1) response else ""
            } else {
                Log.e("SERVER", responseCode.toString())
                throw IOException("HTTP response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("SERVER", e.stackTraceToString())
            return ""
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

    suspend fun sendDataToServer(
        jsonString: String,
        filePath: String,
        statementId: String,
        controllerId: String,
        context: Context
    ): Boolean {
        Log.w("SERVER", "Sending to server: $jsonString")
        val urlString = AppStrings.updateData
        val statementPath = AppStrings.deviceDirectory + "statements-$controllerId.json"
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
//                    val json = IOUtils().readJsonFromFile(statementPath)
//                    val statements = IOUtils().getStatementsFromJson(json)
//                        .filter { it.ListNumber != statementId }
//                    IOUtils().saveJsonToFile(Gson().toJson(statements), statementPath)
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
        val path = AppStrings.deviceDirectory + "record-$controllerId-$statementId.json"
        val urlString = AppStrings.recordsByListId + "?listnumber=$statementId"
        if (File(path).exists()) {
            records = this.reloadRecordsFromFile(controllerId, statementId, context)
//            Toast.makeText(context, "Загружено с устройства", Toast.LENGTH_SHORT).show()
        } else {
//            viewModelScope.launch(exceptionHandler) {
//                try {
//                    val prettyJson = withContext(Dispatchers.IO) {
//                        fetchDataFromServer(urlString)
//                    }
//                    if (prettyJson.isEmpty()) {
//                        Toast.makeText(context, "Пустая запись", Toast.LENGTH_SHORT).show()
//                        return@launch
//                    }
//                    records = IOUtils().convertServerListToRecordDtoList(
//                        IOUtils().parseRecordsFromJson(prettyJson)
//                    )
//                    IOUtils().saveJsonToFile(prettyJson, path)
//                    Toast.makeText(
//                        context,
//                        "Получена ведомость $statementId",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@launch
//
//
//                } catch (e: java.lang.Exception) {
//                    println(e.stackTraceToString())
//                }
//
//            }
            viewModelScope.launch(exceptionHandler) {
                try {
                    val prettyJson = withContext(Dispatchers.IO) {
                        fetchDataFromServer(urlString)
                    }
                    if (prettyJson.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Пустая запись", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    records = IOUtils().convertServerListToRecordDtoList(
                        IOUtils().parseRecordsFromJson(prettyJson)
                    )
                    IOUtils().saveJsonToFile(prettyJson, path)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Получена ведомость $statementId", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    println(e.stackTraceToString())
                }
            }

            return records
        }
        return records
    }

    data class Controller(val Staff_Lnk: String, val Staff_Name: String, val Company_Lnk: String) {}

    override suspend fun getControllers(): List<Controller>? {
        val urlString = AppStrings.controllers
        val pathToControllers = AppStrings.deviceDirectory + "controllers.json"
        try {
            val controllers = withContext(Dispatchers.IO) {
                fetchDataFromServer(urlString)
            }
            if (controllers.isNotEmpty()) {
                IOUtils().saveJsonToFile(controllers, pathToControllers)
                return IOUtils().jsonToControllerListFiltered(controllers)
            } else {
                return null
            }

        } catch (e: IOException) {
            return null
        }
    }

    data class RecordStatement(
        @SerializedName("ListNumber") val listNumber: String,
        @SerializedName("ListDate") val listDate: String,
        @SerializedName("Source") val source: String,
        @SerializedName("Staff_Lnk") val staffLink: String,
        @SerializedName("Staff_Name") val staffName: String,
        @SerializedName("Company_Lnk") val companyLnk: String,
//        @SerializedName("Processed") val processed: String  // removed from API
    )

    override suspend fun getStatementsForController(controllerId: String, branchId: String): List<RecordStatement> {
        val gson = Gson()
        val urlString = AppStrings.statementsByControllerId + controllerId
        val pathToStatements = AppStrings.deviceDirectory + "statements-$controllerId.json"
        try {
            val statements = withContext(Dispatchers.IO) {
                fetchDataFromServer(urlString).trimIndent()
            }

            if (statements != "") {
                val jsonObject = gson.fromJson(statements, JsonObject::class.java)
                val statementList = jsonObject.keySet()
                    .map { key -> gson.fromJson(jsonObject[key], RecordStatement::class.java) }
                    .toMutableList()
                    .filter { it.companyLnk == branchId } //TODO:- after additional field is added
                    .sortedBy { it.listNumber }.toMutableList()
                IOUtils().saveJsonToFile(gson.toJson(statementList), pathToStatements)
                Log.w("STATEMENTS", statementList.toString())
                return statementList
            } else {
                return emptyList()
            }

        } catch (e: IOException) {
            throw IOException("No Statements")
        }
    }


    override suspend fun getBranchList(): List<Branch> {
        val branchList = fetchDataFromServer(AppStrings.branchList).trimIndent()

        if (branchList != "") {
            println(branchList)
            val formattedBranches = branchList.trim()
                .split("},\"")
                .map { it.split("\":{")[1] }
                .map {
                    val linkname = it.split(",")
                    val link = linkname[0].split(":")[1].replace("\"", "")
                    var name = linkname[1].split(":")[1].replace("\"", "").replace("/", "").replace("\\", "")
                    name = if (name != "АО Новгородоблэлектро") name.split("АО")[0] else name
                    Branch(link, name)
                }.toList()

            val json = IOUtils().branchesToJson(formattedBranches)
            val filePath = AppStrings.branchesFS
            IOUtils().saveJsonToFile(json, filePath)

            return formattedBranches
        } else {
            return emptyList()
        }
    }

    override suspend fun getControllersForBranch(branchId: String): List<Controller> {
        if (branchId.isNotEmpty()) {
            val controllerList = fetchDataFromServer(AppStrings.controllersByBranch + branchId.toInt()).trimIndent()
            return try {
                if (controllerList != "") {
                    val filePath = AppStrings.deviceDirectory + "controllers-${branchId}.json"
                    IOUtils().saveJsonToFile(controllerList, filePath)
                    IOUtils().jsonToControllerListFiltered(controllerList)
                } else {
                    emptyList()
                }
            } catch (ex: java.lang.Exception) {
                Log.e("CONTROLLERS", ex.stackTraceToString())
                emptyList()
            }
        }
        return emptyList()
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

//fun main() {
//
//
//    val url = URL("https://indman.nokes.ru/engine/IndManCompanies.php")
//    val connection = url.openConnection() as HttpURLConnection
//    connection.requestMethod = "GET"
//
//    val responseCode = connection.responseCode
//    if (responseCode == HttpURLConnection.HTTP_OK) {
//        val reader = BufferedReader(InputStreamReader(connection.inputStream))
//        val response = StringBuilder()
//
//        var line: String?
//        while (reader.readLine().also { line = it } != null) {
//            response.append(line)
//        }
//        reader.close()
//
//        val responseData = response.toString().trimIndent()
//        // Process the responseData as needed
//        println(responseData)
//
//        val formattedBranches = responseData.trim()
//            .split("},\"")
//            .map { it.split("\":{")[1] }
//            .map {
//                val linkname = it.split(",")
//                val link = linkname[0].split(":")[1]
//                val name = linkname[1].split(":")[1]
//                Branch(link, name)
//            }.toList()
//
//
//        println(formattedBranches)
//
//
//    } else {
//        println("Request failed. Response Code: $responseCode")
//    }
//
//    connection.disconnect()
//
//}