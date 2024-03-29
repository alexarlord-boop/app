//package com.example.app
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.*
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import java.io.FileNotFoundException
//import java.util.*
//
//const val LAST_FILE_OPENED = "lastFileOpened"
//
//class MainActivity : AppCompatActivity(), RecyclerViewInterface,
//    AdapterView.OnItemSelectedListener {
//
//
//    lateinit var btnSelectFile: Button
//    lateinit var area: TextView
//    lateinit var fioHeader: TextView
//    var filename = "storage/emulated/0/download/control.xls"
//    lateinit var workbookHandler: WorkBookHandler
//    var clickedRecordId = -1
//    var clickedControllerId = -1
//    lateinit var houseHeader: TextView
//
//    lateinit var recyclerView: RecyclerView
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        filename = updateFileName(filename, clickedRecordId)
//
//        // Getting reference of recyclerView
//        recyclerView = findViewById(R.id.list_records)
//
//        // Getting view elements
//        btnSelectFile = findViewById(R.id.btn_select)
//        area = findViewById(R.id.hood_area)
//        fioHeader = findViewById(R.id.fio_header)
//        houseHeader = findViewById(R.id.house_header)
//
//        // Setting the layout as linear layout for vertical orientation
//        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
//
//        // Setting controller selector
//        val spinner: Spinner = findViewById(R.id.controller_spinner)
//        spinner.onItemSelectedListener = this
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.controller_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter -> // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner
//            spinner.adapter = adapter
//        }
//
//
//        ActivityCompat.requestPermissions(
//            this, arrayOf(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ),
//            PackageManager.PERMISSION_GRANTED
//        )
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (clickedRecordId != -1) {
//            try {
//                reloadData()
//            } catch (ex: Exception) {
//                Log.e("MyLog", "${ex.message}")
//            }
//        }
//    }
//
//    private fun reloadData() {
//        workbookHandler = WorkBookHandler(filename)
//        filename.let {
//            val file = it.split('/').last()
//            try {
//                workbookHandler = WorkBookHandler(it)
//                workbookHandler.readWorkBookFromFile()
////                visualiseData(workbookHandler.records)
//                Toast.makeText(this, "Загружено из: $file", Toast.LENGTH_SHORT).show()
//            } catch (ex: FileNotFoundException) {
//                visualiseData(mutableListOf())
////                workbookHandler.clearRecords()
//
//                Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
//            }
//            Log.i("MyLog", "RELOADED")
//        }
//    }
//
//    /* Business Logic Section */
//
//    @RequiresApi(Build.VERSION_CODES.R) // android 29+
//    fun selectFile(view: View) {
//        reloadData()
//    }
//
//
//    /*
//        читаем данные из выбранного .xls файла и отображаем списком
//    */
//    private fun visualiseData(fileRecords: MutableList<RecordDto>) {
//
//
//        // visualizing
//        area.text = workbookHandler.area
//
//        // Sending reference and data to Adapter
//        val adapter = RecordAdapter(fileRecords, this) {
//            clickedRecordId = it.positionInView
//        }
//
//        // Modifying RecyclerView
//        recyclerView.apply {
//            this.adapter = adapter
//            this.layoutManager = LinearLayoutManager(applicationContext)
//            this.scrollToPosition(clickedRecordId)
//        }
//        showListHeaders()
//    }
//
//    private fun showListHeaders() {
//        fioHeader.visibility = View.VISIBLE
//        houseHeader.visibility = View.VISIBLE
//    }
//
//    override fun onItemCLick(position: Int) {
//        intent = Intent(this, RecordActivity::class.java)
//        val clickedRecord = workbookHandler.records[position]
//        intent.putExtra("position", position)
//        intent.putExtra("record", clickedRecord)
//        intent.putExtra("workbookHandler", workbookHandler)
//        startActivity(intent)
//    }
//
//    private fun updateFileName(filename: String, id: Int): String {
//        return filename.split("/").toMutableList().also {
//            it[it.lastIndex] = "control${id}.xls"
//        }.joinToString("/")
//    }
//
//    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        clickedControllerId = position + 1
//        filename = updateFileName(filename, position + 1)
//        Log.i("MyLog", "SELECTED ITEM: $clickedControllerId")
//    }
//
//    override fun onNothingSelected(parent: AdapterView<*>?) {
//        Log.i("MyLog", "DEFAULT ITEM")
//    }
//
//}