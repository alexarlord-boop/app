package com.example.app

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ServerHandler {

    fun getRecordsFromServer(string: String) {

        // URL REQUEST

        val executor = Executors.newSingleThreadExecutor()
        fun sendGetRequest(urlString: String) {
            executor.execute {
                try {

                    val url = URL(urlString)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Accept", "application/json")


                    val responseCode = conn.responseCode

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        val response = conn.inputStream.bufferedReader().use { it.readText() }

                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(JsonParser.parseString(response))
                        Log.d("Pretty Printed JSON :", prettyJson)

                    } else {
                        Log.e("Server error", "error")
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    println("API ERR")
                    println(e)
                }
            }

        }
        sendGetRequest(string)
        executor.shutdown()



    }
}