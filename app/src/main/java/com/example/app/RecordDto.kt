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
        return "$area | $houseNumber | $name"
    }

}