package com.example.app.data

import android.util.Log
import com.example.app.AppStrings
import com.example.app.record.RecordDto
import com.example.app.ServerHandler
import com.example.app.ServerRecord
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IOUtils {

    val FORMAT = "dd.MM.yyyy"
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun convertDateToFormattedString(date: LocalDateTime): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT)
        return date.format(dateTimeFormatter)
    }

    fun convertStringToDate(date: String): LocalDateTime {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT)
        return LocalDate.parse(date, dateTimeFormatter).atStartOfDay()
    }

    fun listToJson(list: List<ServerRecord>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    fun jsonToControllerListFiltered(controllers: String): List<ServerHandler.Controller> {

        val gson = Gson()
        Log.w("DATA", controllers)
        if (controllers.trim() === "") {
            return emptyList()
        } else {
            val arrayOfControllers = "[" + controllers
                .substring(0, controllers.length - 1)
                .split("},")
                .map { it.split(":{")[1] }
                .map { "{${it.trim('{').trim('}')}}" }.joinToString(",") + "]"

            Log.w("DATA", arrayOfControllers)
            val controllerList =
                gson.fromJson(arrayOfControllers, Array<ServerHandler.Controller>::class.java)
            println(controllerList)
            return controllerList.filter { it.Staff_Lnk != "0" }
        }
    }

    fun saveJsonToFile(jsonString: String, filePath: String) {
        println(filePath)
        try {
            val file = File(filePath)
            file.writeText(jsonString)
            Log.w("FILESYSTEM", "Saved data to: $filePath")
        } catch (e: java.lang.Exception) {
            Log.e("FILESYSTEM", e.stackTraceToString())
            println(e)
        }
    }


    fun readJsonFromFile(filePath: String): String {
        val jsonString = try {
            File(filePath).readText()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
        return jsonString
    }

    fun parseRecordsFromJson(json: String): MutableList<ServerRecord> {
        val gson = Gson()
        return if (json == "") emptyList<ServerRecord>().toMutableList() else gson.fromJson(json, Array<ServerRecord>::class.java).toMutableList()
    }

    fun convertServerListToRecordDtoList(serverRecords: List<ServerRecord>): List<RecordDto> {
        return serverRecords.map { it -> convertServerRecordToRecordDto(it) }
    }

    fun convertServerRecordToRecordDto(srv: ServerRecord): RecordDto {

//        val d = srv.LastDate.replace("[\t\n]+".toRegex(), " ")
        val cleanedString = srv.LastDate

        return RecordDto(
            srv.Area_name,
            srv.Street_name,
            srv.House_name,
            srv.Flat_number.toDouble(),
            srv.AccountUnit_Lnk.toDouble(),
            srv.Person_name,
            srv.AU_number,
            srv.AU_type,
            LocalDateTime.parse(cleanedString, formatter),
            srv.LastDay_value.toDouble(),
            srv.LastNight_value.toDouble(),
            srv.NewDay_value.toDouble(),
            srv.NewNight_value.toDouble(),
            srv.Comments,
            0.0,
            -1
        )
    }

    fun getSavedStatementIds(): List<String> {
        val directoryPath = AppStrings.deviceDirectory
        val searchTerm = "record-"
        val ids: List<String>
        val directory = File(directoryPath)
        println(directory)
        println()
        val names = directory.listFiles { file ->
            file.isFile && file.name.contains(searchTerm)
        }.map { file -> file.name }
        println(names)
        ids = names.map { name -> name.split("-").last().split(".")[0] }?.toList()!!
        Log.w("STATEMENTS", "Statements ids: $ids")
        return ids

    }

    data class Statement(
        val ListDate: String,
        val ListNumber: String,
        val Source: String,
        val Staff_Lnk: String,
        val Staff_Name: String,
        val Company_Lnk: String
    )

    fun getStatementsFromJson(jsonString: String): MutableList<Statement> {
        val gson = Gson()
        return gson.fromJson(jsonString, Array<Statement>::class.java).toMutableList()
    }

    fun branchesToJson(branches: List<Branch>): String {
        val gson = Gson()
        return gson.toJson(branches)
    }

    fun getBranchListFromJson(jsonData: String): List<Branch> {
        val gson = Gson()
        return gson.fromJson(jsonData, Array<Branch>::class.java).toList()
    }

    fun updateRowData(position: Int, recordDto: RecordDto, filename: String) {

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

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.delete()
    }

}
