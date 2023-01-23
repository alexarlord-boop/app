package com.example.app

import android.content.Context
import android.os.Environment
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
class WorkBookHandler(val context: Context, fileName: String) {

    val STORAGE_PATH = Environment.getExternalStorageDirectory().toString() + "/"
    private val file: File = File(fileName)
    private lateinit var workbook: Workbook
    lateinit var streetName: String

    /*
        превращает содержимое файла в объект workbook
    */
    fun readWorkBookFromFile() {
        FileInputStream(file).use {
            val workbook = WorkbookFactory.create(it)
            this.workbook = workbook
        }
    }

    /*
        разбирает лист на объекты-записи
    */
    fun getRecordsFromFile(): MutableList<RecordDto> {
        val sheet = workbook.getSheetAt(0)
        val records = mutableListOf<RecordDto>()
        val lastId = sheet.lastRowNum
        for (recordId in 1..lastId) {
            records.add(parseRow(sheet.getRow(recordId)))
        }
        streetName = records[0].street
        return records
    }

    fun parseRow(row: Row): RecordDto {
        return RecordDto(
            row.getCell(0).stringCellValue,
            row.getCell(1).stringCellValue,
            row.getCell(2).stringCellValue,
            row.getCell(3).numericCellValue,
            row.getCell(4).numericCellValue,
            row.getCell(5).stringCellValue,
            row.getCell(6).stringCellValue,
            row.getCell(7).stringCellValue,
            row.getCell(8).numericCellValue,
            row.getCell(9).numericCellValue,
            row.getCell(10).numericCellValue,
            row.getCell(11).numericCellValue,
            row.getCell(12).numericCellValue,
            row.getCell(13).stringCellValue
        )

    }


    private fun saveWorkBookToFile() {
        if (!file.exists()) {
            file.createNewFile()
        }
        FileOutputStream(file).use { fileOut -> workbook.write(fileOut) }
    }


    fun uploadFileToServer() {}

}