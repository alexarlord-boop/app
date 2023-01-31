package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class recordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val passedRecord = intent.getParcelableExtra<RecordDto>("record")
        Log.i("MyLog", passedRecord.toString())

        val name: TextView = findViewById(R.id.record_name)
        val puType: TextView = findViewById(R.id.record_pu_type)
        val puNumber: TextView = findViewById(R.id.record_pu_number)


        name.text = passedRecord?.name
        puType.text = passedRecord?.puType
        puNumber.text = passedRecord?.puNumber
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // destroys activity and returns to the previous one
    }
}