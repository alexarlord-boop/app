package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

const val LAST_FILE_OPENED = "lastFileOpened"

class MainActivity : AppCompatActivity(), RecyclerViewInterface {
    lateinit var launcher: ActivityResultLauncher<Intent>

    lateinit var fileRecords: MutableList<RecordDto>

    lateinit var workbookHandler: WorkBookHandler

    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var fioHeader: TextView
    var filename = "storage/emulated/0/download/control.xls"
    var clickedRecordId = -1
    lateinit var houseHeader: TextView
    lateinit var recyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Getting reference of recyclerView
        recyclerView = findViewById(R.id.list_records)

        // Getting view elements
        btnSelectFile = findViewById(R.id.btn_select)
        area = findViewById(R.id.hood_area)
        fioHeader = findViewById(R.id.fio_header)
        houseHeader = findViewById(R.id.house_header)

        // Setting the layout as linear layout for vertical orientation
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)


        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

    }

    override fun onResume() {
        super.onResume()
        if (clickedRecordId != -1) {
            try {
                reloadData()
            } catch (ex: Exception) {
                println(ex.stackTrace)
            }
        }
    }

    fun reloadData() {
        filename.let {
            workbookHandler = WorkBookHandler(it)
            workbookHandler.readWorkBookFromFile()
            visualiseDataFromXlsFile()
            Log.i("MyLog", "RELOADED")
        }
    }

    /* Business Logic Section */

    @RequiresApi(Build.VERSION_CODES.R) // android 29+
    fun selectFile(view: View) {
        Log.i("MyLog", Environment.isExternalStorageManager().toString())


        try {
            reloadData()
            Toast.makeText(this, "Файл загружен", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            println(ex.stackTrace)
        }
    }


    /*
        читаем данные из выбранного .xls файла и отображаем списком
    */
    private fun visualiseDataFromXlsFile() {

        try {
            fileRecords = workbookHandler.getRecordsFromFile()

            // visualizing
            area.text = workbookHandler.getArea()

            // Sending reference and data to Adapter
            val adapter = RecordAdapter(fileRecords, this) {
                clickedRecordId = it.positionInView
            }

            // Modifying RecyclerView
            recyclerView.apply {
                this.adapter = adapter
                this.layoutManager = LinearLayoutManager(applicationContext)
                this.scrollToPosition(clickedRecordId)
            }
            showListHeaders()
        } catch (ex: Exception) {
            Toast.makeText(this, "Выберите подходящий формат", Toast.LENGTH_SHORT).show()
            println(ex.message)
        }
    }

    fun showListHeaders() {
        fioHeader.visibility = View.VISIBLE
        houseHeader.visibility = View.VISIBLE
    }

    override fun onItemCLick(position: Int) {
        intent = Intent(this, RecordActivity::class.java)
        val clickedRecord = fileRecords[position]
        intent.putExtra("position", position)
        intent.putExtra("record", clickedRecord)
        intent.putExtra("workbookHandler", workbookHandler)
        startActivity(intent)
    }

}