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
class WorkBookHandler(val fileName: String) : Parcelable {

    val STORAGE_PATH = Environment.getExternalStoragePublicDirectory("Download")
    private val file: File = File(STORAGE_PATH, fileName)
    var workbook = readWorkBookFromFile()
    var sheet = workbook.getSheetAt(0)
    var records = getRecordsFromFile()

    /*
        превращает содержимое файла в объект workbook
    */
    fun readWorkBookFromFile(): Workbook {
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

    fun dataToRow(position: Int, recordDto: RecordDto) {
        val row = sheet.createRow(position)
        row.createCell(0).setCellValue(recordDto.area)
        row.createCell(1).setCellValue(recordDto.street)
        row.createCell(2).setCellValue(recordDto.houseNumber)
        row.createCell(3).setCellValue(recordDto.flatNumber)
        row.createCell(4).setCellValue(recordDto.account)
        row.createCell(5).setCellValue(recordDto.name)
        row.createCell(6).setCellValue(recordDto.puNumber)
        row.createCell(7).setCellValue(recordDto.puType)
        row.createCell(8).setCellValue(recordDto.lastKoDate)
        row.createCell(9).setCellValue(recordDto.lastKo_D)
        row.createCell(10).setCellValue(recordDto.lastKo_N)
        row.createCell(11).setCellValue(recordDto.ko_D)
        row.createCell(12).setCellValue(recordDto.ko_N)
        row.createCell(13).setCellValue(recordDto.comments)
        saveWorkBookToFile()
    }

    fun getStreet(): String {
        return workbook.getSheetAt(0).getRow(1).getCell(1).stringCellValue
    }

    fun getArea(): String {
        return workbook.getSheetAt(0).getRow(1).getCell(0).stringCellValue
    }

    fun updateRowData(position: Int, recordDto: RecordDto) {
        records[position] = recordDto
        dataToRow(position + 1, recordDto)
    }

    private fun saveWorkBookToFile() {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            FileOutputStream(file).use { fileOut -> workbook.write(fileOut) }
        } catch (ex: Exception) {
            Log.e("MyLog", ex.message.toString())
        }
    }


    fun uploadFileToServer() {}

}