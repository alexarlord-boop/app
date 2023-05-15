package com.example.app

interface DataHandlerInterface {
    fun updateRowData(position: Int, recordDto: RecordDto, filename: String)

}