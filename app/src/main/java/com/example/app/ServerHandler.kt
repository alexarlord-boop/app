package com.example.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors


class ServerHandler: DataHandlerInterface {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")


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

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("Coroutine", "Caught an exception: $exception")
    }

    fun getRecordsFromServer(id: String, context: Context) {
        val path = "storage/emulated/0/download/control$id.json"
        viewModelScope.launch(exceptionHandler) {
            try {
                val prettyJson = withContext(Dispatchers.IO) {
                    val url = URL("https://indman.nokes.ru/engine/IndManDataByListNumber.php?listnumber=$id")
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
                IOUtils().saveJsonToFile(prettyJson, path)
                println(records.size)

            Toast.makeText(context, "Загружены записи для контролера $id", Toast.LENGTH_SHORT).show()
            } catch (e: java.lang.Exception) {
                println(e.message)
                Toast.makeText(context, "Сервер недоступен", Toast.LENGTH_SHORT).show()
                reloadRecordsFromFile(id, context)
            }
        }
    }

    fun reloadRecordsFromFile(id: String, context: Context) {
        val path = "storage/emulated/0/download/control$id.json"
        val records = convertServerListToRecordDtoList(parseRecordsFromJson(IOUtils().readJsonFromFile(path)))
        onRecordListChange(records)
        Toast.makeText(context, "Загружены скачанные данные", Toast.LENGTH_LONG).show()
    }

    fun parseRecordsFromJson(json: String): MutableList<ServerRecord> {
        val gson = Gson()
        return gson.fromJson(json, Array<ServerRecord>::class.java).toMutableList()
    }

    fun getRecordFromJson(json: String): ServerRecord {
        val gson = Gson()
        return gson.fromJson(json, ServerRecord::class.java)
    }

    fun getJsonFromRecord(record: ServerRecord): String {
        val gson = Gson()
        return gson.toJson(record)
    }

    fun convertServerListToRecordDtoList(serverRecords: List<ServerRecord>): List<RecordDto> {
        return serverRecords.map { it -> convertServerRecordToRecordDto(it) }
    }

    fun convertServerRecordToRecordDto(srv: ServerRecord): RecordDto {

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

    override fun updateRowData(position: Int, recordDto: RecordDto, filename: String) {
        // update serverDto -> json -> include in full json -> save to file

        val json = IOUtils().readJsonFromFile(filename)
        val records = parseRecordsFromJson(json).sortedBy { it ->
            it.House_name.split("/")[0].filter { it.isDigit() }.toInt()
        }.toMutableList()

        val oldRecord = records[position]

        oldRecord.Comments = recordDto.comments
        oldRecord.NewDay_value = recordDto.ko_D.toString()
        oldRecord.NewNight_value = recordDto.ko_N.toString()
        oldRecord.NewDate = LocalDateTime.now().toString().replace("T", " ")

        records[position] = oldRecord

        val newJson = IOUtils().listToJson(records)
        IOUtils().saveJsonToFile(newJson, filename)

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