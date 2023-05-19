package com.example.app

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class RecordActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU) // !!!!!!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val name: TextView = findViewById(R.id.record_name)
        val puType: TextView = findViewById(R.id.record_pu_type)
        val puNumber: TextView = findViewById(R.id.record_pu_number)
        val lastCheckDate: TextView = findViewById(R.id.record_last_check)
        val lastCheckDateDay: TextView = findViewById(R.id.record_last_check_day)
        val lastCheckDateNight: TextView = findViewById(R.id.record_last_check_night)
        val newDataDay: EditText = findViewById(R.id.record_current_check_day)
        val newDataNight: EditText = findViewById(R.id.record_current_check_night)
        val newComments: TextInputEditText = findViewById(R.id.textInputEditText)

        val gson = Gson()
        val passedRecord = gson.fromJson(intent.getStringExtra("recordData"), RecordDto::class.java)

        var dataHandler: DataHandlerInterface? = null

        val dataMode = intent.getStringExtra("dataMode")!!.toInt()
        val filename = intent.getStringExtra("filename")!!
        val lastDate = intent.getStringExtra("lastDate")!!
        println(lastDate)

        when (dataMode) {
            0 -> {
//                dataHandler = WorkBookHandler()
//                dataHandler.getRecordsFromFile(filename)
                passedRecord.lastKoDate = IOUtils().convertStringToDate(lastDate)
            }
            1 -> {
                dataHandler = ServerHandler()
                passedRecord.lastKoDate = IOUtils().convertStringToDate(lastDate)
            }
        }

        passedRecord?.let {
            name.text = it.name
            puType.text = it.puType
            puNumber.text = it.puNumber
            lastCheckDate.text = lastDate
            lastCheckDateDay.text = it.lastKo_D.toString().beforeZeroOrBlank()
            lastCheckDateNight.text = it.lastKo_N.toString().beforeZeroOrBlank()
            newDataDay.setText(it.ko_D.toString().beforeZeroOrBlank())
            newDataNight.setText(it.ko_N.toString().beforeZeroOrBlank())
            newComments.setText(it.comments)
        }

        val position = intent.getIntExtra("position", -1)


        val saveBtn: Button = findViewById(R.id.save_btn)

        saveBtn.setOnClickListener {
            val day = newDataDay.text.toString().trim()
            val night = newDataNight.text.toString().trim()
            val inputDay = checkInput(lastCheckDateDay.text.toString(), day)
            val inputNight = checkInput(lastCheckDateNight.text.toString(), night)

            when {
                !inputDay -> newDataDay.error = "Значение должно быть не меньше предыдущего"
                !inputNight -> newDataNight.error = "Значение должно быть не меньше предыдущего"
                day.length > 6 -> newDataDay.error = "Значение не должно превышать лимит"
                night.length > 6 -> newDataNight.error = "Значение не должно превышать лимит"
                else -> {
                    passedRecord?.let { record ->
                        record.ko_D = day.toDoubleOrNull() ?: 0.0
                        record.ko_N = night.toDoubleOrNull() ?: 0.0
                        record.comments = newComments.text.toString()

                        dataHandler?.let { handler ->
                            IOUtils().updateRowData(position, record, filename)
                            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                    }

                }
            }
        }
    }

    fun checkInput(oldValue: String, newValue: String): Boolean {
        return (newValue.isNotEmpty() && newValue.isNotBlank()) &&
                newValue.toDouble() >= oldValue.toDouble()
    }

    private fun String.beforeZeroOrBlank(): String {
        return split(".")[0]
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // destroys activity and returns to the previous one
    }
}