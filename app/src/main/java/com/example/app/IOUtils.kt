package com.example.app

import android.util.Log
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

    fun saveJsonToFile(jsonString: String, filePath: String) {
        Log.w("FILESYSTEM", "Saved data to: $filePath")
        val file = File(filePath)
        file.writeText(jsonString)
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
        return gson.fromJson(json, Array<ServerRecord>::class.java).toMutableList()
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

}