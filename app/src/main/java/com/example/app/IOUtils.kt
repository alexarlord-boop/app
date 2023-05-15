package com.example.app
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IOUtils {

    val FORMAT = "dd.MM.yyyy"
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


}