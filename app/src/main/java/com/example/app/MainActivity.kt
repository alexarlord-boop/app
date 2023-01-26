package com.example.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>

    lateinit var fileRecords: MutableList<RecordDto>

    lateinit var workbookHandler: WorkBookHandler

    lateinit var btnSelectFile: Button
    lateinit var area: TextView
    lateinit var streetName: TextView
    lateinit var fio_header: TextView
    lateinit var house_header: TextView
    lateinit var flat_header: TextView
    lateinit var recyclerView: RecyclerView

    fun getPath(context: Context, uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Downloads.DATA)
        val cursor: Cursor? =
            uri?.let { context.contentResolver.query(it, proj, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = Uri.parse(result.data?.data?.path)
                    Log.d("MyLog", uri.toString())
                    result.data?.data?.path?.let { visualiseDataFromXlsFile(it) }
                }
            }

        // Getting reference of recyclerView
        recyclerView = findViewById(R.id.list_records)

        // Getting view elements
        btnSelectFile = findViewById(R.id.btn_select)
        area = findViewById(R.id.hood_area)
        streetName = findViewById(R.id.street)
        fio_header = findViewById(R.id.fio_header)
        house_header = findViewById(R.id.house_header)
        flat_header = findViewById(R.id.flat_header)

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
        обработка запросов
    */


    /*
        читаем данные из выбранного .xls файла и отображаем списком
    */
    private fun visualiseDataFromXlsFile(filePath: String) {

        val fileName = filePath.split("/").last()
        Log.i("MyLog", fileName)
        try {
            workbookHandler = WorkBookHandler(this, fileName)
            workbookHandler.readWorkBookFromFile()
            fileRecords = workbookHandler.getRecordsFromFile()

            // визуализация
            area.text = workbookHandler.area
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
            println(ex.message)
        }
    }

    fun showListHeaders() {
        fio_header.visibility = View.VISIBLE
        house_header.visibility = View.VISIBLE
        flat_header.visibility = View.VISIBLE
    }

}