package com.example.app

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

/*
    Data Transfer Object для записи из файла
*/
// TODO: -refactor data types of cells

@Parcelize
class RecordDto(
    val area: String,
    val street: String,
    val houseNumber: String,
    val flatNumber: Double,
    val account: Double,
    val name: String,
    val puNumber: String,
    val puType: String,
    val lastKoDate: String,
    val lastKo_D: Double,
    val lastKo_N: Double,
    var ko_D: Double,
    var ko_N: Double,
    var comments: String
) : Parcelable {

    override fun toString(): String {
        return "$area | $houseNumber | $name"
    }

}