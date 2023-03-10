package com.example.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val position = intent.getIntExtra("position", -1)
        val passedRecord = intent.getParcelableExtra<RecordDto>("record")
        Log.i("MyLog", passedRecord.toString())

        val workbookHandler = intent.getParcelableExtra<WorkBookHandler>("workbookHandler")
        Log.i("MyLog", "${workbookHandler?.area}")

        val name: TextView = findViewById(R.id.record_name)
        val puType: TextView = findViewById(R.id.record_pu_type)
        val puNumber: TextView = findViewById(R.id.record_pu_number)
        val lastCheckDate: TextView = findViewById(R.id.record_last_check)
        val lastCheckDateDay: TextView = findViewById(R.id.record_last_check_day)
        val lastCheckDateNight: TextView = findViewById(R.id.record_last_check_night)
        val newDataDay: EditText = findViewById(R.id.record_current_check_day)
        val newDataNight: EditText = findViewById(R.id.record_current_check_night)
        val newComments: TextInputEditText = findViewById(R.id.textInputEditText)

        passedRecord?.let {
            name.text = it.name
            puType.text = it.puType
            puNumber.text = it.puNumber
            lastCheckDate.text = workbookHandler?.convertDateToFormattedString(it.lastKoDate)
            lastCheckDateDay.text = it.lastKo_D.toString().beforeZeroOrBlank()
            lastCheckDateNight.text = it.lastKo_N.toString().beforeZeroOrBlank()


            newDataDay.setText(it.ko_D.toString().beforeZeroOrBlank())
            newDataNight.setText(it.ko_N.toString().beforeZeroOrBlank())
            newComments.setText(it.comments)
        }


        val saveBtn: Button = findViewById(R.id.save_btn)

        saveBtn.setOnClickListener {
            val day = newDataDay.text.toString().trim()
            val night = newDataNight.text.toString().trim()
            val inputDay = checkInput(lastCheckDateDay.text.toString(), day)
            val inputNight = checkInput(lastCheckDateNight.text.toString(), night)

            if (inputDay && inputNight) {
                val comments = newComments.text.toString()

                passedRecord?.also {
                    it.ko_D = if (day != "") day.toDouble() else 0.0
                    it.ko_N = if (night != "") night.toDouble() else 0.0
                    it.comments = comments

                    workbookHandler?.updateRowData(position, it)
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            } else {
                if (!inputDay) {
                    newDataDay.error = "???????????????? ???????????? ???????? ???? ???????????? ??????????????????????"
                }
                if (!inputNight) {
                    newDataNight.error = "???????????????? ???????????? ???????? ???? ???????????? ??????????????????????"
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