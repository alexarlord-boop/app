package com.example.app

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime
import java.util.Date

/*
    Data Transfer Object для записи из файла
*/
// TODO: -refactor data types of cells

data class RecordDto(
    val area: String,
    val street: String,
    val houseNumber: String,
    val flatNumber: Double,
    val account: Double,
    val name: String,
    val puNumber: String,
    val puType: String,
    var lastKoDate: LocalDateTime,
    val lastKo_D: Double,
    val lastKo_N: Double,
    var ko_D: Double,
    var ko_N: Double,
    var comments: String,
    val ID: Double,

    val positionInView: Int
) {

    override fun toString(): String {
        return "RecordDto(area='$area', street='$street', houseNumber='$houseNumber', flatNumber=$flatNumber, account=$account, name='$name', puNumber='$puNumber', puType='$puType', lastKoDate=$lastKoDate, lastKo_D=$lastKo_D, lastKo_N=$lastKo_N, ko_D=$ko_D, ko_N=$ko_N, comments='$comments', ID=$ID, positionInView=$positionInView)"
    }
}