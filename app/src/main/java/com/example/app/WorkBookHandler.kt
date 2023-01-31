package com.example.app

import android.os.Environment
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/* This class helps manage an excel file
*  You can download from server, modify in local storage and upload to server unique excel file
*  CRUD methods
*  ---  */
@Parcelize
class WorkBookHandler(val fileName: String): Parcelable {

    val STORAGE_PATH = Environment.getExternalStoragePublicDirectory("Download")
    private val file: File = File(STORAGE_PATH, fileName)
    var workbook = readWorkBookFromFile()

    /*
        превращает содержимое файла в объект workbook
    */
    fun readWorkBookFromFile():Workbook {
        FileInputStream(file).use {
            val workbook = WorkbookFactory.create(it)
            return workbook
        }
    }

    /*
        разбирает лист на объекты-записи
    */
    fun getRecordsFromFile(): MutableList<RecordDto> {
        val sheet = workbook.getSheetAt(0)
        val records = mutableListOf<RecordDto>()
        val lastId = sheet.lastRowNum

        for (recordId in 1..lastId) {  // skipping headers in row 0
            val row = sheet.getRow(recordId)
            if (!row.isEmpty()) records.add(parseRow(row)) else continue
        }

        return records
    }

    fun Row.isEmpty(): Boolean {
        return this.getCell(2).toString().isEmpty()
    }

    fun parseRow(row: Row): RecordDto {
        Log.w("MyLog", row.getCell(8).stringCellValue.toString())
        return RecordDto(
            row.getCell(0).stringCellValue.trim(),
            row.getCell(1).stringCellValue.trim(),
            row.getCell(2).stringCellValue.trim(),
            row.getCell(3).stringCellValue.trim().split(".")[0],
            row.getCell(4).stringCellValue.trim(),
            row.getCell(5).stringCellValue.trim(),
            row.getCell(6).stringCellValue.trim(),
            row.getCell(7).stringCellValue.trim(),
            row.getCell(8).stringCellValue.trim(),
            row.getCell(9).stringCellValue.trim(),
            row.getCell(10).stringCellValue.trim(),
            row.getCell(11).stringCellValue.trim(),
            row.getCell(12).stringCellValue.trim(),
            row.getCell(13).stringCellValue.trim()
        )

    }

    fun getStreet(): String {
        return workbook.getSheetAt(0).getRow(1).getCell(1).stringCellValue
    }

    fun getArea(): String {
        return workbook.getSheetAt(0).getRow(1).getCell(0).stringCellValue
    }

    fun saveRowData() {

    }

    private fun saveWorkBookToFile() {
        if (!file.exists()) {
            file.createNewFile()
        }
        FileOutputStream(file).use { fileOut -> workbook.write(fileOut) }
    }


    fun uploadFileToServer() {}

}