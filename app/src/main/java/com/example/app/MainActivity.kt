package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var fileRecords: MutableList<RecordDto>

    lateinit var workbookHandler: WorkBookHandler

    lateinit var btnSelectFile: Button
    lateinit var streetName: TextView
    lateinit var house_header: TextView
    lateinit var flat_header: TextView
    lateinit var account_header: TextView
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting reference of recyclerView
        recyclerView = findViewById(R.id.list_records)

        // Getting view elements
        btnSelectFile = findViewById(R.id.btn_select)
        streetName = findViewById(R.id.street)
        house_header = findViewById(R.id.house_header)
        flat_header = findViewById(R.id.flat_header)
        account_header = findViewById(R.id.account_header)

        // Setting the layout as linear layout for vertical orientation
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = linearLayoutManager



//        workbookHandler = WorkBookHandler(applicationContext)

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

    }


    /* Business Logic Section */

    fun selectFile(view: View) {
        intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            // создания запроса на выбор файла
            startActivityForResult(Intent.createChooser(intent, "Select file"), 100)
        } catch (ex: Exception) {
            println(ex.stackTrace)
        }
    }

    /*
        обработка запросов
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // визуализация данных файла
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            visualiseDataFromXlsFile(data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /*
        читаем данные из выбранного .xls файла и отображаем списком
    */
    private fun visualiseDataFromXlsFile(data: Intent) {
        val uri = data.data
        val fileName = uri?.path?.split(":")?.last()

        try {
            workbookHandler = WorkBookHandler(this, fileName!!)
            workbookHandler.readWorkBookFromFile()
            fileRecords = workbookHandler.getRecordsFromFile()

            // визуализация
            streetName.text = workbookHandler.streetName

            // Sending reference and data to Adapter
            val adapter = RecordAdapter(fileRecords)

            // Setting Adapter to RecyclerView
            recyclerView.setAdapter(adapter);
            recyclerView.layoutManager = LinearLayoutManager(this)
            showListHeaders()

            Toast.makeText(this, "Загружаем данные из $fileName", Toast.LENGTH_LONG).show()

        } catch (ex: Exception) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
            println(ex.stackTrace)
        }

    }

    fun showListHeaders() {
        house_header.visibility = View.VISIBLE
        flat_header.visibility = View.VISIBLE
        account_header.visibility = View.VISIBLE
    }

}