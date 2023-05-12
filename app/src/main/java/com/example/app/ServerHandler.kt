package com.example.app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors


class ServerHandler {

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _listOfRecords: MutableLiveData<List<RecordDto>> = MutableLiveData()
    val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    fun onRecordListChange(newRecords: List<RecordDto>) {
        _listOfRecords.value = newRecords
        println(newRecords)
    }

    fun clearList() {
        onRecordListChange(emptyList())
    }

    fun getRecordsFromServer(string: String) {
        viewModelScope.launch {
            try {
                val prettyJson = withContext(Dispatchers.IO) {
                    val url = URL(string)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Accept", "application/json")

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        throw IOException("HTTP response code: $responseCode")
                    }
                }

                val records = convertServerListToRecordDtoList(parseRecordsFromJson(prettyJson))
                onRecordListChange(records)



            } catch (e: Exception) {
                println("API ERR")
                Log.e("SERVER", e.toString())
            }
        }
    }


    fun parseRecordsFromJson(json: String): List<ServerRecord> {
        val gson = Gson()
        return gson.fromJson(json, Array<ServerRecord>::class.java).toList()
    }

    fun getRecordFromJson(json: String): ServerRecord {
        val gson = Gson()
        return gson.fromJson(json, ServerRecord::class.java)
    }

    fun convertServerListToRecordDtoList(serverRecords: List<ServerRecord>): List<RecordDto> {
        return serverRecords.map { it -> convertServerRecordToRecordDto(it) }
    }

    fun convertServerRecordToRecordDto(srv: ServerRecord): RecordDto {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        return RecordDto(
            srv.Area_name,
            srv.Street_name,
            srv.House_name,
            srv.Flat_number.toDouble(),
            srv.AccountUnit_Lnk.toDouble(),
            srv.Person_name,
            srv.AU_number,
            srv.AU_type,
           LocalDateTime.parse(srv.LastDate.replace("[\t\n]+".toRegex(), " "), formatter),
            srv.LastDay_value.toDouble(),
            srv.LastNight_value.toDouble(),
            srv.NewDay_value.toDouble(),
            srv.NewNight_value.toDouble(),
            srv.Comments,
            0.0,
            -1
        )
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
    val Comments: String,
    val LastDate: String,
    val NewDate: String,
    val LastDay_value: String,
    val LastNight_value: String,
    val NewDay_value: String,
    val NewNight_value: String

) {
    override fun toString(): String {
        return "ServerRecord(ListNumber='$ListNumber', ListDate='$ListDate', Source='$Source', Staff_Lnk='$Staff_Lnk', Staff_Name='$Staff_Name', AccountUnit_Lnk='$AccountUnit_Lnk', AU_number='$AU_number', AU_type='$AU_type', Area_name='$Area_name', Street_name='$Street_name', House_name='$House_name', Flat_number='$Flat_number', Person_name='$Person_name', Comments='$Comments', LastDate='$LastDate', NewDate='$NewDate', LastDay_value='$LastDay_value', LastNight_value='$LastNight_value', NewDay_value='$NewDay_value', NewNight_value='$NewNight_value')"
    }
}