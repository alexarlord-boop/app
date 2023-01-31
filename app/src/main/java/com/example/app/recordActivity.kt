package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

class recordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val passedRecord = intent.getParcelableExtra<RecordDto>("record")
        Log.i("MyLog", passedRecord.toString())

        val workbookHandler = intent.getParcelableExtra<WorkBookHandler>("workbookHandler")
        Log.i("MyLog", "${workbookHandler?.getArea()}")

        val saveBtn: Button = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener(View.OnClickListener {

        })

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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // destroys activity and returns to the previous one
    }
}