package com.example.app

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import org.apache.poi.EmptyFileException
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/* This class helps manage an excel file
*  You can download from server, modify in local storage and upload to server unique excel file
*  CRUD methods
*  ---  */

class WorkBookHandler : ViewModel() {


    val FORMAT = "dd.MM.yyyy"
    var workbook: Workbook? = null
    var sheet: Sheet? = null
    var cellStyle: CellStyle? = null

    //    lateinit var records: MutableList<RecordDto>
    var area = ""

    private val _listOfRecords: MutableLiveData<List<RecordDto>> = MutableLiveData()
    val listOfRecords: LiveData<List<RecordDto>> = _listOfRecords

    fun onRecordListChange(newRecords: List<RecordDto>) {
        _listOfRecords.value = newRecords
    }


    fun initHandler() {
        sheet = workbook?.getSheetAt(0)
        cellStyle = sheet?.getRow(1)?.getCell(8)?.cellStyle
        area = workbook?.getSheetAt(0)?.getRow(1)?.getCell(0)?.stringCellValue.toString()
    }


    private fun readWorkBookFromFile(filename: String): Workbook? {
        try {
            val file = File(filename)
            FileInputStream(file).use {
                return WorkbookFactory.create(it)
            }
        }
        catch (ex: EmptyFileException){
            Log.e("MyLog", "${ex.message}")
            throw ex
        }
        catch (ex: FileNotFoundException) {
            Log.e("MyLog", "${ex.message}")
            throw ex
        }
    }

    fun getRecordsFromFile(filename: String) {
        try {
            workbook = readWorkBookFromFile(filename)
            initHandler()
            workbook?.let {
                val sheet = it.getSheetAt(0)
                val records = mutableListOf<RecordDto>()
                val lastId = sheet?.lastRowNum

                for (position in 1..lastId!!) {  // skipping headers in row 0
                    val row = sheet.getRow(position)
                    if (!row.isEmpty()) records.add(parseRow(row, position - 1)) else continue
                }

                onRecordListChange(records)
            }
        }
        catch (ex: EmptyFileException) {
            onRecordListChange(emptyList())
            throw ex
        }
        catch (ex: FileNotFoundException) {
            onRecordListChange(emptyList())
            throw ex
        }

    }


    fun Row.isEmpty(): Boolean {
        return this.getCell(0).stringCellValue.isBlank()
    }

    fun parseRow(row: Row, position: Int): RecordDto {
        return RecordDto(
            row.getCell(0).stringCellValue.trim(),
            row.getCell(1).stringCellValue.trim(),
            row.getCell(2).stringCellValue.trim(),
            row.getCell(3).numericCellValue,
            row.getCell(4).numericCellValue,
            row.getCell(5).stringCellValue.trim(),
            row.getCell(6).stringCellValue.trim(),
            row.getCell(7).stringCellValue.trim(),
            row.getCell(8).localDateTimeCellValue,
            row.getCell(9).numericCellValue,
            row.getCell(10).numericCellValue,
            row.getCell(11).numericCellValue,
            row.getCell(12).numericCellValue,
            row.getCell(13).stringCellValue.trim(),
            row.getCell(14).numericCellValue,
            position
        )

    }

    fun convertDateToFormattedString(date: LocalDateTime): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT)
        return date.format(dateTimeFormatter)
    }

    fun dataToRow(position: Int, recordDto: RecordDto) {
        val row = sheet?.createRow(position)
        row?.let {
            row.createCell(0).setCellValue(recordDto.area)
            row.createCell(1).setCellValue(recordDto.street)
            row.createCell(2).setCellValue(recordDto.houseNumber)
            row.createCell(3).setCellValue(recordDto.flatNumber)
            row.createCell(4).setCellValue(recordDto.account)
            row.createCell(5).setCellValue(recordDto.name)
            row.createCell(6).setCellValue(recordDto.puNumber)
            row.createCell(7).setCellValue(recordDto.puType)

            // passing same date value

            val cell = row.createCell(8)
            cell.setCellValue(recordDto.lastKoDate)
            cell.cellStyle = cellStyle

            row.createCell(9).setCellValue(recordDto.lastKo_D)
            row.createCell(10).setCellValue(recordDto.lastKo_N)
            row.createCell(11).setCellValue(recordDto.ko_D)
            row.createCell(12).setCellValue(recordDto.ko_N)
            row.createCell(13).setCellValue(recordDto.comments)
            row.createCell(14).setCellValue(recordDto.ID)

        }


    }


    fun updateRowData(position: Int, recordDto: RecordDto) {
        dataToRow(position + 1, recordDto)
    }

    fun saveWorkBookToFile(filename: String) {
        try {
            val file = File(filename)
            if (!file.exists()) {
                file.createNewFile()
            }
            FileOutputStream(file).use { fileOut -> workbook?.write(fileOut) }
        } catch (ex: Exception) {
            Log.e("MyLog", ex.message.toString())
        }
    }

    fun getRecordsFromServer(connection: String) {}

}