package com.example.app
import java.io.File
import java.io.IOException

class IOUtils {

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