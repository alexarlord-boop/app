package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class recordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val position = intent.getIntExtra("position", -1)
        val passedRecord = intent.getParcelableExtra<RecordDto>("record")
        Log.i("MyLog", passedRecord.toString())

        val workbookHandler = intent.getParcelableExtra<WorkBookHandler>("workbookHandler")
        Log.i("MyLog", "${workbookHandler?.getArea()}")

        val name: TextView = findViewById(R.id.record_name)
        val puType: TextView = findViewById(R.id.record_pu_type)
        val puNumber: TextView = findViewById(R.id.record_pu_number)
        val lastCheckDate: TextView = findViewById(R.id.record_last_check)
        val lastCheckDateDay: TextView = findViewById(R.id.record_last_check_day)
        val lastCheckDateNight: TextView = findViewById(R.id.record_last_check_night)



        name.text = passedRecord?.name
        puType.text = passedRecord?.puType
        puNumber.text = passedRecord?.puNumber
        lastCheckDate.text = passedRecord?.lastKoDate
        lastCheckDateDay.text = passedRecord?.lastKo_D
        lastCheckDateNight.text = passedRecord?.lastKo_N

        val saveBtn: Button = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener(View.OnClickListener {
            val newDataDay: EditText = findViewById(R.id.record_current_check_day)
            val newDataNight: EditText = findViewById(R.id.record_current_check_night)
            val comments: TextInputEditText = findViewById(R.id.textInputEditText)

            passedRecord?.let {
                it.ko_D = newDataDay.text.toString()
                it.ko_N = newDataNight.text.toString()
                it.comments = comments.text.toString()
                workbookHandler?.updateRowData(position, it)
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // destroys activity and returns to the previous one
    }
}