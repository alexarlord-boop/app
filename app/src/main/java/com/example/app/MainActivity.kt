package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import java.util.*

class MainActivity : AppCompatActivity(), RecyclerViewInterface {
    lateinit var launcher: ActivityResultLauncher<Intent>

    lateinit var fileRecords: MutableList<RecordDto>

    lateinit var workbookHandler: WorkBookHandler

    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var streetName: TextView
    lateinit var fioHeader: TextView
    var filename: String? = null
    lateinit var houseHeader: TextView
    lateinit var flatHeader: TextView
    lateinit var recyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.path?.let {
                        filename = it.split("/").last()
                        reloadData()
                    }
                }
            }

        // Getting reference of recyclerView
        recyclerView = findViewById(R.id.list_records)

        // Getting view elements
        btnSelectFile = findViewById(R.id.btn_select)
        area = findViewById(R.id.hood_area)
        streetName = findViewById(R.id.street)
        fioHeader = findViewById(R.id.fio_header)
        houseHeader = findViewById(R.id.house_header)
        flatHeader = findViewById(R.id.flat_header)

        // Setting the layout as linear layout for vertical orientation
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = linearLayoutManager


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
        reloadData()
    }

    fun reloadData() {
        filename?.let {
            workbookHandler = WorkBookHandler(it)
            workbookHandler.readWorkBookFromFile()
            visualiseDataFromXlsFile()
        }
    }

    /* Business Logic Section */

    @RequiresApi(Build.VERSION_CODES.R) // android 29+
    fun selectFile(view: View) {
        Log.i("MyLog", Environment.isExternalStorageManager().toString())

        intent = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Downloads.EXTERNAL_CONTENT_URI)
        intent.type = "*/*"

        try {
            // создания запроса на выбор файла
            launcher.launch(intent)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }


    /*
        читаем данные из выбранного .xls файла и отображаем списком
    */
    private fun visualiseDataFromXlsFile() {

        try {
            fileRecords = workbookHandler.getRecordsFromFile()


            // визуализация
            area.text = workbookHandler.getArea()
            streetName.text = workbookHandler.getStreet()

            // Sending reference and data to Adapter
            val adapter = RecordAdapter(fileRecords, this)

            // Setting Adapter to RecyclerView
            recyclerView.setAdapter(adapter);
            recyclerView.layoutManager = LinearLayoutManager(this)
            showListHeaders()
        } catch (ex: Exception) {
            Toast.makeText(this, "Выберите подходящий формат", Toast.LENGTH_SHORT).show()
            println(ex.message)
        }
    }

    fun showListHeaders() {
        fioHeader.visibility = View.VISIBLE
        houseHeader.visibility = View.VISIBLE
        flatHeader.visibility = View.VISIBLE
    }

    override fun onItemCLick(position: Int) {
        intent = Intent(this, recordActivity::class.java)
        val clickedRecord = fileRecords[position]
        intent.putExtra("position", position)
        intent.putExtra("record", clickedRecord)
        intent.putExtra("workbookHandler", workbookHandler)
        startActivity(intent)
    }

}